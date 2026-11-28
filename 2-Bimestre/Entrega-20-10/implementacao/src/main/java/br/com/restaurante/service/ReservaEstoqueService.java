package br.com.restaurante.service;

import br.com.restaurante.model.*;
import br.com.restaurante.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReservaEstoqueService {

    private final IngredienteRepository ingredientes;

    private final ComposicaoProdutoRepository composicoes;

    private final ReservaEstoqueRepository reservas;

    private final MovimentacaoEstoqueRepository movimentacoes;

    private final EntityManager entityManager;


    public ReservaEstoqueService(

        IngredienteRepository ingredientes,

        ComposicaoProdutoRepository composicoes,

        ReservaEstoqueRepository reservas,

        MovimentacaoEstoqueRepository movimentacoes,

        EntityManager entityManager

    ) {

        this.ingredientes = ingredientes;

        this.composicoes = composicoes;

        this.reservas = reservas;

        this.movimentacoes = movimentacoes;

        this.entityManager = entityManager;
    }


    /* =========================================
       RESERVA DE INGREDIENTES - RN09 E RN10
       ========================================= */

    @Transactional(propagation = Propagation.MANDATORY)
    public void reservarIngredientes(Pedido pedido) {

        validarPedidoPersistido(pedido);

        if (pedido.status != StatusPedido.RECEBIDO) {

            throw conflito(
                "Somente pedidos recebidos podem reservar ingredientes"
            );
        }


        if (
            !reservas
                .findByPedido_IdOrderByIngrediente_IdAsc(
                    pedido.id
                )
                .isEmpty()
        ) {

            throw conflito(
                "O pedido ja possui registros de reserva"
            );
        }


        Map<Long, BigDecimal> necessidades =
            calcularNecessidades(pedido);


        Map<Long, Ingrediente> ingredientesBloqueados =
            new TreeMap<>();


        /*
         * Primeiro, bloquear e validar todos
         * os ingredientes necessarios.
         */

        for (Map.Entry<Long, BigDecimal> registro
                : necessidades.entrySet()) {

            Ingrediente ingrediente =
                buscarParaAtualizacao(
                    registro.getKey()
                );

            BigDecimal quantidade =
                registro.getValue();


            validarIntegridade(ingrediente);


            if (!ingrediente.ativo) {

                throw conflito(
                    "Ingrediente inativo: " +
                    ingrediente.nome
                );
            }


            if (
                ingrediente.getEstoqueDisponivel()
                    .compareTo(quantidade) < 0
            ) {

                throw conflito(
                    "Estoque insuficiente para o ingrediente: " +
                    ingrediente.nome
                );
            }


            ingredientesBloqueados.put(
                ingrediente.id,
                ingrediente
            );
        }


        /*
         * Somente depois de validar todos
         * os ingredientes, realizar as reservas.
         */

        for (Map.Entry<Long, BigDecimal> registro
                : necessidades.entrySet()) {

            Ingrediente ingrediente =
                ingredientesBloqueados.get(
                    registro.getKey()
                );

            BigDecimal quantidade =
                registro.getValue();


            BigDecimal fisicoAnterior =
                ingrediente.estoqueFisico;

            BigDecimal reservadoAnterior =
                ingrediente.estoqueReservado;


            ingrediente.estoqueReservado =
                ingrediente.estoqueReservado.add(
                    quantidade
                );


            validarIntegridade(ingrediente);

            ingredientes.save(ingrediente);


            ReservaEstoque reserva =
                new ReservaEstoque();

            reserva.pedido = pedido;

            reserva.ingrediente = ingrediente;

            reserva.quantidade = quantidade;

            reserva.status = StatusReserva.RESERVADA;

            reserva.dataReserva = LocalDateTime.now();


            reservas.save(reserva);


            registrarMovimentacao(

                ingrediente,

                pedido,

                null,

                TipoMovimentacao.RESERVA,

                quantidade,

                fisicoAnterior,

                reservadoAnterior,

                "Reserva automatica na confirmacao do pedido"
            );
        }


        reservas.flush();
    }


    /* =========================================
       CONSUMO DE INGREDIENTES - RN11
       ========================================= */

    @Transactional(propagation = Propagation.MANDATORY)
    public void consumirReservas(

        Pedido pedido,

        Usuario responsavel

    ) {

        validarPedidoPersistido(pedido);


        if (pedido.status != StatusPedido.EM_PREPARO) {

            throw conflito(
                "O pedido precisa estar em preparo para consumir reservas"
            );
        }


        finalizarReservas(

            pedido,

            responsavel,

            StatusReserva.CONSUMIDA,

            TipoMovimentacao.CONSUMO,

            true,

            "Baixa automatica de ingredientes do pedido pronto"
        );
    }


    /* =========================================
       LIBERACAO DE INGREDIENTES - RN12
       ========================================= */

    @Transactional(propagation = Propagation.MANDATORY)
    public void liberarReservas(

        Pedido pedido,

        Usuario responsavel

    ) {

        validarPedidoPersistido(pedido);


        if (pedido.status != StatusPedido.RECEBIDO) {

            throw conflito(
                "Somente pedidos que nao iniciaram o preparo podem liberar reservas"
            );
        }


        finalizarReservas(

            pedido,

            responsavel,

            StatusReserva.LIBERADA,

            TipoMovimentacao.LIBERACAO,

            false,

            "Liberacao de ingredientes por cancelamento antes do preparo"
        );
    }


    /* =========================================
       REGISTRO DE PERDAS DURANTE O PREPARO
       ========================================= */

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarPerdas(

        Pedido pedido,

        Usuario responsavel

    ) {

        validarPedidoPersistido(pedido);


        if (pedido.status != StatusPedido.EM_PREPARO) {

            throw conflito(
                "Somente pedidos em preparo podem registrar perdas de reservas"
            );
        }


        finalizarReservas(

            pedido,

            responsavel,

            StatusReserva.PERDIDA,

            TipoMovimentacao.PERDA,

            true,

            "Perda de ingredientes por cancelamento durante o preparo"
        );
    }


    /* =========================================
       CALCULO DAS NECESSIDADES DO PEDIDO
       ========================================= */

    private Map<Long, BigDecimal> calcularNecessidades(
        Pedido pedido
    ) {

        Map<Long, BigDecimal> necessidades =
            new TreeMap<>();


        for (ItemPedido item : pedido.itens) {

            List<ComposicaoProduto> receita =
                composicoes
                    .findByProduto_IdOrderByIngrediente_IdAsc(
                        item.produto.id
                    );


            if (receita.isEmpty()) {

                if (item.produto.controlaEstoque) {

                    throw conflito(
                        "Produto sem composicao cadastrada: " +
                        item.nomeProduto
                    );
                }

                continue;
            }


            for (ComposicaoProduto composicao : receita) {

                BigDecimal quantidadeNecessaria =
                    composicao.quantidade.multiply(

                        BigDecimal.valueOf(
                            item.quantidade
                        )
                    );


                necessidades.merge(

                    composicao.ingrediente.id,

                    quantidadeNecessaria,

                    BigDecimal::add
                );
            }
        }


        return necessidades;
    }


    /* =========================================
       FINALIZACAO DAS RESERVAS
       ========================================= */

    private void finalizarReservas(

        Pedido pedido,

        Usuario responsavel,

        StatusReserva novoStatus,

        TipoMovimentacao tipo,

        boolean baixarEstoqueFisico,

        String motivo

    ) {

        List<ReservaEstoque> lista =
            reservas
                .findByPedido_IdAndStatusOrderByIngrediente_IdAsc(

                    pedido.id,

                    StatusReserva.RESERVADA
                );


        /*
         * Pedidos antigos ou produtos sem controle
         * de estoque podem nao possuir reservas.
         */

        for (ReservaEstoque reserva : lista) {

            Ingrediente ingrediente =
                buscarParaAtualizacao(
                    reserva.ingrediente.id
                );


            validarIntegridade(ingrediente);


            BigDecimal quantidade =
                reserva.quantidade;


            if (
                ingrediente.estoqueReservado
                    .compareTo(quantidade) < 0
            ) {

                throw conflito(
                    "Saldo reservado inconsistente: " +
                    ingrediente.nome
                );
            }


            BigDecimal fisicoAnterior =
                ingrediente.estoqueFisico;

            BigDecimal reservadoAnterior =
                ingrediente.estoqueReservado;


            if (baixarEstoqueFisico) {

                if (
                    ingrediente.estoqueFisico
                        .compareTo(quantidade) < 0
                ) {

                    throw conflito(
                        "Saldo fisico insuficiente: " +
                        ingrediente.nome
                    );
                }


                ingrediente.estoqueFisico =
                    ingrediente.estoqueFisico.subtract(
                        quantidade
                    );
            }


            ingrediente.estoqueReservado =
                ingrediente.estoqueReservado.subtract(
                    quantidade
                );


            validarIntegridade(ingrediente);

            ingredientes.save(ingrediente);


            reserva.status = novoStatus;

            reserva.dataFinalizacao =
                LocalDateTime.now();


            reservas.save(reserva);


            registrarMovimentacao(

                ingrediente,

                pedido,

                responsavel,

                tipo,

                quantidade,

                fisicoAnterior,

                reservadoAnterior,

                motivo
            );
        }


        reservas.flush();
    }


    /* =========================================
       HISTORICO DE MOVIMENTACOES AUTOMATICAS
       ========================================= */

    private void registrarMovimentacao(

        Ingrediente ingrediente,

        Pedido pedido,

        Usuario responsavel,

        TipoMovimentacao tipo,

        BigDecimal quantidade,

        BigDecimal fisicoAnterior,

        BigDecimal reservadoAnterior,

        String motivo

    ) {

        MovimentacaoEstoque movimentacao =
            new MovimentacaoEstoque();


        movimentacao.ingrediente = ingrediente;

        movimentacao.pedido = pedido;

        movimentacao.responsavel = responsavel;

        movimentacao.tipo = tipo;

        movimentacao.quantidade = quantidade;

        movimentacao.fisicoAnterior =
            fisicoAnterior;

        movimentacao.fisicoPosterior =
            ingrediente.estoqueFisico;

        movimentacao.reservadoAnterior =
            reservadoAnterior;

        movimentacao.reservadoPosterior =
            ingrediente.estoqueReservado;

        movimentacao.motivo = motivo;

        movimentacao.dataHora = LocalDateTime.now();


        movimentacoes.save(movimentacao);
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Ingrediente buscarParaAtualizacao(
        Long id
    ) {

        Ingrediente ingrediente =
            ingredientes.buscarParaAtualizacao(id)

                .orElseThrow(() ->

                    new ResponseStatusException(

                        HttpStatus.NOT_FOUND,

                        "Ingrediente nao encontrado: " + id
                    )
                );


        /*
         * Atualizar os dados da entidade apos
         * adquirir o bloqueio pessimista.
         */

        entityManager.refresh(

            ingrediente,

            LockModeType.PESSIMISTIC_WRITE
        );


        return ingrediente;
    }


    private void validarPedidoPersistido(
        Pedido pedido
    ) {

        if (pedido == null || pedido.id == null) {

            throw conflito(
                "O pedido precisa estar persistido"
            );
        }
    }


    private void validarIntegridade(
        Ingrediente ingrediente
    ) {

        if (!ingrediente.isSaldoValido()) {

            throw conflito(
                "Saldo de estoque inconsistente: " +
                ingrediente.nome
            );
        }
    }


    private ResponseStatusException conflito(
        String mensagem
    ) {

        return new ResponseStatusException(

            HttpStatus.CONFLICT,

            mensagem
        );
    }
}