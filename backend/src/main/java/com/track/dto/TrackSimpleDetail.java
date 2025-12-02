package com.track.dto;

import com.track.entity.Track;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 轨迹简化详情数据传输对象
 * 包含轨迹基本信息和统计信息，不包含轨迹点列表
 * 用于减少数据传输量，提高性能
 */
@Schema(description = "轨迹简化详情响应（不包含轨迹点数据）")
@Data
@EqualsAndHashCode(callSuper = false)
public class TrackSimpleDetail {

    @Schema(description = "轨迹基本信息")
    private Track track;

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