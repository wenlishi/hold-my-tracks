package com.track.validator;

import com.track.annotation.ValidateParam;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * 参数校验工具类
 * 处理 @ValidateParam 注解
 */
@Component
public class ParamValidator {

    /**
     * 校验对象中带有 @ValidateParam 注解的字段
     */
    public void validate(Object obj) {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            ValidateParam validateAnnotation = field.getAnnotation(ValidateParam.class);
            if (validateAnnotation != null) {
                validateField(obj, field, validateAnnotation);
            }
        }
    }

    /**
     * 校验单个字段
     */
    private void validateField(Object obj, Field field, ValidateParam annotation) {
        try {
            field.setAccessible(true);
            Object value = field.get(obj);

            if (value == null) {
                // 空值检查，根据业务需求决定是否允许为空
                return;
            }

            String fieldValue = value.toString();
            String type = annotation.type();
            String message = annotation.message();

            // 根据校验类型进行校验
            switch (type) {
                case "username":
                    validateUsername(fieldValue, message);
                    break;
                case "email":
                    validateEmail(fieldValue, message);
                    break;
                case "phone":
                    validatePhone(fieldValue, message);
                    break;
                case "password":
                    validatePassword(fieldValue, message);
                    break;
                default:
                    // 通用校验
                    validateCommon(fieldValue, annotation);
                    break;
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException("字段访问失败: " + field.getName(), e);
        }
    }

    /**
     * 用户名校验
     */
    private void validateUsername(String username, String message) {
        // 用户名不能包含特殊字符
        if (!Pattern.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]{3,50}$", username)) {
            throw new IllegalArgumentException(message.isEmpty() ?
                "用户名只能包含字母、数字、下划线和中文，长度3-50位" : message);
        }

        // 不能包含敏感词
        if (username.toLowerCase().contains("admin") ||
            username.toLowerCase().contains("root") ||
            username.toLowerCase().contains("system")) {
            throw new IllegalArgumentException(message.isEmpty() ?
                "用户名不能包含敏感词汇" : message);
        }
    }

    /**
     * 邮箱校验
     */
    private void validateEmail(String email, String message) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!Pattern.matches(emailRegex, email)) {
            throw new IllegalArgumentException(message.isEmpty() ?
                "邮箱格式不正确" : message);
        }
    }

    /**
     * 手机号校验
     */
    private void validatePhone(String phone, String message) {
        String phoneRegex = "^1[3-9]\\d{9}$";
        if (!Pattern.matches(phoneRegex, phone)) {
            throw new IllegalArgumentException(message.isEmpty() ?
                "手机号格式不正确" : message);
        }
    }

    /**
     * 密码校验
     */
    private void validatePassword(String password, String message) {
        // 密码强度要求：至少包含字母和数字，长度6-20位
        if (!Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$", password)) {
            throw new IllegalArgumentException(message.isEmpty() ?
                "密码必须包含字母和数字，长度6-20位" : message);
        }
    }

    /**
     * 通用校验
     */
    private void validateCommon(String value, ValidateParam annotation) {
        // 正则表达式校验
        if (!annotation.regex().isEmpty() && !Pattern.matches(annotation.regex(), value)) {
            throw new IllegalArgumentException(annotation.message());
        }

        // 长度校验
        if (annotation.min() > Long.MIN_VALUE || annotation.max() < Long.MAX_VALUE) {
            try {
                long numValue = Long.parseLong(value);
                if (numValue < annotation.min() || numValue > annotation.max()) {
                    throw new IllegalArgumentException(annotation.message());
                }
            } catch (NumberFormatException e) {
                // 如果不是数字，跳过数值范围校验
            }
        }
    }
}