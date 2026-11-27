
package br.com.restaurante.dto;

import jakarta.validation.constraints.*;

public record ItemPedidoRequest(

    @NotNull
    @Positive
    Long produtoId,

    @NotNull
    @Min(1)
    @Max(99)
    Integer quantidade

) {
}
