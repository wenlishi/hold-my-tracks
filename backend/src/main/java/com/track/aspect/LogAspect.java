package com.track.aspect;

import com.track.annotation.LogOperation;
import com.track.security.UserPrincipal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * 处理 @LogOperation 注解
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.track.annotation.LogOperation)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogOperation logAnnotation = method.getAnnotation(LogOperation.class);

        String operation = logAnnotation.operation();
        String module = logAnnotation.module();
        boolean logParams = logAnnotation.logParams();
        boolean logResult = logAnnotation.logResult();

        // 获取用户信息
        String username = "anonymous";
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            username = userPrincipal.getUsername();
            userId = userPrincipal.getId();
        }

        // 获取资源ID
        String resourceId = getResourceId(joinPoint, logAnnotation.resourceId());

        // 记录开始日志
        Map<String, Object> logData = new HashMap<>();
        logData.put("operation", operation);
        logData.put("module", module.isEmpty() ? signature.getDeclaringType().getSimpleName() : module);
        logData.put("username", username);
        logData.put("userId", userId);
        logData.put("resourceId", resourceId);
        logData.put("method", signature.getName());
        logData.put("startTime", LocalDateTime.now());

        if (logParams) {
            logData.put("parameters", getMethodParameters(joinPoint));
        }

        logger.info("操作开始: {}", logData);

        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录结束日志
            Map<String, Object> endLogData = new HashMap<>();
            endLogData.put("operation", operation);
            endLogData.put("username", username);
            endLogData.put("userId", userId);
            endLogData.put("resourceId", resourceId);
            endLogData.put("success", success);
            endLogData.put("executionTime", executionTime + "ms");
            endLogData.put("endTime", LocalDateTime.now());

            if (!success) {
                endLogData.put("error", errorMsg);
                logger.error("操作失败: {}", endLogData);
            } else {
                if (logResult) {
                    endLogData.put("result", result != null ? result.toString() : "null");
                }
                logger.info("操作完成: {}", endLogData);
            }
        }
    }

    /**
     * 使用SpEL表达式获取资源ID
     */
    private String getResourceId(ProceedingJoinPoint joinPoint, String resourceIdExpression) {
        if (resourceIdExpression == null || resourceIdExpression.isEmpty()) {
            return null;
        }

        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();

            // 设置参数到上下文
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }

            Expression expression = parser.parseExpression(resourceIdExpression);
            Object result = expression.getValue(context);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            logger.warn("解析资源ID表达式失败: {}", resourceIdExpression, e);
            return null;
        }
    }


    /**
     * 获取方法参数（已优化：过滤掉框架对象和敏感字段）
     */
    private Map<String, Object> getMethodParameters(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = new HashMap<>();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        for (int i = 0; i < parameterNames.length; i++) {
            Object arg = args[i];

            // --- 优化开始：过滤掉不需要记录的参数类型 ---
            if (arg == null) {
                params.put(parameterNames[i], null);
                continue;
            }

            // 1. 过滤 Spring Security 的 Authentication 对象 (因为外层已经记录了 User)
            if (arg instanceof Authentication) {
                continue;
            }
            // 2. 过滤 Request / Response 对象 (太大且没用)
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            }
            // 3. 过滤 @Valid 的校验结果 BindingResult
            if (arg instanceof BindingResult) {
                continue;
            }
            // 4. 过滤文件上传对象 (二进制文件没法打印)
            if (arg instanceof MultipartFile || arg instanceof MultipartFile[]) {
                continue;
            }
            // --- 优化结束 ---

            // 5. 敏感参数名脱敏 (如密码)
            if (!parameterNames[i].toLowerCase().contains("password")) {
                params.put(parameterNames[i], arg);
            }
        }
        return params;
    }
}