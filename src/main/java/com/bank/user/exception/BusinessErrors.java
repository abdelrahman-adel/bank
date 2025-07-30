package com.bank.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrors {

    NO_SUCH_USER(HttpStatus.NOT_FOUND, "No such user!"),
    USER_EXPIRED(HttpStatus.CONFLICT, "User Civil ID is expired!"),
    USER_CIVIL_ID_USED(HttpStatus.CONFLICT, "User Civil ID is already used!"),
    INVALID_REQUEST_TYPE(HttpStatus.BAD_REQUEST, "Invalid request type!");

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
