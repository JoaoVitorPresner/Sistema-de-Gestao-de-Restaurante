package br.com.restaurante.dto;

import br.com.restaurante.model.Pedido;
import br.com.restaurante.model.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(

    Long id,

    Long mesaId,

    Integer numeroMesa,

    LocalDateTime dataHora,

    StatusPedido status,

    String observacoes,

    BigDecimal valorTotal,

    List<ItemPedidoResponse> itens,

    String motivoCancelamento,

    LocalDateTime dataCancelamento,

    String canceladoPor

) {

    /* =========================================
       RESPOSTA PADRAO
       ========================================= */

    public static PedidoResponse de(Pedido pedido) {

        return de(pedido, false);
    }


    /* =========================================
       RESPOSTA ADMINISTRATIVA
       ========================================= */

    public static PedidoResponse deAdministrativo(
            Pedido pedido) {

        return de(pedido, true);
    }


    /* =========================================
       CONVERSAO DA ENTIDADE PARA DTO
       ========================================= */

    private static PedidoResponse de(

            Pedido pedido,

            boolean incluirResponsavel) {

        String responsavel = null;

        if (
            incluirResponsavel &&
            pedido.canceladoPor != null
        ) {

            responsavel = pedido.canceladoPor.nome;
        }

        return new PedidoResponse(

            pedido.id,

            pedido.mesa.id,

            pedido.mesa.numero,

            pedido.dataHora,

            pedido.status,

            pedido.observacoes,

            pedido.valorTotal,

            pedido.itens.stream()

                .map(ItemPedidoResponse::de)

                .toList(),

            pedido.motivoCancelamento,

            pedido.dataCancelamento,

            responsavel
        );
    }
}