package br.com.restaurante.repository;

import br.com.restaurante.model.ComposicaoProduto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComposicaoProdutoRepository
        extends JpaRepository<ComposicaoProduto, Long> {


    List<ComposicaoProduto>
        findByProduto_IdOrderByIngrediente_IdAsc(
            Long produtoId
        );


    Optional<ComposicaoProduto>
        findByProduto_IdAndIngrediente_Id(
            Long produtoId,
            Long ingredienteId
        );


    boolean existsByProduto_IdAndIngrediente_Id(
        Long produtoId,
        Long ingredienteId
    );


    boolean existsByProduto_Id(
        Long produtoId
    );


    boolean existsByIngrediente_Id(
        Long ingredienteId
    );


    boolean existsByIngrediente_IdAndProduto_AtivoTrue(
        Long ingredienteId
    );


    List<ComposicaoProduto>
        findByIngrediente_IdOrderByProduto_NomeAsc(
            Long ingredienteId
        );
}