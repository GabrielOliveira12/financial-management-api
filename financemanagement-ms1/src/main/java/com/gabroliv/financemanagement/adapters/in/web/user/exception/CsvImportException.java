package com.gabroliv.financemanagement.adapters.in.web.user.exception;

import java.util.Map;

public class CsvImportException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public CsvImportException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
