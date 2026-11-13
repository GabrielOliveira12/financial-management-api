package com.gabroliv.financemanagement.adapters.in.web.user.dto;

import java.util.Map;

public record CsvImportResponse(int createdUsers, int failedUsers, Map<String, String> errors) {
}
