package br.com.restaurante.dto;

import jakarta.validation.constraints.NotNull;

public record ControleEstoqueRequest(

    @NotNull(
        message = "Informe se o produto controla estoque"
    )
    Boolean controlaEstoque

) {
}