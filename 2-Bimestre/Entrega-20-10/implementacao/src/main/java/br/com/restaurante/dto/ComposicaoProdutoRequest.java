package br.com.restaurante.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ComposicaoProdutoRequest(

    @NotNull
    @Positive
    Long ingredienteId,


    @NotNull
    @DecimalMin(
        value = "0.001",
        message = "A quantidade deve ser maior que zero"
    )
    @Digits(integer = 9, fraction = 3)
    BigDecimal quantidade

) {
}