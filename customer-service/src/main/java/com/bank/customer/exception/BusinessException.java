package com.bank.customer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8731876716961767073L;

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
