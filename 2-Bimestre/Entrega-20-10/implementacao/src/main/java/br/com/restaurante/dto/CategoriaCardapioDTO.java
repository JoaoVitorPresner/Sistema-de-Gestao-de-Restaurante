
package br.com.restaurante.dto;

import java.util.List;

public record CategoriaCardapioDTO(

    Long id,

    String nome,

    String descricao,

    List<ProdutoCardapioDTO> produtos

) {
}
