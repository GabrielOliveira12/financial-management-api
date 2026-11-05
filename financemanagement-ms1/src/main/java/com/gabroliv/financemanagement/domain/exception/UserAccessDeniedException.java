package com.gabroliv.financemanagement.domain.exception;

public class UserAccessDeniedException extends RuntimeException {

    public UserAccessDeniedException() {
        super("You do not have permission to access this user.");
    }
}
