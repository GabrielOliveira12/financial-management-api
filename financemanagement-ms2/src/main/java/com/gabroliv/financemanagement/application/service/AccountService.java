package com.gabroliv.financemanagement.application.service;

import com.gabroliv.financemanagement.application.ports.in.AccountUseCase;
import com.gabroliv.financemanagement.application.ports.out.AccountGatewayPort;
import com.gabroliv.financemanagement.application.ports.out.AccountLimits;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements AccountUseCase {

    private final AccountGatewayPort accountGatewayPort;

    public AccountService(AccountGatewayPort accountGatewayPort) {
        this.accountGatewayPort = accountGatewayPort;
    }

    @Override
    public AccountLimits getBalance(Long userId) {
        return accountGatewayPort.getAccountLimits(userId);
    }
}
