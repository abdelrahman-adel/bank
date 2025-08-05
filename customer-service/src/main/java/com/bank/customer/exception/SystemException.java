package com.bank.customer.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class SystemException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4515108214398407800L;

    public SystemException(Throwable cause) {
        super(cause);
    }
}
