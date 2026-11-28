package br.com.restaurante.dto;

import br.com.restaurante.model.Conta;
import br.com.restaurante.model.StatusConta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ContaResponse(

    Long id,

    Long mesaId,

    Integer numeroMesa,

    BigDecimal subtotal,

    BigDecimal percentualDesconto,

    BigDecimal valorDesconto,

    String motivoDesconto,

    BigDecimal valorTotal,

    BigDecimal valorPago,

    BigDecimal valorEstornado,

    BigDecimal valorLiquido,

    StatusConta status,

    LocalDateTime dataFinalizacao,

    String nomeResponsavel,

    String motivoEstorno,

    LocalDateTime dataEstorno,

    String nomeResponsavelEstorno,

    List<PagamentoResponse> pagamentos

) {

    public static ContaResponse de(
            Conta conta) {

        boolean estornada =
            conta.status == StatusConta.ESTORNADA;

        BigDecimal valorEstornado =
            estornada
                ? conta.valorPago
                : BigDecimal.ZERO;

        BigDecimal valorLiquido =
            estornada
                ? BigDecimal.ZERO
                : conta.valorPago;

        return new ContaResponse(

            conta.id,

            conta.mesa.id,

            conta.numeroMesa,

            conta.subtotal,

            conta.percentualDesconto,

            conta.valorDesconto,

            conta.motivoDesconto,

            conta.valorTotal,

            conta.valorPago,

            valorEstornado,

            valorLiquido,

            conta.status,

            conta.dataFinalizacao,

            conta.nomeResponsavel,

            conta.motivoEstorno,

            conta.dataEstorno,

            conta.nomeResponsavelEstorno,

            conta.pagamentos.stream()
                .map(PagamentoResponse::de)
                .toList()

        );
    }

}