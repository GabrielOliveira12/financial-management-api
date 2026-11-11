package com.gabroliv.financemanagement.application.ports.out;

public interface AccountGatewayPort {
    AccountLimits getAccountLimits(Long userId);
}
