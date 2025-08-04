package com.bank.customer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrors {

    NO_SUCH_CUSTOMER(HttpStatus.NOT_FOUND, "No such customer!"),
    CUSTOMER_LEGAL_ID_USED(HttpStatus.CONFLICT, "Customer Legal ID is already used!");

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