package com.gabroliv.financemanagement.adapters.out.mockapi.gateway;

import com.gabroliv.financemanagement.adapters.out.mockapi.dto.MockApiAccountResponse;
import com.gabroliv.financemanagement.adapters.out.mockapi.exception.AccountGatewayException;
import com.gabroliv.financemanagement.adapters.out.mockapi.exception.AccountNotFoundException;
import com.gabroliv.financemanagement.application.ports.out.AccountGatewayPort;
import com.gabroliv.financemanagement.application.ports.out.AccountLimits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
public class MockApiAccountGateway implements AccountGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(MockApiAccountGateway.class);

    private final RestClient restClient;
    private final String accountsUrl;

    public MockApiAccountGateway(
            RestClient.Builder restClientBuilder,
            @Value("${mock-api.accounts-url}") String accountsUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.accountsUrl = accountsUrl;
    }

    @Override
    public AccountLimits getAccountLimits(Long userId) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(accountsUrl)
                    .queryParam("userId", userId)
                    .build()
                    .toUri();

            log.info("Consultando MockAPI: {}", uri);

            List<MockApiAccountResponse> accounts = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (accounts == null || accounts.isEmpty()) {
                throw new AccountNotFoundException(userId);
            }

            MockApiAccountResponse account = accounts.getFirst();

            return new AccountLimits(
                    account.userId(),
                    account.balance(),
                    account.dailyLimit(),
                    account.transactionLimit()
            );
        } catch (AccountNotFoundException exception) {
            throw exception;
        } catch (RestClientException | IllegalArgumentException exception) {
            log.error(
                    "Erro ao consultar a MockAPI. URL base: {}, userId: {}",
                    accountsUrl,
                    userId,
                    exception
            );

            throw new AccountGatewayException(exception);
        }
    }
}
