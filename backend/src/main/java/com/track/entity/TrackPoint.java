package com.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "轨迹点实体")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("track_points")
public class TrackPoint {

    @Schema(description = "轨迹点ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "轨迹ID")
    @TableField("track_id")
    private Long trackId;

    @Schema(description = "经度", example = "116.397128")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "39.916527")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "海拔（米）", example = "50.5")
    @TableField("altitude")
    private BigDecimal altitude;

    @Schema(description = "速度（米/秒）", example = "5.2")
    @TableField("speed")
    private BigDecimal speed;

    @Schema(description = "定位精度（米）", example = "10.0")
    @TableField("accuracy")
    private BigDecimal accuracy;

    @Schema(description = "卫星数量", example = "8")
    @TableField("satellite_count")
    private Integer satelliteCount;

    @Schema(description = "地址信息", example = "北京市朝阳区")
    @TableField("address")
    private String address;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}