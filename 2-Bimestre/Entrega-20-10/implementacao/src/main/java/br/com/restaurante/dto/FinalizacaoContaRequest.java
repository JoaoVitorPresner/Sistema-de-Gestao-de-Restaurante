package br.com.restaurante.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record FinalizacaoContaRequest(

    @NotBlank(
        message = "O codigo do atendimento e obrigatorio"
    )
    @Size(max = 36)
    String codigoAtendimento,

    @NotNull(
        message = "O percentual de desconto e obrigatorio"
    )
    @DecimalMin(
        value = "0.00",
        message = "O desconto nao pode ser negativo"
    )
    @DecimalMax(
        value = "100.00",
        message = "O desconto nao pode superar 100%"
    )
    @Digits(
        integer = 3,
        fraction = 2
    )
    BigDecimal percentualDesconto,

    @Size(
        max = 255,
        message = "O motivo do desconto deve possuir ate 255 caracteres"
    )
    String motivoDesconto,

    @NotNull(
        message = "A lista de pagamentos e obrigatoria"
    )
    @Size(
        max = 20,
        message = "Limite de 20 pagamentos por conta"
    )
    List<@NotNull @Valid PagamentoRequest> pagamentos

) {

    @AssertTrue(
        message = "Descontos exigem uma justificativa"
    )
    public boolean isMotivoDescontoValido() {

        if (percentualDesconto == null) {
            return true;
        }

        if (
            percentualDesconto.compareTo(
                BigDecimal.ZERO
            ) <= 0
        ) {
            return true;
        }

        return motivoDesconto != null &&
               !motivoDesconto.isBlank();
    }

}