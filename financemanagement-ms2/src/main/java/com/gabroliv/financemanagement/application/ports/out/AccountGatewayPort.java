package com.gabroliv.financemanagement.application.ports.out;

import java.math.BigDecimal;

public interface AccountGatewayPort {

    AccountLimits getAccountLimits(Long userId);

    void updateBalance(Long userId, BigDecimal newBalance);
}
