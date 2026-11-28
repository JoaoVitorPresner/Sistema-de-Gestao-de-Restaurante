package br.com.restaurante.dto;

import jakarta.validation.constraints.*;

public record EstornoContaRequest(

    @NotBlank(
        message = "O motivo do estorno e obrigatorio"
    )
    @Size(
        max = 255,
        message = "O motivo deve possuir ate 255 caracteres"
    )
    String motivo

) {
}