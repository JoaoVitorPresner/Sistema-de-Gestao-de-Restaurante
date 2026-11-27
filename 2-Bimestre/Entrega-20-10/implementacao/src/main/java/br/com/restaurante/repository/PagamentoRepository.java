package br.com.restaurante.repository;

import br.com.restaurante.model.Pagamento;
import br.com.restaurante.model.FormaPagamento;
import br.com.restaurante.model.StatusConta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PagamentoRepository
        extends JpaRepository<Pagamento, Long> {

    List<Pagamento>
        findByConta_IdOrderByIdAsc(
            Long contaId
        );

    List<Pagamento>
        findByFormaPagamentoOrderByDataHoraDescIdDesc(
            FormaPagamento formaPagamento
        );

    List<Pagamento>
        findByConta_StatusOrderByDataHoraDescIdDesc(
            StatusConta status
        );

    long countByConta_Id(
        Long contaId
    );

    @Query("""
        SELECT SUM(p.valor)
        FROM Pagamento p
        WHERE p.conta.id = :contaId
        """)
    BigDecimal somarPagamentosDaConta(
        @Param("contaId") Long contaId
    );

}