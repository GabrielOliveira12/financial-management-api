package com.gabroliv.financemanagement.application.ports.out;

import java.time.LocalDate;
import java.util.Optional;

public interface CurrencyConversionPort {

    Optional<CurrencyRate> getRate(String currency, LocalDate date);
}
