package br.com.restaurante.dto;

import br.com.restaurante.model.Ingrediente;
import br.com.restaurante.model.UnidadeMedida;

import java.math.BigDecimal;

public record IngredienteResponse(

    Long id,

    String nome,

    String descricao,

    UnidadeMedida unidadeMedida,

    BigDecimal estoqueFisico,

    BigDecimal estoqueReservado,

    BigDecimal estoqueDisponivel,

    BigDecimal estoqueMinimo,

    boolean estoqueBaixo,

    boolean ativo

) {


    public static IngredienteResponse de(
        Ingrediente ingrediente
    ) {

        return new IngredienteResponse(

            ingrediente.id,

            ingrediente.nome,

            ingrediente.descricao,

            ingrediente.unidadeMedida,

            ingrediente.estoqueFisico,

            ingrediente.estoqueReservado,

            ingrediente.getEstoqueDisponivel(),

            ingrediente.estoqueMinimo,

            ingrediente.isEstoqueBaixo(),

            ingrediente.ativo
        );
    }
}