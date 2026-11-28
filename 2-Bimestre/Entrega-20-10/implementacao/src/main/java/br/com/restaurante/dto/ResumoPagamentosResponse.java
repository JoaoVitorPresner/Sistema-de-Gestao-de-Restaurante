package br.com.restaurante.dto;

import java.math.BigDecimal;

public record ResumoPagamentosResponse(

    long mesasOcupadas,

    long mesasAguardandoPagamento,

    long contasFinalizadas,

    long contasEstornadas,

    BigDecimal valorBruto,

    BigDecimal valorDescontos,

    BigDecimal valorRecebido,

    BigDecimal valorEstornado,

    BigDecimal valorLiquido

) {
}