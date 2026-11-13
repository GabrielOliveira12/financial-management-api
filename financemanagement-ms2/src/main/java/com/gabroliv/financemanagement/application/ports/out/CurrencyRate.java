package com.gabroliv.financemanagement.application.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public record CurrencyRate(
        String currency,
        BigDecimal rate,
        LocalDate rateDate
) {
    public BigDecimal convert(BigDecimal amount) {
        return amount.divide(rate, 2, java.math.RoundingMode.HALF_UP);
    }
}
