
package br.com.restaurante.dto;

import br.com.restaurante.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(

    Long id,

    Long produtoId,

    String nomeProduto,

    Integer quantidade,

    BigDecimal precoUnitario,

    BigDecimal subtotal

) {

    public static ItemPedidoResponse de(
            ItemPedido item) {

        return new ItemPedidoResponse(

            item.id,

            item.produto.id,

            item.nomeProduto,

            item.quantidade,

            item.precoUnitario,

            item.subtotal
        );
    }
}
