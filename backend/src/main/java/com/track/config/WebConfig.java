package com.track.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 注册 LocalDate 转换器，支持 yyyy-MM-dd 格式
        registry.addConverter(String.class, LocalDate.class, source -> {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            return LocalDate.parse(source.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        });
    }
}