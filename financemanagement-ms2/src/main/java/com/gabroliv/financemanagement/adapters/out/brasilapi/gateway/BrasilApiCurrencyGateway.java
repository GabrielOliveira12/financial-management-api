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

                if (response == null || response.cotacoes() == null || response.cotacoes().isEmpty()) {
                    continue;
                }

                List<BrasilApiCotacaoResponse.Cotacao> cotacoes = response.cotacoes();

                BrasilApiCotacaoResponse.Cotacao cotacao = cotacoes.stream()
                        .filter(c -> FECHAMENTO_PTAX.equals(c.tipoBoletim()))
                        .findFirst()
                        .orElse(cotacoes.getLast());

                return Optional.of(new CurrencyRate(currency, cotacao.cotacaoVenda(), targetDate));

            } catch (RestClientException exception) {
                log.debug("BrasilAPI sem cotação para {}/{}, tentando dia anterior.", currency, targetDate);
            }
        }

        log.warn("BrasilAPI sem cotação disponível para {} nos últimos 3 dias úteis a partir de {}.", currency, date);
        return Optional.empty();
    }
}
