package br.com.restaurante.dto;

import br.com.restaurante.model.Pedido;
import br.com.restaurante.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoContaResponse(

    Long id,

    LocalDateTime dataHora,

    StatusPedido status,

    BigDecimal valorOriginal,

    BigDecimal valorConsiderado,

    boolean finalizado,

    List<ItemPedidoResponse> itens

) {

    public static PedidoContaResponse de(
            Pedido pedido) {

        boolean cancelado =
            pedido.status == StatusPedido.CANCELADO;

        boolean finalizado =
            pedido.status == StatusPedido.ENTREGUE ||
            cancelado;

        BigDecimal valorConsiderado =
            cancelado
                ? BigDecimal.ZERO
                : pedido.valorTotal;

        return new PedidoContaResponse(

            pedido.id,

            pedido.dataHora,

            pedido.status,

            pedido.valorTotal,

            valorConsiderado,

            finalizado,

            pedido.itens.stream()
                .map(ItemPedidoResponse::de)
                .toList()

        );
    }

}