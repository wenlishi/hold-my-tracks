package com.track.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 * 用于标记需要权限验证的方法
 *
 * 使用示例：
 * @RequirePermission(resourceType = "track", resourceIdParam = "trackId")
 * public ResponseEntity<Track> getTrack(@PathVariable Long trackId) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 资源类型（如：track, user, point等）
     */
    String resourceType() default "";

    /**
     * 资源ID参数名（方法参数名）
     */
    String resourceIdParam() default "";

    /**
     * 权限操作类型（如：read, write, delete等）
     */
    String operation() default "read";
}