
package br.com.restaurante;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProdutoRepository
        extends JpaRepository<Produto, Long> {

    List<Produto> findAllByOrderByNomeAsc();

    boolean existsByCategoria_IdAndAtivoTrue(
        Long categoriaId
    );

    List<Produto>
        findByAtivoTrueAndCategoria_AtivoTrueOrderByNomeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
    SELECT p
    FROM Produto p
    WHERE p.id = :id
    """)
Optional<Produto> buscarParaAtualizacao(
    @Param("id") Long id
);

}
