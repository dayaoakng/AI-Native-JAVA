package com.demo.framework.result;

import lombok.Getter;

/**
 * 业务状态码及默认提示语。
 */
@Getter
public enum ResultCode {
    SUCCESS("200", "操作成功"),

    BAD_REQUEST("400", "请求参数错误"),
    UNAUTHORIZED("401", "未登录或登录已过期"),
    FORBIDDEN("403", "无权限访问"),
    NOT_FOUND("404", "资源不存在"),

    BUSINESS_ERROR("600", "业务处理失败"),
    SYSTEM_ERROR("601", "系统繁忙，请稍后重试"),
    DATA_NOT_FOUND("602", "数据不存在"),
    DATA_DUPLICATE("603", "数据已存在");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
