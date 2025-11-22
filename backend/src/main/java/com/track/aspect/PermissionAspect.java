package com.track.aspect;

import com.track.annotation.RequirePermission;
import com.track.entity.Track;
import com.track.security.UserPrincipal;
import com.track.service.TrackService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限验证切面
 * 处理 @RequirePermission 注解
 */
@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private TrackService trackService;

    @Around("@annotation(com.track.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return createUnauthorizedResponse("用户未认证");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission permissionAnnotation = method.getAnnotation(RequirePermission.class);

        String resourceType = permissionAnnotation.resourceType();
        String resourceIdParam = permissionAnnotation.resourceIdParam();
        String operation = permissionAnnotation.operation();

        // 获取资源ID
        Long resourceId = getResourceIdFromParams(joinPoint, signature, resourceIdParam);
        if (resourceId == null) {
            return createNotFoundResponse("资源ID参数不存在: " + resourceIdParam);
        }

        // 根据资源类型进行权限验证
        boolean hasPermission = checkResourcePermission(resourceType, resourceId, userId, operation);
        if (!hasPermission) {
            return createForbiddenResponse("没有操作权限");
        }

        // 执行原方法
        return joinPoint.proceed();
    }

    /**
     * 从方法参数中获取资源ID
     */
    private Long getResourceIdFromParams(ProceedingJoinPoint joinPoint, MethodSignature signature, String resourceIdParam) {
        Object[] args = joinPoint.getArgs();
        Parameter[] parameters = signature.getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(resourceIdParam)) {
                Object arg = args[i];
                if (arg instanceof Long) {
                    return (Long) arg;
                } else if (arg instanceof String) {
                    try {
                        return Long.parseLong((String) arg);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 检查资源权限
     */
    private boolean checkResourcePermission(String resourceType, Long resourceId, Long userId, String operation) {
        switch (resourceType) {
            case "track":
                // 直接查询轨迹，避免冗余的COUNT查询
                Track track = trackService.findByIdAndUserId(resourceId, userId);
                return track != null;
            // 可以扩展其他资源类型的权限检查
            default:
                return false;
        }
    }

    /**
     * 创建未认证响应
     */
    private Object createUnauthorizedResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        response.put("error", "Unauthorized");
        response.put("message", message);
        return response;
    }

    /**
     * 创建禁止访问响应
     */
    private Object createForbiddenResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpServletResponse.SC_FORBIDDEN);
        response.put("error", "Forbidden");
        response.put("message", message);
        return response;
    }

    /**
     * 创建资源不存在响应
     */
    private Object createNotFoundResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpServletResponse.SC_NOT_FOUND);
        response.put("error", "Not Found");
        response.put("message", message);
        return response;
    }
}