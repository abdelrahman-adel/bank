package com.bank.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrors {

    NO_SUCH_ACCOUNT(HttpStatus.NOT_FOUND, "No such account!"),
    MAX_ACCOUNTS_REACHED(HttpStatus.CONFLICT, "Customer has reached the maximum number of accounts!");

    private final HttpStatus httpStatus;
    private final String message;

    BusinessErrors(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public BusinessException exception() {
        return new BusinessException(httpStatus, message);
    }

}
