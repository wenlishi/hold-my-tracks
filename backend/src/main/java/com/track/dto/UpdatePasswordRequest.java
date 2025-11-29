package com.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "修改密码请求")
public class UpdatePasswordRequest {

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", example = "newpassword123")
    private String newPassword;
}