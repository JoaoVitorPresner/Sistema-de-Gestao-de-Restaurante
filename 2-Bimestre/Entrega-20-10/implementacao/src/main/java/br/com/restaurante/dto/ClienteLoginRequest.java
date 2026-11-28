package br.com.restaurante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ClienteLoginRequest(

        @NotNull
        @Min(1)
        Integer numeroMesa

) {
}