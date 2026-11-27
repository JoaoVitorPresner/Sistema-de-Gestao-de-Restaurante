package br.com.restaurante.dto;

import br.com.restaurante.model.FormaPagamento;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PagamentoRequest(

    @NotNull(
        message = "A forma de pagamento e obrigatoria"
    )
    FormaPagamento formaPagamento,

    @NotNull(
        message = "O valor do pagamento e obrigatorio"
    )
    @DecimalMin(
        value = "0.01",
        message = "O pagamento deve ser maior que zero"
    )
    @Digits(
        integer = 12,
        fraction = 2,
        message = "Valor de pagamento invalido"
    )
    BigDecimal valor

) {
}