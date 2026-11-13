package com.gabroliv.financemanagement.adapters.in.web.account.dto;

import java.math.BigDecimal;

public record AccountBalanceResponse(
        Long userId,
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal transactionLimit
) {}
