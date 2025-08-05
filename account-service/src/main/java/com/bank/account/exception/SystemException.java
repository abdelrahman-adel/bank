package com.bank.account.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class SystemException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7150895591088558644L;

    public SystemException(Throwable cause) {
        super(cause);
    }
}
