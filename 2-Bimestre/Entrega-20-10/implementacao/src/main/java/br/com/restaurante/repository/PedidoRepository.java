
package br.com.restaurante.repository;

import br.com.restaurante.model.Pedido;
import br.com.restaurante.model.StatusPedido;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PedidoRepository
        extends JpaRepository<Pedido, Long> {


    List<Pedido> findAllByOrderByDataHoraDesc();


    List<Pedido>
        findByMesa_IdAndCodigoAtendimentoOrderByDataHoraDesc(

            Long mesaId,

            String codigoAtendimento
        );


    Optional<Pedido>
        findByIdAndMesa_IdAndCodigoAtendimento(

            Long id,

            Long mesaId,

            String codigoAtendimento
        );


    List<Pedido>
        findByStatusInOrderByDataHoraAsc(

            Collection<StatusPedido> status
        );


    List<Pedido>
        findByMesa_IdAndCodigoAtendimentoAndStatusNot(

            Long mesaId,

            String codigoAtendimento,

            StatusPedido status
        );


    boolean
        existsByMesa_IdAndCodigoAtendimentoAndStatusIn(

            Long mesaId,

            String codigoAtendimento,

            Collection<StatusPedido> status
        );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT p
    FROM Pedido p
    WHERE p.id = :id
    """)
Optional<Pedido> buscarParaAtualizacao(
    @Param("id") Long id
);

    @Query("SELECT p.mesa.id FROM Pedido p WHERE p.id = :id")
    Optional<Long> buscarMesaIdPorPedido(
        @Param("id") Long id
    );
}
