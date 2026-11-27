package br.com.restaurante.dto;

import br.com.restaurante.model.UnidadeMedida;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record IngredienteRequest(

    @NotBlank(
        message = "O nome do ingrediente e obrigatorio"
    )
    @Size(max = 100)
    String nome,


    @Size(max = 255)
    String descricao,


    @NotNull(
        message = "A unidade de medida e obrigatoria"
    )
    UnidadeMedida unidadeMedida,


    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 12, fraction = 3)
    BigDecimal estoqueMinimo

) {
}