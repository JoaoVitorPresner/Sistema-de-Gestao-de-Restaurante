package br.com.restaurante.dto;

import java.math.BigDecimal;

public record ProdutoCardapioDTO(

    Long id,

    String nome,

    String descricao,

    BigDecimal preco,

    boolean disponivel

) {
}