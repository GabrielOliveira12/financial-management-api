package com.gabroliv.financemanagement.adapters.out.brasilapi.gateway;

import com.gabroliv.financemanagement.adapters.out.brasilapi.dto.BrasilApiCotacaoResponse;
import com.gabroliv.financemanagement.application.ports.out.CurrencyConversionPort;
import com.gabroliv.financemanagement.application.ports.out.CurrencyRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class BrasilApiCurrencyGateway implements CurrencyConversionPort {

    private static final Logger log = LoggerFactory.getLogger(BrasilApiCurrencyGateway.class);
    private static final String BRASIL_API_URL = "https://brasilapi.com.br/api/cambio/v1/cotacao";
    private static final String FECHAMENTO_PTAX = "FECHAMENTO PTAX";

    private final RestClient restClient;

    public BrasilApiCurrencyGateway(HttpClient trustAllHttpClient) {
        this.restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(trustAllHttpClient))
                .build();
    }

    @Override
    public Optional<CurrencyRate> getRate(String currency, LocalDate date) {
        for (int daysBack = 0; daysBack <= 3; daysBack++) {
            LocalDate targetDate = date.minusDays(daysBack);
            try {
                BrasilApiCotacaoResponse response = restClient.get()
                        .uri(BRASIL_API_URL + "/{currency}/{date}", currency, targetDate)
                        .retrieve()
                        .body(BrasilApiCotacaoResponse.class);

                if (response == null || response.rates() == null || response.rates().isEmpty()) {
                    continue;
                }

                List<BrasilApiCotacaoResponse.Rate> rates = response.rates();

                BrasilApiCotacaoResponse.Rate rate = rates.stream()
                        .filter(r -> FECHAMENTO_PTAX.equals(r.bulletinType()))
                        .findFirst()
                        .orElse(rates.getLast());

                return Optional.of(new CurrencyRate(currency, rate.sellRate(), targetDate));

            } catch (RestClientException exception) {
                log.debug("BrasilAPI has no rate for {}/{}, trying the previous day.", currency, targetDate);
            }
        }

        log.warn("BrasilAPI has no rate available for {} in the last 3 business days from {}.", currency, date);
        return Optional.empty();
    }
}
