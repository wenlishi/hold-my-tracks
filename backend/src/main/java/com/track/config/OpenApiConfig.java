package com.track.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI配置类
 * 配置JWT认证，使Swagger UI显示Authorize按钮
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置OpenAPI，添加JWT安全方案
     * @return OpenAPI实例
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("个人轨迹管理与分析系统 API")
                .version("1.0.0")
                .description("个人轨迹管理与分析系统后端API文档")
            )
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .addSecurityItem(
                new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth")
            );
    }
}