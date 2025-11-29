package com.track.common;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 响应状态码枚举
 * 定义标准的HTTP响应状态码和业务状态码
 */
@Schema(description = "响应状态码枚举")
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAILED(500, "操作失败"),

    /**
     * 参数验证失败
     */
    VALIDATE_FAILED(400, "参数验证失败"),

    /**
     * 未登录
     */
    UNAUTHORIZED(401, "暂未登录或token已经过期"),

    /**
     * 未授权
     */
    FORBIDDEN(403, "没有相关权限"),

    /**
     * 资源未找到
     */
    NOT_FOUND(404, "资源未找到"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    /**
     * 系统内部错误
     */
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    /**
     * 业务异常
     */
    BUSINESS_ERROR(1000, "业务异常"),

    /**
     * 数据已存在
     */
    DATA_EXISTS(1001, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXISTS(1002, "数据不存在"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILED(1003, "文件上传失败"),

    /**
     * 文件下载失败
     */
    FILE_DOWNLOAD_FAILED(1004, "文件下载失败"),

    /**
     * 数据库操作失败
     */
    DATABASE_ERROR(1005, "数据库操作失败"),

    /**
     * 网络连接异常
     */
    NETWORK_ERROR(1006, "网络连接异常"),

    /**
     * 第三方服务异常
     */
    THIRD_PARTY_ERROR(1007, "第三方服务异常"),

    /**
     * 缓存异常
     */
    CACHE_ERROR(1008, "缓存异常");

    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private final Integer code;

    /**
     * 状态消息
     */
    @Schema(description = "状态消息")
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}