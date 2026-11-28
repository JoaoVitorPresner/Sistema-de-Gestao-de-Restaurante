package br.com.restaurante;

import br.com.restaurante.dto.*;
import br.com.restaurante.model.*;
import br.com.restaurante.repository.*;
import br.com.restaurante.service.*;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@WithMockUser(
    username = "gerente_integracao_financeira",
    roles = "GERENTE"
)
class IntegracaoPagamentoPedidoTest {

    private static final String LOGIN =
        "gerente_integracao_financeira";

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    MesaRepository mesas;

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    ProdutoRepository produtos;

    @Autowired
    IngredienteRepository ingredientes;

    @Autowired
    PedidoRepository pedidos;

    @Autowired
    ReservaEstoqueRepository reservas;

    @Autowired
    MovimentacaoEstoqueRepository movimentacoes;

    @Autowired
    ContaRepository contas;

    @Autowired
    PagamentoRepository pagamentos;

    @Autowired
    PedidoService pedidoService;

    @Autowired
    MesaService mesaService;

    @Autowired
    PagamentoService pagamentoService;

    @Autowired
    ComposicaoProdutoService composicaoService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EntityManager entityManager;

    private Usuario gerente;

    private Categoria categoria;

    private Mesa mesa;

    private Produto produto;

    private Ingrediente ingrediente;

    private String codigoAtendimento;

    private Long pedidoId;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        gerente = new Usuario();

        gerente.nome = "Gerente integracao financeira";
        gerente.login = LOGIN;

        gerente.senha =
            passwordEncoder.encode("SenhaTeste123");

        gerente.perfil = Perfil.GERENTE;
        gerente.ativo = true;
        gerente.bloqueado = false;

        gerente = usuarios.saveAndFlush(gerente);


        categoria = new Categoria();

        categoria.nome = "Categoria integracao financeira";
        categoria.ativo = true;

        categoria = categorias.saveAndFlush(categoria);


        produto = new Produto();

        produto.nome = "Hamburguer integracao financeira";
        produto.preco = new BigDecimal("29.90");
        produto.categoria = categoria;
        produto.ativo = true;
        produto.controlaEstoque = false;

        produto = produtos.saveAndFlush(produto);


        ingrediente = new Ingrediente();

        ingrediente.nome = "Carne integracao financeira";

        ingrediente.unidadeMedida =
            UnidadeMedida.GRAMA;

        ingrediente.estoqueFisico =
            new BigDecimal("1000");

        ingrediente.estoqueReservado =
            BigDecimal.ZERO;

        ingrediente.estoqueMinimo =
            new BigDecimal("200");

        ingrediente.ativo = true;

        ingrediente =
            ingredientes.saveAndFlush(ingrediente);


        composicaoService.adicionarIngrediente(

            produto.id,

            new ComposicaoProdutoRequest(

                ingrediente.id,

                new BigDecimal("150")
            )
        );


        mesa = new Mesa();

        mesa.numero = 860;
        mesa.capacidade = 4;
        mesa.status = StatusMesa.OCUPADA;

        codigoAtendimento =
            UUID.randomUUID().toString();

        mesa.codigoAtendimento =
            codigoAtendimento;

        mesa = mesas.saveAndFlush(mesa);

        pedidoId = realizarPedido(
            codigoAtendimento,
            2
        );
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Long realizarPedido(
            String codigo,
            int quantidade) {

        return pedidoService.realizarPedido(

            mesa.id,

            codigo,

            new PedidoRequest(

                null,

                List.of(

                    new ItemPedidoRequest(
                        produto.id,
                        quantidade
                    )
                )
            )

        ).id();
    }


    private void entregarPedido(Long id) {

        pedidoService.iniciarPreparo(id);

        pedidoService.marcarPronto(id);

        pedidoService.registrarEntrega(id);
    }


    private void prepararFechamento() {

        entregarPedido(pedidoId);

        mesaService.solicitarPagamento(mesa.id);
    }


    private FinalizacaoContaRequest dadosPagamento(
            String codigo,
            String valor) {

        return new FinalizacaoContaRequest(

            codigo,

            BigDecimal.ZERO,

            null,

            List.of(

                new PagamentoRequest(

                    FormaPagamento.PIX,

                    new BigDecimal(valor)
                )
            )
        );
    }


    private ContaResponse finalizarConta() {

        return pagamentoService.finalizarConta(

            mesa.id,

            dadosPagamento(
                codigoAtendimento,
                "59.80"
            ),

            LOGIN
        );
    }


    private Mesa consultarMesa() {

        entityManager.flush();

        Mesa atual = mesas.findById(mesa.id)
            .orElseThrow();

        entityManager.refresh(atual);

        return atual;
    }


    private void verificarValor(
            String esperado,
            BigDecimal encontrado) {

        assertEquals(

            0,

            new BigDecimal(esperado)
                .compareTo(encontrado)
        );
    }


    private void verificarEstoque(
            String fisico,
            String reservado) {

        entityManager.flush();

        Ingrediente atual = ingredientes

            .findById(ingrediente.id)

            .orElseThrow();

        entityManager.refresh(atual);

        verificarValor(
            fisico,
            atual.estoqueFisico
        );

        verificarValor(
            reservado,
            atual.estoqueReservado
        );
    }


    /* =========================================
       G01 - LIBERACAO DA MESA
       ========================================= */

    @Test
    void liberaMesaAposPagamentoIntegral() {

        prepararFechamento();

        ContaResponse conta = finalizarConta();

        assertEquals(
            StatusConta.FINALIZADA,
            conta.status()
        );

        assertEquals(
            StatusMesa.LIVRE,
            consultarMesa().status
        );
    }


    /* =========================================
       G02 - ENCERRAMENTO DO ATENDIMENTO
       ========================================= */

    @Test
    void removeCodigoDoAtendimentoEncerrado() {

        prepararFechamento();

        ContaResponse conta = finalizarConta();

        assertNull(
            consultarMesa().codigoAtendimento
        );

        Conta persistida = contas

            .findById(conta.id())

            .orElseThrow();

        assertEquals(

            codigoAtendimento,

            persistida.codigoAtendimento
        );
    }


    /* =========================================
       G03 - INVALIDACAO DO ACESSO ANTERIOR
       ========================================= */

    @Test
    void invalidaAcessoDoClienteAnterior() {

        prepararFechamento();

        finalizarConta();

        ResponseStatusException erro =
            assertThrows(

                ResponseStatusException.class,

                () -> mesaService.validarAtendimento(

                    mesa.id,

                    codigoAtendimento
                )
            );

        assertEquals(
            401,
            erro.getStatusCode().value()
        );
    }


    /* =========================================
       G04 - NOVO ATENDIMENTO
       ========================================= */

    @Test
    void permiteNovoAtendimentoNaMesmaMesa() {

        prepararFechamento();

        finalizarConta();

        Mesa novoAtendimento =
            mesaService.acessarComoCliente(860);

        assertEquals(

            StatusMesa.OCUPADA,

            novoAtendimento.status
        );

        assertNotNull(
            novoAtendimento.codigoAtendimento
        );

        assertNotEquals(

            codigoAtendimento,

            novoAtendimento.codigoAtendimento
        );
    }


    /* =========================================
       G05 - ISOLAMENTO ENTRE ATENDIMENTOS
       ========================================= */

    @Test
    void separaPedidosDeAtendimentosDiferentes() {

        prepararFechamento();

        finalizarConta();

        Mesa novoAtendimento =
            mesaService.acessarComoCliente(860);

        Long novoPedidoId = realizarPedido(

            novoAtendimento.codigoAtendimento,

            1
        );

        ContaAbertaResponse aberta =
            pagamentoService.consultarContaAberta(
                mesa.id
            );

        assertEquals(
            1L,
            aberta.totalPedidos()
        );

        verificarValor(
            "29.90",
            aberta.subtotal()
        );

        assertEquals(

            novoPedidoId,

            aberta.pedidos().get(0).id()
        );

        assertNotEquals(

            pedidoId,

            aberta.pedidos().get(0).id()
        );
    }


    /* =========================================
       G06 - PROTECAO CONTRA DUPLO FECHAMENTO
       ========================================= */

    @Test
    void impedeFechamentoDuplicado() {

        prepararFechamento();

        finalizarConta();

        ResponseStatusException erro =
            assertThrows(

                ResponseStatusException.class,

                this::finalizarConta
            );

        assertEquals(
            409,
            erro.getStatusCode().value()
        );
    }


    /* =========================================
       G07 - PRESERVACAO DOS PEDIDOS
       ========================================= */

    @Test
    void preservaHistoricoDosPedidosAposPagamento() {

        prepararFechamento();

        finalizarConta();

        entityManager.flush();
        entityManager.clear();

        Pedido pedido = pedidos

            .findById(pedidoId)

            .orElseThrow();

        assertEquals(
            StatusPedido.ENTREGUE,
            pedido.status
        );

        assertEquals(

            codigoAtendimento,

            pedido.codigoAtendimento
        );

        verificarValor(
            "59.80",
            pedido.valorTotal
        );

        assertEquals(
            1,
            pedido.itens.size()
        );

        assertEquals(
            2,
            pedido.itens.get(0).quantidade
        );
    }


    /* =========================================
       G08 - PRESERVACAO DA BAIXA DE ESTOQUE
       ========================================= */

    @Test
    void pagamentoNaoRealizaNovaBaixaDeEstoque() {

        verificarEstoque("1000", "300");

        prepararFechamento();

        verificarEstoque("700", "0");

        finalizarConta();

        verificarEstoque("700", "0");

        ReservaEstoque reserva = reservas

            .findByPedido_IdOrderByIngrediente_IdAsc(
                pedidoId
            )

            .get(0);

        assertEquals(

            StatusReserva.CONSUMIDA,

            reserva.status
        );

        long consumos = movimentacoes

            .findByPedido_IdOrderByDataHoraAscIdAsc(
                pedidoId
            )

            .stream()

            .filter(movimentacao ->

                movimentacao.tipo ==
                    TipoMovimentacao.CONSUMO
            )

            .count();

        assertEquals(1L, consumos);
    }


    /* =========================================
       G09 - ESTORNO SEM DEVOLUCAO DE ESTOQUE
       ========================================= */

    @Test
    void estornoNaoDevolveIngredientesConsumidos() {

        prepararFechamento();

        ContaResponse conta = finalizarConta();

        verificarEstoque("700", "0");

        long movimentosAntes =
            movimentacoes.count();

        pagamentoService.estornarConta(

            conta.id(),

            new EstornoContaRequest(
                "Estorno financeiro de teste"
            ),

            LOGIN
        );

        verificarEstoque("700", "0");

        assertEquals(

            movimentosAntes,

            movimentacoes.count()
        );

        assertEquals(

            StatusReserva.CONSUMIDA,

            reservas
                .findByPedido_IdOrderByIngrediente_IdAsc(
                    pedidoId
                )
                .get(0)
                .status
        );
    }


    /* =========================================
       G10 - ESTORNO SEM REABERTURA DA MESA
       ========================================= */

    @Test
    void estornoNaoReabreAtendimentoEncerrado() {

        prepararFechamento();

        ContaResponse conta = finalizarConta();

        pagamentoService.estornarConta(

            conta.id(),

            new EstornoContaRequest(
                "Estorno autorizado"
            ),

            LOGIN
        );

        Mesa atual = consultarMesa();

        assertEquals(
            StatusMesa.LIVRE,
            atual.status
        );

        assertNull(
            atual.codigoAtendimento
        );
    }


    /* =========================================
       G11 - PRESERVACAO DOS PAGAMENTOS
       ========================================= */

    @Test
    void estornoPreservaRegistrosFinanceirosOriginais() {

        prepararFechamento();

        ContaResponse conta = finalizarConta();

        pagamentoService.estornarConta(

            conta.id(),

            new EstornoContaRequest(
                "Estorno com auditoria"
            ),

            LOGIN
        );

        entityManager.flush();
        entityManager.clear();

        Conta persistida = contas

            .findById(conta.id())

            .orElseThrow();

        assertEquals(

            StatusConta.ESTORNADA,

            persistida.status
        );

        verificarValor(
            "59.80",
            persistida.valorPago
        );

        assertEquals(

            1L,

            pagamentos.countByConta_Id(
                conta.id()
            )
        );

        verificarValor(

            "59.80",

            pagamentos.somarPagamentosDaConta(
                conta.id()
            )
        );

        assertEquals(

            "Estorno com auditoria",

            persistida.motivoEstorno
        );

        assertNotNull(
            persistida.dataEstorno
        );

        assertEquals(

            gerente.id,

            persistida.estornadoPor.id
        );
    }


    /* =========================================
       G12 - ESTORNO DURANTE NOVO ATENDIMENTO
       ========================================= */

    @Test
    void estornoAnteriorNaoInterfereNoNovoAtendimento() {

        prepararFechamento();

        ContaResponse conta = finalizarConta();

        Mesa novoAtendimento =
            mesaService.acessarComoCliente(860);

        String novoCodigo =
            novoAtendimento.codigoAtendimento;

        Long novoPedidoId = realizarPedido(
            novoCodigo,
            1
        );

        pagamentoService.estornarConta(

            conta.id(),

            new EstornoContaRequest(
                "Estorno de atendimento anterior"
            ),

            LOGIN
        );

        Mesa atual = consultarMesa();

        assertEquals(
            StatusMesa.OCUPADA,
            atual.status
        );

        assertEquals(

            novoCodigo,

            atual.codigoAtendimento
        );

        assertEquals(

            StatusPedido.RECEBIDO,

            pedidos.findById(novoPedidoId)
                .orElseThrow().status
        );

        verificarEstoque("700", "150");
    }


    /* =========================================
       G13 - CODIGO ANTIGO EM NOVO ATENDIMENTO
       ========================================= */

    @Test
    void impedeUsoDeCodigoDeAtendimentoAnterior() {

        prepararFechamento();

        finalizarConta();

        Mesa novoAtendimento =
            mesaService.acessarComoCliente(860);

        Long novoPedidoId = realizarPedido(

            novoAtendimento.codigoAtendimento,

            1
        );

        entregarPedido(novoPedidoId);

        mesaService.solicitarPagamento(mesa.id);

        ResponseStatusException erro =
            assertThrows(

                ResponseStatusException.class,

                () -> pagamentoService.finalizarConta(

                    mesa.id,

                    dadosPagamento(
                        codigoAtendimento,
                        "29.90"
                    ),

                    LOGIN
                )
            );

        assertEquals(
            409,
            erro.getStatusCode().value()
        );
    }


    /* =========================================
       G14 - PERSISTENCIA E ROLLBACK EFETIVO
       ========================================= */

    @Test
    void pagamentoInsuficientePreservaDadosPersistidos() {

        prepararFechamento();

        Long mesaId = mesa.id;
        Long produtoId = produto.id;
        Long ingredienteId = ingrediente.id;
        Long categoriaId = categoria.id;
        Long gerenteId = gerente.id;

        entityManager.flush();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        try {

            long contasAntes = contas.count();

            long pagamentosAntes =
                pagamentos.count();

            long pedidosAntes =
                pedidos.count();

            long movimentosAntes =
                movimentacoes.count();

            ResponseStatusException erro =
                assertThrows(

                    ResponseStatusException.class,

                    () -> pagamentoService.finalizarConta(

                        mesaId,

                        dadosPagamento(
                            codigoAtendimento,
                            "59.79"
                        ),

                        LOGIN
                    )
                );

            assertEquals(
                409,
                erro.getStatusCode().value()
            );

            assertEquals(
                contasAntes,
                contas.count()
            );

            assertEquals(
                pagamentosAntes,
                pagamentos.count()
            );

            assertEquals(
                pedidosAntes,
                pedidos.count()
            );

            assertEquals(
                movimentosAntes,
                movimentacoes.count()
            );

            Mesa atual = mesas.findById(mesaId)
                .orElseThrow();

            assertEquals(

                StatusMesa.AGUARDANDO_PAGAMENTO,

                atual.status
            );

            assertEquals(

                codigoAtendimento,

                atual.codigoAtendimento
            );

            Ingrediente saldo = ingredientes

                .findById(ingredienteId)

                .orElseThrow();

            verificarValor(
                "700",
                saldo.estoqueFisico
            );

            verificarValor(
                "0",
                saldo.estoqueReservado
            );

            ContaResponse finalizada =
                pagamentoService.finalizarConta(

                    mesaId,

                    dadosPagamento(
                        codigoAtendimento,
                        "59.80"
                    ),

                    LOGIN
                );

            assertEquals(

                StatusConta.FINALIZADA,

                finalizada.status()
            );

            assertEquals(

                contasAntes + 1,

                contas.count()
            );

            assertEquals(

                pagamentosAntes + 1,

                pagamentos.count()
            );

            assertEquals(

                StatusMesa.LIVRE,

                mesas.findById(mesaId)
                    .orElseThrow().status
            );

        } finally {

            TestTransaction.start();

            contas
                .findByMesa_IdAndCodigoAtendimento(
                    mesaId,
                    codigoAtendimento
                )
                .ifPresent(conta ->
                    contas.delete(conta)
                );

            contas.flush();

            movimentacoes.deleteAll(

                movimentacoes
                    .findByPedido_IdOrderByDataHoraAscIdAsc(
                        pedidoId
                    )
            );

            reservas.deleteAll(

                reservas
                    .findByPedido_IdOrderByIngrediente_IdAsc(
                        pedidoId
                    )
            );

            entityManager.flush();

            pedidos.deleteById(pedidoId);

            entityManager.flush();

            composicaoService.removerIngrediente(

                produtoId,

                ingredienteId
            );

            produtos.deleteById(produtoId);

            entityManager.flush();

            ingredientes.deleteById(ingredienteId);

            categorias.deleteById(categoriaId);

            mesas.deleteById(mesaId);

            usuarios.deleteById(gerenteId);

            entityManager.flush();

            TestTransaction.flagForCommit();
            TestTransaction.end();
        }
    }
}