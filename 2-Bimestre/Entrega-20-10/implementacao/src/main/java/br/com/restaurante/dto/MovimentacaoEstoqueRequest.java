package br.com.restaurante.dto;

import br.com.restaurante.model.TipoMovimentacao;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MovimentacaoEstoqueRequest(

    @NotNull
    @Positive
    Long ingredienteId,


    @NotNull(
        message = "O tipo de movimentacao e obrigatorio"
    )
    TipoMovimentacao tipo,


    @NotNull
    @DecimalMin(
        value = "0.001",
        message = "A quantidade deve ser maior que zero"
    )
    @Digits(integer = 12, fraction = 3)
    BigDecimal quantidade,


    @NotBlank(
        message = "O motivo da movimentacao e obrigatorio"
    )
    @Size(max = 255)
    String motivo

) {


    @AssertTrue(
        message = "Somente entradas e saidas manuais sao permitidas"
    )
    public boolean isTipoManualValido() {

        return tipo == TipoMovimentacao.ENTRADA ||

               tipo == TipoMovimentacao.SAIDA_MANUAL;
    }
}