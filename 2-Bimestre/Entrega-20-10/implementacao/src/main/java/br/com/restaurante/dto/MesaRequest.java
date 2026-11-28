package br.com.restaurante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MesaRequest(

        @NotNull
        @Min(1)
        Integer numero,

        @NotNull
        @Min(1)
        Integer capacidade

) {
}