package com.gabroliv.financemanagement.application.ports.in;

import com.gabroliv.financemanagement.application.ports.out.AccountLimits;

public interface AccountUseCase {

    AccountLimits getBalance(Long userId);
}
