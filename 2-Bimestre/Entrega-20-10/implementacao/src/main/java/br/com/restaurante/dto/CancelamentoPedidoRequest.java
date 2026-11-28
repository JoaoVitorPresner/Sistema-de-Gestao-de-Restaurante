package br.com.restaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelamentoPedidoRequest(

    @NotBlank(
        message = "O motivo do cancelamento e obrigatorio"
    )
    @Size(
        max = 255,
        message = "O motivo deve possuir no maximo 255 caracteres"
    )
    String motivo

) {
}