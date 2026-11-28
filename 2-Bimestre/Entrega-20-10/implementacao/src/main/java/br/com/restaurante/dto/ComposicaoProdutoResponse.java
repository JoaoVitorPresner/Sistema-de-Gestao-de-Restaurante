package br.com.restaurante.dto;

import br.com.restaurante.model.ComposicaoProduto;
import br.com.restaurante.model.UnidadeMedida;

import java.math.BigDecimal;

public record ComposicaoProdutoResponse(

    Long id,

    Long produtoId,

    String nomeProduto,

    Long ingredienteId,

    String nomeIngrediente,

    UnidadeMedida unidadeMedida,

    BigDecimal quantidade,

    BigDecimal estoqueDisponivel,

    boolean ingredienteAtivo,

    boolean suficiente

) {


    public static ComposicaoProdutoResponse de(
        ComposicaoProduto composicao
    ) {

        BigDecimal disponivel =
            composicao.ingrediente.getEstoqueDisponivel();


        boolean suficiente =

            composicao.ingrediente.ativo &&

            disponivel.compareTo(
                composicao.quantidade
            ) >= 0;


        return new ComposicaoProdutoResponse(

            composicao.id,

            composicao.produto.id,

            composicao.produto.nome,

            composicao.ingrediente.id,

            composicao.ingrediente.nome,

            composicao.ingrediente.unidadeMedida,

            composicao.quantidade,

            disponivel,

            composicao.ingrediente.ativo,

            suficiente
        );
    }
}