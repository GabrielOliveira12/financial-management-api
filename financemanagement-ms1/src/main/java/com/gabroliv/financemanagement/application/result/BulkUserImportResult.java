package com.gabroliv.financemanagement.application.result;

import com.gabroliv.financemanagement.domain.model.User;

import java.util.List;

public record BulkUserImportResult(List<User> createdUsers, List<ImportError> errors) {

    public record ImportError(String identifier, String reason) {}
}
