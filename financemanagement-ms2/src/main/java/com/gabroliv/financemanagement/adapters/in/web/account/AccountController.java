package com.gabroliv.financemanagement.adapters.in.web.account;

import com.gabroliv.financemanagement.adapters.in.web.account.dto.AccountBalanceResponse;
import com.gabroliv.financemanagement.application.ports.in.AccountUseCase;
import com.gabroliv.financemanagement.application.ports.out.AccountLimits;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountUseCase accountUseCase;

    public AccountController(AccountUseCase accountUseCase) {
        this.accountUseCase = accountUseCase;
    }

    @GetMapping("/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(
            @AuthenticationPrincipal Long authenticatedUserId) {

        AccountLimits limits = accountUseCase.getBalance(authenticatedUserId);

        return ResponseEntity.ok(new AccountBalanceResponse(
                limits.userId(),
                limits.balance(),
                limits.dailyLimit(),
                limits.transactionLimit()
        ));
    }
}
