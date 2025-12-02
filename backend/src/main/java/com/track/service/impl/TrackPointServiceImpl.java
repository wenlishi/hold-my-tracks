package com.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.track.entity.TrackPoint;
import com.track.mapper.TrackPointMapper;
import com.track.service.TrackPointService;
import com.track.starter.model.Point;
import com.track.starter.pipeline.TrajectoryPipeline;
import com.track.starter.service.CompressionService;
import com.track.starter.service.NoiseFilterService;
import com.track.util.PointConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class TrackPointServiceImpl extends ServiceImpl<TrackPointMapper, TrackPoint> implements TrackPointService {

    private static final Logger log = LoggerFactory.getLogger(TrackPointServiceImpl.class);

    @Autowired
    private TrackPointMapper trackPointMapper;

    @Autowired(required = false)
    private TrajectoryPipeline trajectoryPipeline;

    @Autowired(required = false)
    private CompressionService compressionService;

    @Autowired(required = false)
    private NoiseFilterService noiseFilterService;

    @Override
    public List<TrackPoint> findByTrackId(Long trackId) {
        QueryWrapper<TrackPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("track_id", trackId);
        queryWrapper.orderByAsc("create_time");
        return trackPointMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TrackPoint> saveBatchWithProcessing(List<TrackPoint> trackPoints, Long trackId) {
        if (trackPoints == null || trackPoints.isEmpty()) {
            return Collections.emptyList();
        }

        // 如果轨迹处理流水线未启用，直接保存
        if (trajectoryPipeline == null) {
            log.info("轨迹处理未启用，直接保存 {} 个轨迹点", trackPoints.size());
            saveBatch(trackPoints);
            return trackPoints;
        }

        try {
            log.info("开始处理轨迹点，原始数量: {}", trackPoints.size());

            // 转换为Point列表
            List<Point> points = PointConverter.toPoints(trackPoints);

            // 使用轨迹处理流水线进行处理
            List<Point> processedPoints = trajectoryPipeline.process(points);

            double compressionRate = (1.0 - (double) processedPoints.size() / points.size()) * 100;
            log.info("轨迹处理完成，原始数量: {}, 处理后数量: {}, 压缩率: {}%",
                    points.size(), processedPoints.size(), String.format("%.2f", compressionRate));

            // 转换回TrackPoint
            List<TrackPoint> processedTrackPoints = PointConverter.toTrackPoints(processedPoints, trackId);

            // 保存处理后的轨迹点
            saveBatch(processedTrackPoints);

            return processedTrackPoints;

        } catch (Exception e) {
            log.error("轨迹处理失败，将直接保存原始数据", e);
            // 如果处理失败，保存原始数据
            saveBatch(trackPoints);
            return trackPoints;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processTrackPoints(Long trackId) {
        if (trajectoryPipeline == null) {
            log.warn("轨迹处理未启用，无法处理轨迹点");
            return 0;
        }

        try {
            // 获取原始轨迹点
            List<TrackPoint> originalPoints = findByTrackId(trackId);
            if (originalPoints.isEmpty()) {
                log.info("轨迹 {} 没有轨迹点需要处理", trackId);
                return 0;
            }

            log.info("开始处理轨迹 {} 的轨迹点，原始数量: {}", trackId, originalPoints.size());

            // 转换为Point列表
            List<Point> points = PointConverter.toPoints(originalPoints);

            // 使用轨迹处理流水线进行处理
            List<Point> processedPoints = trajectoryPipeline.process(points);

            double compressionRate = (1.0 - (double) processedPoints.size() / points.size()) * 100;
            log.info("轨迹 {} 处理完成，原始数量: {}, 处理后数量: {}, 压缩率: {}%",
                    trackId, points.size(), processedPoints.size(), String.format("%.2f", compressionRate));

            // 删除原始轨迹点
            QueryWrapper<TrackPoint> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("track_id", trackId);
            trackPointMapper.delete(deleteWrapper);

            // 转换回TrackPoint并保存
            List<TrackPoint> processedTrackPoints = PointConverter.toTrackPoints(processedPoints, trackId);
            saveBatch(processedTrackPoints);

            return processedTrackPoints.size();

        } catch (Exception e) {
            log.error("处理轨迹 {} 的轨迹点失败", trackId, e);
            throw new RuntimeException("轨迹处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TrackPoint> getRawPointsForHeatmap(Long trackId) {
        // 获取原始轨迹点（只进行去噪，不压缩）
        List<TrackPoint> rawPoints = findByTrackId(trackId);

        if (rawPoints.isEmpty()) {
            return rawPoints;
        }

        try {
            // 转换为Point列表
            List<Point> points = PointConverter.toPoints(rawPoints);

            // 只进行去噪处理，不压缩
            List<Point> filteredPoints = points;

            // 如果有噪声过滤服务，进行过滤
            if (noiseFilterService != null) {
                log.info("开始处理轨迹 {} 的热力图数据，原始点数: {}", trackId, points.size());

                // 使用噪声过滤服务进行过滤
                // 热力图需要保留更多细节，所以使用较宽松的过滤参数
                filteredPoints = noiseFilterService.filter(
                        points,
                        200.0,  // 最大速度阈值：200 km/h（过滤异常漂移点）
                        0.0,    // 最小速度阈值：0.1 km/h（过滤静止点）
                        100.0,  // 最大精度阈值：100米（过滤低精度点）
                        600000, // 最大时间间隔：10分钟（过滤异常时间间隔）
                        10000.0 // 最大距离阈值：10公里（过滤异常跳跃点）
                );

                double filterRate = (1.0 - (double) filteredPoints.size() / points.size()) * 100;
                log.info("轨迹 {} 热力图数据处理完成，原始点数: {}, 过滤后点数: {}, 过滤率: {}%",
                        trackId, points.size(), filteredPoints.size(), String.format("%.2f", filterRate));
            } else if (trajectoryPipeline != null) {
                // 如果单独的服务不可用，但流水线可用，使用流水线的过滤功能
                log.info("使用轨迹流水线处理轨迹 {} 的热力图数据，原始点数: {}", trackId, points.size());
                filteredPoints = trajectoryPipeline.filterOnly(points);
                log.info("轨迹 {} 热力图数据处理完成，过滤后点数: {}", trackId, filteredPoints.size());
            } else {
                log.info("轨迹处理未启用，返回原始热力图数据，点数: {}", points.size());
            }

            // 转换回TrackPoint
            return PointConverter.toTrackPoints(filteredPoints, trackId);

        } catch (Exception e) {
            log.error("获取热力图数据失败，返回原始数据", e);
            return rawPoints;
        }
    }

    @Override
    public List<TrackPoint> getCompressedPoints(Long trackId, double tolerance) {
        // 获取原始轨迹点
        List<TrackPoint> rawPoints = findByTrackId(trackId);

        if (rawPoints.isEmpty() || compressionService == null) {
            return rawPoints;
        }

        try {
            // 转换为Point列表
            List<Point> points = PointConverter.toPoints(rawPoints);

            // 进行轨迹压缩
            List<Point> compressedPoints = compressionService.compress(points, tolerance);

            double compressionRate = (1.0 - (double) compressedPoints.size() / points.size()) * 100;
            log.info("轨迹 {} 压缩完成，原始点数: {}, 压缩后点数: {}, 压缩率: {}%",
                    trackId, points.size(), compressedPoints.size(), String.format("%.2f", compressionRate));

            // 转换回TrackPoint
            return PointConverter.toTrackPoints(compressedPoints, trackId);

        } catch (Exception e) {
            log.error("轨迹压缩失败，返回原始数据", e);
            return rawPoints;
        }
    }
}