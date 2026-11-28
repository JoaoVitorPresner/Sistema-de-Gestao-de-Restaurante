
package br.com.restaurante.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record PedidoRequest(

    @Size(max = 500)
    String observacoes,

    @NotEmpty
    @Size(max = 100)
    List<@NotNull @Valid ItemPedidoRequest> itens

) {
}
