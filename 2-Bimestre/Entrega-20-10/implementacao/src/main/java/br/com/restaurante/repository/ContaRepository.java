package br.com.restaurante.repository;

import br.com.restaurante.model.Conta;
import br.com.restaurante.model.StatusConta;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ContaRepository
        extends JpaRepository<Conta, Long> {

    List<Conta>
        findAllByOrderByDataFinalizacaoDescIdDesc();

    List<Conta>
        findByStatusOrderByDataFinalizacaoDescIdDesc(
            StatusConta status
        );

    List<Conta>
        findByMesa_IdOrderByDataFinalizacaoDescIdDesc(
            Long mesaId
        );

    Optional<Conta>
        findByMesa_IdAndCodigoAtendimento(
            Long mesaId,
            String codigoAtendimento
        );

    boolean
        existsByMesa_IdAndCodigoAtendimento(
            Long mesaId,
            String codigoAtendimento
        );

    List<Conta>
        findByDataFinalizacaoGreaterThanEqualAndDataFinalizacaoLessThanOrderByDataFinalizacaoDescIdDesc(
            LocalDateTime inicio,
            LocalDateTime fim
        );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c
        FROM Conta c
        WHERE c.id = :id
        """)
    Optional<Conta> buscarParaAtualizacao(
        @Param("id") Long id
    );

}