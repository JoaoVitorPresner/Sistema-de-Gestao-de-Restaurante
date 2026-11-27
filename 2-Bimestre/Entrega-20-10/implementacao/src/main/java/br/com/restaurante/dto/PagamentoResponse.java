package br.com.restaurante.dto;

import br.com.restaurante.model.FormaPagamento;
import br.com.restaurante.model.Pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponse(

    Long id,

    FormaPagamento formaPagamento,

    BigDecimal valor,

    LocalDateTime dataHora

) {

    public static PagamentoResponse de(
            Pagamento pagamento) {

        return new PagamentoResponse(

            pagamento.id,

            pagamento.formaPagamento,

            pagamento.valor,

            pagamento.dataHora

        );
    }

}