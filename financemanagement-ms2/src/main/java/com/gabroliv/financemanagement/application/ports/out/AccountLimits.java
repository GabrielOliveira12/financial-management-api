package com.gabroliv.financemanagement.application.ports.out;

import java.math.BigDecimal;

public record AccountLimits(Long userId, BigDecimal balance, BigDecimal dailyLimit, BigDecimal transactionLimit) { }
