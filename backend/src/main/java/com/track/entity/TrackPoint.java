package com.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("track_points")
public class TrackPoint {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("track_id")
    private Long trackId;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("altitude")
    private BigDecimal altitude;

    @TableField("speed")
    private BigDecimal speed;

    @TableField("accuracy")
    private BigDecimal accuracy;

    @TableField("satellite_count")
    private Integer satelliteCount;

    @TableField("address")
    private String address;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}