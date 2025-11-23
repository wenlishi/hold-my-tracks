package com.track.aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理参数校验等异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 场景1：业务逻辑校验失败 (手动 throw 的)
     * 例如：用户名已存在、邮箱已被占用
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(IllegalArgumentException e) {
        logger.warn("参数校验失败: {}", e.getMessage());
        return buildErrorResponse(e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidException(MethodArgumentNotValidException e) {
        // 1. 日志记录：哪个字段校验失败了？
        logger.warn("参数格式校验失败: {}", e.getMessage());
        // 2. 提取错误信息
        // e.getBindingResult() 里面装着所有的错误细节
        // 我们通常只需要拿到第一个具体的错误提示返给前端即可
        String errorMessage = "参数格式错误";
        org.springframework.validation.FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError != null) {
            // 这就是你在 DTO 里写的 message，例如：@NotBlank(message = "用户名不能为空")
            errorMessage = fieldError.getDefaultMessage(); 
        }
        // 3. 返回错误信息
        return buildErrorResponse(errorMessage);

    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        logger.error("捕获到未知异常，类型是: {}", e.getClass().getName());
        logger.error("异常信息: {}", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "系统内部错误");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("message", message);
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.badRequest().body(response);
    }
}