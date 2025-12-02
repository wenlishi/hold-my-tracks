package com.track.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.track.entity.TrackPoint;

import java.util.List;

public interface TrackPointService extends IService<TrackPoint> {

    List<TrackPoint> findByTrackId(Long trackId);

    /**
     * 批量保存轨迹点，并进行轨迹处理
     *
     * @param trackPoints 轨迹点列表
     * @param trackId 轨迹ID
     * @return 保存后的轨迹点列表
     */
    List<TrackPoint> saveBatchWithProcessing(List<TrackPoint> trackPoints, Long trackId);

    /**
     * 对现有轨迹点进行轨迹处理
     *
     * @param trackId 轨迹ID
     * @return 处理后的轨迹点数量
     */
    int processTrackPoints(Long trackId);

    /**
     * 获取轨迹的原始轨迹点（用于热力图）
     *
     * @param trackId 轨迹ID
     * @return 原始轨迹点列表
     */
    List<TrackPoint> getRawPointsForHeatmap(Long trackId);

    /**
     * 获取压缩后的轨迹点（用于轨迹展示）
     *
     * @param trackId 轨迹ID
     * @param tolerance 压缩容差（米）
     * @return 压缩后的轨迹点列表
     */
    List<TrackPoint> getCompressedPoints(Long trackId, double tolerance);
}