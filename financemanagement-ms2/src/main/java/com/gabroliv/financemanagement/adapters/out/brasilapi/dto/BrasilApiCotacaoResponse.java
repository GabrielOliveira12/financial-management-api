package com.gabroliv.financemanagement.adapters.out.brasilapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record BrasilApiCotacaoResponse(
        @JsonProperty("moeda") String currency,
        @JsonProperty("data") String date,
        @JsonProperty("cotacoes") List<Rate> rates
) {
    public record Rate(
            @JsonProperty("cotacao_venda") BigDecimal sellRate,
            @JsonProperty("cotacao_compra") BigDecimal buyRate,
            @JsonProperty("tipo_boletim") String bulletinType,
            @JsonProperty("data_hora_cotacao") String quotedAt
    ) {}
}
