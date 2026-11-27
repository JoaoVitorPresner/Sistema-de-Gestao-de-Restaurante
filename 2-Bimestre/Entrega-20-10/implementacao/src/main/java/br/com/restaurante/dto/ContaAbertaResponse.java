package br.com.restaurante.dto;

import br.com.restaurante.model.StatusMesa;

import java.math.BigDecimal;
import java.util.List;

public record ContaAbertaResponse(

    Long mesaId,

    Integer numeroMesa,

    String codigoAtendimento,

    StatusMesa statusMesa,

    long totalPedidos,

    long pedidosPendentes,

    long pedidosEntregues,

    long pedidosCancelados,

    BigDecimal valorOriginalPedidos,

    BigDecimal valorCancelado,

    BigDecimal subtotal,

    boolean podeFinalizar,

    String motivoBloqueio,

    List<PedidoContaResponse> pedidos

) {
}