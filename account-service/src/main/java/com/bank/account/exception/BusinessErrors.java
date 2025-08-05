package com.bank.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrors {

    NO_SUCH_ACCOUNT(HttpStatus.NOT_FOUND, "The requested account does not exist."),
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "The customer for this account could not be found."),
    CUSTOMER_INACTIVE(HttpStatus.BAD_REQUEST, "The customer for this account is not active."),
    ACCOUNT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "This customer has reached the maximum number of allowed accounts."),
    SALARY_ACCOUNT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "This customer already has a salary account."),
    INVESTMENT_ACCOUNT_MIN_BALANCE(HttpStatus.BAD_REQUEST, "Investment accounts must have a minimum balance of 10,000."),
    RETAIL_CUSTOMER_ACCOUNT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "Retail customers can only open savings accounts.");

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
