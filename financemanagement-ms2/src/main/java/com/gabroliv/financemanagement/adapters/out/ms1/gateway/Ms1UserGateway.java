package com.gabroliv.financemanagement.adapters.out.ms1.gateway;

import com.gabroliv.financemanagement.application.ports.out.UserGatewayPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class Ms1UserGateway implements UserGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(Ms1UserGateway.class);

    private final RestClient restClient;
    private final String ms1Url;

    public Ms1UserGateway(
            RestClient.Builder restClientBuilder,
            @Value("${ms1.url}") String ms1Url
    ) {
        this.restClient = restClientBuilder.build();
        this.ms1Url = ms1Url;
    }

    @Override
    public boolean existsById(Long userId) {
        try {
            Boolean exists = restClient.get()
                    .uri(ms1Url + "/users/{id}/exists", userId)
                    .retrieve()
                    .body(Boolean.class);

            return Boolean.TRUE.equals(exists);
        } catch (RestClientException exception) {
            log.error("Error while checking user {} with MS1.", userId, exception);
            return false;
        }
    }
}
