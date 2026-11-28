package br.com.restaurante.dto;

import br.com.restaurante.model.MovimentacaoEstoque;
import br.com.restaurante.model.TipoMovimentacao;
import br.com.restaurante.model.UnidadeMedida;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoEstoqueResponse(

    Long id,

    Long ingredienteId,

    String nomeIngrediente,

    UnidadeMedida unidadeMedida,

    Long pedidoId,

    String responsavel,

    TipoMovimentacao tipo,

    BigDecimal quantidade,

    BigDecimal fisicoAnterior,

    BigDecimal fisicoPosterior,

    BigDecimal reservadoAnterior,

    BigDecimal reservadoPosterior,

    String motivo,

    LocalDateTime dataHora

) {


    public static MovimentacaoEstoqueResponse de(
        MovimentacaoEstoque movimentacao
    ) {

        return new MovimentacaoEstoqueResponse(

            movimentacao.id,

            movimentacao.ingrediente.id,

            movimentacao.ingrediente.nome,

            movimentacao.ingrediente.unidadeMedida,

            movimentacao.pedido != null
                ? movimentacao.pedido.id
                : null,

            movimentacao.responsavel != null
                ? movimentacao.responsavel.nome
                : "Sistema",

            movimentacao.tipo,

            movimentacao.quantidade,

            movimentacao.fisicoAnterior,

            movimentacao.fisicoPosterior,

            movimentacao.reservadoAnterior,

            movimentacao.reservadoPosterior,

            movimentacao.motivo,

            movimentacao.dataHora
        );
    }
}