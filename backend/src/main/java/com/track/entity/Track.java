package com.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "轨迹实体")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("tracks")
public class Track {

    @Schema(description = "轨迹ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "轨迹名称")
    @TableField("track_name")
    private String trackName;

    @Schema(description = "轨迹描述")
    @TableField("description")
    private String description;

    @Schema(description = "开始时间", example = "2024-01-01 10:00:00")
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-01-01 12:00:00")
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "总距离（米）", example = "1500.50")
    @TableField("total_distance")
    private BigDecimal totalDistance;

    @Schema(description = "总点数", example = "100")
    @TableField("total_points")
    private Integer totalPoints;

    @Schema(description = "状态：0-进行中，1-已完成，2-已删除", example = "1")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}