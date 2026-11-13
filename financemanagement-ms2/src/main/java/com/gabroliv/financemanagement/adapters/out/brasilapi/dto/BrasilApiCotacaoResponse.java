package com.gabroliv.financemanagement.adapters.out.brasilapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record BrasilApiCotacaoResponse(
        String moeda,
        String data,
        List<Cotacao> cotacoes
) {
    public record Cotacao(
            @JsonProperty("cotacao_venda") BigDecimal cotacaoVenda,
            @JsonProperty("cotacao_compra") BigDecimal cotacaoCompra,
            @JsonProperty("tipo_boletim") String tipoBoletim,
            @JsonProperty("data_hora_cotacao") String dataHoraCotacao
    ) {}
}
