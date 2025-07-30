package com.bank.user.api.advice;

import com.bank.user.exception.BusinessException;
import com.bank.user.exception.SystemException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ErrorResponse handleApiException(BusinessException ex) {
        return ErrorResponse.builder(ex, ex.getStatus(), ex.getMessage()).build();
    }

    @ExceptionHandler(SystemException.class)
    public ErrorResponse handleApiException(SystemException ex) {
        return ErrorResponse.builder(ex, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgumentException(IllegalArgumentException ex) {
        return ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, ex.getMessage()).build();
    }
}
