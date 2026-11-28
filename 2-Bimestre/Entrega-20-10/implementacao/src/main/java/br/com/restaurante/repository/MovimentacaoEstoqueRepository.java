package br.com.restaurante.repository;

import br.com.restaurante.model.MovimentacaoEstoque;
import br.com.restaurante.model.TipoMovimentacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoEstoqueRepository
        extends JpaRepository<MovimentacaoEstoque, Long> {


    List<MovimentacaoEstoque>
        findTop100ByOrderByDataHoraDescIdDesc();


    List<MovimentacaoEstoque>
        findTop100ByIngrediente_IdOrderByDataHoraDescIdDesc(
            Long ingredienteId
        );


    List<MovimentacaoEstoque>
        findTop100ByTipoOrderByDataHoraDescIdDesc(
            TipoMovimentacao tipo
        );


    List<MovimentacaoEstoque>
        findByPedido_IdOrderByDataHoraAscIdAsc(
            Long pedidoId
        );
}