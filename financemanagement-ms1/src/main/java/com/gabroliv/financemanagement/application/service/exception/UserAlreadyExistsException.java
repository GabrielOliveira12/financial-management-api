package com.gabroliv.financemanagement.application.service.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String field, String value) {
        super("User already exists with " + field + ": " + value);
    }
}
