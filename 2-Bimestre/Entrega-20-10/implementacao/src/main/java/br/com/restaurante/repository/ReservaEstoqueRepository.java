package br.com.restaurante.repository;

import br.com.restaurante.model.ReservaEstoque;
import br.com.restaurante.model.StatusReserva;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaEstoqueRepository
        extends JpaRepository<ReservaEstoque, Long> {


    List<ReservaEstoque>
        findByPedido_IdOrderByIngrediente_IdAsc(
            Long pedidoId
        );


    List<ReservaEstoque>
        findByPedido_IdAndStatusOrderByIngrediente_IdAsc(
            Long pedidoId,
            StatusReserva status
        );


    List<ReservaEstoque>
        findByIngrediente_IdAndStatusOrderByDataReservaAsc(
            Long ingredienteId,
            StatusReserva status
        );


    boolean existsByIngrediente_IdAndStatus(
        Long ingredienteId,
        StatusReserva status
    );


    boolean existsByPedido_IdAndStatus(
        Long pedidoId,
        StatusReserva status
    );
}