package com.track.dto;

import com.track.entity.Track;
import com.track.entity.TrackPoint;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 轨迹详情数据传输对象
 * 包含轨迹基本信息和轨迹点列表
 */
@Schema(description = "轨迹详情响应")
@Data
@EqualsAndHashCode(callSuper = false)
public class TrackDetail {

    @Schema(description = "轨迹基本信息")
    private Track track;

    @Schema(description = "轨迹点列表")
    private List<TrackPoint> trackPoints;

    @Schema(description = "轨迹统计信息")
    private TrackStats stats;

    @Schema(description = "轨迹统计信息")
    @Data
    public static class TrackStats {
        @Schema(description = "轨迹总长度（米）", example = "1500.50")
        private BigDecimal totalDistance;

        @Schema(description = "轨迹总点数", example = "100")
        private Integer totalPoints;

        @Schema(description = "平均速度（米/秒）", example = "5.2")
        private BigDecimal averageSpeed;

        @Schema(description = "最大速度（米/秒）", example = "15.8")
        private BigDecimal maxSpeed;

        @Schema(description = "轨迹持续时间（秒）", example = "7200")
        private Long duration;

        @Schema(description = "海拔变化（米）", example = "150.5")
        private BigDecimal altitudeChange;
    }
}