package com.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "更新设备信息请求")
public class UpdateDeviceInfoRequest {

    @NotBlank(message = "设备型号不能为空")
    @Schema(description = "设备型号", example = "iPhone 14 Pro")
    private String deviceModel;
}