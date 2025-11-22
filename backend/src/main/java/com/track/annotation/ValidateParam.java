package com.track.annotation;

import java.lang.annotation.*;

/**
 * 参数校验注解
 * 用于自定义参数校验规则
 *
 * 使用示例：
 * @ValidateParam(type = "username", message = "用户名格式不正确")
 * private String username;
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateParam {

    /**
     * 校验类型
     */
    String type() default "";

    /**
     * 错误消息
     */
    String message() default "参数校验失败";

    /**
     * 正则表达式（用于正则校验）
     */
    String regex() default "";

    /**
     * 最小值（用于数值校验）
     */
    long min() default Long.MIN_VALUE;

    /**
     * 最大值（用于数值校验）
     */
    long max() default Long.MAX_VALUE;
}