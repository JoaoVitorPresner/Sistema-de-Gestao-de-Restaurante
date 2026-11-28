
package br.com.restaurante.repository;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.StatusMesa;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MesaRepository
        extends JpaRepository<Mesa, Long> {

    Optional<Mesa> findByNumero(Integer numero);

    boolean existsByNumero(Integer numero);

    List<Mesa> findAllByOrderByNumeroAsc();

    List<Mesa> findByStatusOrderByNumeroAsc(
            StatusMesa status);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mesa m WHERE m.id = :id")
    Optional<Mesa> buscarParaAtualizacao(
            @Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mesa m WHERE m.numero = :numero")
    Optional<Mesa> buscarPorNumeroParaAtualizacao(
        @Param("numero") Integer numero
    );
}
