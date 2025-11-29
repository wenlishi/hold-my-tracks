package com.track.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(description = "设备实体")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("devices")
public class Device {

    @Schema(description = "设备ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "设备名称")
    @TableField("device_name")
    private String deviceName;

    @Schema(description = "设备类型")
    @TableField("device_type")
    private String deviceType;

    @Schema(description = "设备唯一标识")
    @TableField("device_id")
    private String deviceId;

    @Schema(description = "系统版本")
    @TableField("system_version")
    private String systemVersion;

    @Schema(description = "应用版本")
    @TableField("app_version")
    private String appVersion;

    @Schema(description = "状态：0-离线，1-在线", example = "1")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}