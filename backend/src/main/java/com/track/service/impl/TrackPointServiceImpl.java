package com.track.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.track.entity.TrackPoint;
import com.track.mapper.TrackPointMapper;
import com.track.service.TrackPointService;
import com.track.starter.model.Point;
import com.track.starter.pipeline.TrajectoryPipeline;
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

            log.info("轨迹处理完成，原始数量: {}, 处理后数量: {}, 压缩率: {:.2f}%",
                    points.size(), processedPoints.size(),
                    (1.0 - (double) processedPoints.size() / points.size()) * 100);

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

            log.info("轨迹 {} 处理完成，原始数量: {}, 处理后数量: {}, 压缩率: {:.2f}%",
                    trackId, points.size(), processedPoints.size(),
                    (1.0 - (double) processedPoints.size() / points.size()) * 100);

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
}