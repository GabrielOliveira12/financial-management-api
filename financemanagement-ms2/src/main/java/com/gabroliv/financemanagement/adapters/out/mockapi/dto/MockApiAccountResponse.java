package com.gabroliv.financemanagement.adapters.out.mockapi.dto;

import java.math.BigDecimal;

public record MockApiAccountResponse(
        String id,
        Long userId,
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal transactionLimit
) {
}
