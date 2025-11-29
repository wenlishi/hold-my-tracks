package com.track.aspect;

import com.track.common.Result;
import com.track.common.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理参数校验等异常
 * @RestControllerAdvice 此注解使得它能捕获所有进入 Controller 之后的异常。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 场景1：业务逻辑校验失败 (手动 throw 的)
     * 例如：用户名已存在、邮箱已被占用
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Object>> handleValidationException(IllegalArgumentException e) {
        logger.warn("参数校验失败: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Result.validateFailed(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Result<Object>> handleBadCredentialsException(BadCredentialsException e) {
        logger.warn( "登录失败：{}", e.getMessage());
        return ResponseEntity.badRequest().body(Result.failed("用户名或密码错误"));
    }

    /**
     * 场景2：参数格式校验失败 (Spring @Valid 自动触发)
     * 例如：@NotBlank, @Size
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleValidException(MethodArgumentNotValidException e) {
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
        return ResponseEntity.badRequest().body(Result.validateFailed(errorMessage));

    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleGeneralException(Exception e) {
        logger.error("捕获到未知异常，类型是: {}", e.getClass().getName());
        logger.error("异常信息: {}", e.getMessage(), e);

        // 生产环境通常不返回 e.getMessage() 给前端，怕暴露敏感信息
        String errorMessage = "系统繁忙，请稍后再试";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.failed(ResultCode.INTERNAL_SERVER_ERROR.getCode(), errorMessage));
    }
}