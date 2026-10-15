package com.demo.framework.exception;

import com.demo.framework.result.ResultCode;
import lombok.Getter;

/**
 * 可预期的业务失败异常，由 GlobalExceptionHandler 统一转换为 HTTP 响应体。
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    public BusinessException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }
}
