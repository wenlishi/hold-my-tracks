package com.track.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于自动记录方法执行日志
 *
 * 使用示例：
 * @LogOperation(operation = "查询轨迹", resourceId = "#trackId")
 * public ResponseEntity<Track> getTrack(@PathVariable Long trackId) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 操作描述
     * 
     */
    String operation();

    /**
     * 资源ID表达式（支持SpEL表达式，如："#trackId"）
     */
    String resourceId() default "";

    /**
     * 模块名称
     */
    String module() default "";

    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;

    /**
     * 是否记录执行结果
     */
    boolean logResult() default false;
}