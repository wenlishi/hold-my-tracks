package com.track.dto;

import com.track.entity.Track;
import com.track.entity.TrackPoint;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 轨迹详情数据传输对象
 * 包含轨迹基本信息和轨迹点列表
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TrackDetail {

    /**
     * 轨迹基本信息
     */
    private Track track;

    /**
     * 轨迹点列表
     */
    private List<TrackPoint> trackPoints;

    /**
     * 轨迹统计信息
     */
    private TrackStats stats;

    /**
     * 轨迹统计信息内部类
     */
    @Data
    public static class TrackStats {
        /**
         * 轨迹总长度（米）
         */
        private BigDecimal totalDistance;

        /**
         * 轨迹总点数
         */
        private Integer totalPoints;

        /**
         * 平均速度（米/秒）
         */
        private BigDecimal averageSpeed;

        /**
         * 最大速度（米/秒）
         */
        private BigDecimal maxSpeed;

        /**
         * 轨迹持续时间（秒）
         */
        private Long duration;

        /**
         * 海拔变化（米）
         */
        private BigDecimal altitudeChange;
    }
}