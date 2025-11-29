package com.track.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "JWT认证响应")
@Data
public class JwtResponse {
    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "令牌类型", example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "真实姓名")
    private String realName;

    public JwtResponse(String token, Long id, String username, String email, String realName) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.realName = realName;
    }
}