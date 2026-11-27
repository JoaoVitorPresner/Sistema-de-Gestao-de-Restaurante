package br.com.restaurante.repository;

import br.com.restaurante.model.Ingrediente;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredienteRepository
        extends JpaRepository<Ingrediente, Long> {


    List<Ingrediente> findAllByOrderByNomeAsc();


    List<Ingrediente> findByAtivoTrueOrderByNomeAsc();


    boolean existsByNomeIgnoreCase(String nome);


    boolean existsByNomeIgnoreCaseAndIdNot(
        String nome,
        Long id
    );


    /* =========================================
       BLOQUEIO PARA ATUALIZACAO DO ESTOQUE
       ========================================= */

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT i
        FROM Ingrediente i
        WHERE i.id = :id
        """)
    Optional<Ingrediente> buscarParaAtualizacao(
        @Param("id") Long id
    );
}