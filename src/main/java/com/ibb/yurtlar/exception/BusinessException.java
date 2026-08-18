package com.ibb.yurtlar.exception;

import com.ibb.yurtlar.exception.reason.BusinessExceptionReason;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;

    public BusinessException(
            BusinessExceptionReason reason,
            Object... parameters
    ) {
        super(reason.formatMessage(parameters));

        this.code = reason.getCode();
        this.httpStatus = reason.getHttpStatus();
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}