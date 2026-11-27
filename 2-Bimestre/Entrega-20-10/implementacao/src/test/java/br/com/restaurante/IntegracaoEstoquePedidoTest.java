package br.com.restaurante;

import br.com.restaurante.dto.ItemPedidoRequest;
import br.com.restaurante.dto.PedidoRequest;
import br.com.restaurante.dto.PedidoResponse;
import br.com.restaurante.dto.CancelamentoPedidoRequest;
import br.com.restaurante.dto.ComposicaoProdutoRequest;

import br.com.restaurante.model.*;
import br.com.restaurante.repository.*;

import br.com.restaurante.service.PedidoService;
import br.com.restaurante.service.ComposicaoProdutoService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.test.context.transaction.TestTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@WithMockUser(
    username = "gerente_estoque_teste",
    roles = "GERENTE"
)
class IntegracaoEstoquePedidoTest {

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    ProdutoRepository produtos;

    @Autowired
    IngredienteRepository ingredientes;

    @Autowired
    ComposicaoProdutoRepository composicoes;

    @Autowired
    ReservaEstoqueRepository reservas;

    @Autowired
    MovimentacaoEstoqueRepository movimentacoes;

    @Autowired
    PedidoRepository pedidos;

    @Autowired
    MesaRepository mesas;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PedidoService pedidoService;

    @Autowired
    ComposicaoProdutoService composicaoService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EntityManager entityManager;

    private Categoria categoria;

    private Produto produto;

    private Ingrediente ingrediente;

    private Mesa mesa;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        Usuario gerente = new Usuario();

        gerente.nome = "Gerente de estoque teste";
        gerente.login = "gerente_estoque_teste";

        gerente.senha =
            passwordEncoder.encode("SenhaTeste123");

        gerente.perfil = Perfil.GERENTE;
        gerente.ativo = true;
        gerente.bloqueado = false;

        usuarios.saveAndFlush(gerente);


        categoria = new Categoria();

        categoria.nome = "Categoria integracao estoque";
        categoria.ativo = true;

        categoria = categorias.saveAndFlush(categoria);


        produto = criarProduto(
            "Hamburguer integracao teste"
        );


        ingrediente = new Ingrediente();

        ingrediente.nome = "Carne integracao teste";

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

        mesa.numero = 999;
        mesa.capacidade = 4;

        mesa.status = StatusMesa.OCUPADA;

        mesa.codigoAtendimento =
            UUID.randomUUID().toString();

        mesa = mesas.saveAndFlush(mesa);
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Produto criarProduto(String nome) {

        Produto novo = new Produto();

        novo.nome = nome;
        novo.preco = new BigDecimal("29.90");
        novo.categoria = categoria;
        novo.ativo = true;
        novo.controlaEstoque = false;

        return produtos.saveAndFlush(novo);
    }


    private PedidoResponse realizarPedido(
        int quantidade
    ) {

        return realizarPedidoComItens(

            List.of(

                new ItemPedidoRequest(
                    produto.id,
                    quantidade
                )
            )
        );
    }


    private PedidoResponse realizarPedidoComItens(
        List<ItemPedidoRequest> itens
    ) {

        PedidoRequest dados = new PedidoRequest(
            null,
            itens
        );

        return pedidoService.realizarPedido(

            mesa.id,

            mesa.codigoAtendimento,

            dados
        );
    }


    private void verificarSaldo(
        String fisico,
        String reservado,
        String disponivel
    ) {

        entityManager.flush();

        Ingrediente atual = ingredientes
            .findById(ingrediente.id)
            .orElseThrow();

        entityManager.refresh(atual);

        assertEquals(

            0,

            new BigDecimal(fisico)
                .compareTo(atual.estoqueFisico)
        );

        assertEquals(

            0,

            new BigDecimal(reservado)
                .compareTo(atual.estoqueReservado)
        );

        assertEquals(

            0,

            new BigDecimal(disponivel)
                .compareTo(atual.getEstoqueDisponivel())
        );
    }


    private ReservaEstoque buscarReserva(
        Long pedidoId
    ) {

        List<ReservaEstoque> registros =
            reservas.findByPedido_IdOrderByIngrediente_IdAsc(
                pedidoId
            );

        assertEquals(1, registros.size());

        return registros.get(0);
    }


    private void cancelarPedido(Long pedidoId) {

        pedidoService.cancelarPedido(

            pedidoId,

            new CancelamentoPedidoRequest(
                "Cancelamento de teste"
            ),

            "gerente_estoque_teste"
        );
    }


    private void verificarMovimentacao(
        Long pedidoId,
        TipoMovimentacao tipo,
        String quantidade
    ) {

        boolean encontrada = movimentacoes

            .findByPedido_IdOrderByDataHoraAscIdAsc(
                pedidoId
            )

            .stream()

            .anyMatch(movimentacao ->

                movimentacao.tipo == tipo &&

                movimentacao.quantidade.compareTo(
                    new BigDecimal(quantidade)
                ) == 0
            );

        assertTrue(encontrada);
    }


    /* =========================================
       CT57 - RN09 E RN10
       CONFIRMACAO E RESERVA
       ========================================= */

    @Test
    void confirmaPedidoEReservaIngredientes() {

        PedidoResponse pedido = realizarPedido(2);

        assertEquals(
            StatusPedido.RECEBIDO,
            pedido.status()
        );

        verificarSaldo("1000", "300", "700");

        ReservaEstoque reserva =
            buscarReserva(pedido.id());

        assertEquals(
            StatusReserva.RESERVADA,
            reserva.status
        );

        assertEquals(

            0,

            new BigDecimal("300")
                .compareTo(reserva.quantidade)
        );
    }


    /* =========================================
       CT58 - HISTORICO DE RESERVA
       ========================================= */

    @Test
    void registraMovimentacaoDeReserva() {

        PedidoResponse pedido = realizarPedido(2);

        verificarMovimentacao(

            pedido.id(),

            TipoMovimentacao.RESERVA,

            "300"
        );
    }


    /* =========================================
       CT59 - INICIO DO PREPARO
       ========================================= */

    @Test
    void iniciarPreparoMantemReserva() {

        PedidoResponse pedido = realizarPedido(2);

        PedidoResponse atualizado =
            pedidoService.iniciarPreparo(
                pedido.id()
            );

        assertEquals(
            StatusPedido.EM_PREPARO,
            atualizado.status()
        );

        verificarSaldo("1000", "300", "700");

        assertEquals(

            StatusReserva.RESERVADA,

            buscarReserva(pedido.id()).status
        );
    }


    /* =========================================
       CT60 - RN11
       CONSUMO DOS INGREDIENTES
       ========================================= */

    @Test
    void pedidoProntoRealizaBaixaFisica() {

        PedidoResponse pedido = realizarPedido(2);

        pedidoService.iniciarPreparo(pedido.id());

        PedidoResponse atualizado =
            pedidoService.marcarPronto(pedido.id());

        assertEquals(
            StatusPedido.PRONTO,
            atualizado.status()
        );

        verificarSaldo("700", "0", "700");

        assertEquals(

            StatusReserva.CONSUMIDA,

            buscarReserva(pedido.id()).status
        );

        verificarMovimentacao(

            pedido.id(),

            TipoMovimentacao.CONSUMO,

            "300"
        );
    }


    /* =========================================
       CT61 - ENTREGA SEM NOVA BAIXA
       ========================================= */

    @Test
    void entregaNaoRealizaNovaBaixa() {

        PedidoResponse pedido = realizarPedido(2);

        pedidoService.iniciarPreparo(pedido.id());

        pedidoService.marcarPronto(pedido.id());

        PedidoResponse atualizado =
            pedidoService.registrarEntrega(
                pedido.id()
            );

        assertEquals(
            StatusPedido.ENTREGUE,
            atualizado.status()
        );

        verificarSaldo("700", "0", "700");

        long consumos = movimentacoes

            .findByPedido_IdOrderByDataHoraAscIdAsc(
                pedido.id()
            )

            .stream()

            .filter(m ->
                m.tipo == TipoMovimentacao.CONSUMO
            )

            .count();

        assertEquals(1L, consumos);
    }


    /* =========================================
       CT62 - RN12
       CANCELAMENTO ANTES DO PREPARO
       ========================================= */

    @Test
    void cancelamentoAntesDoPreparoLiberaReserva() {

        PedidoResponse pedido = realizarPedido(2);

        cancelarPedido(pedido.id());

        verificarSaldo("1000", "0", "1000");

        assertEquals(

            StatusReserva.LIBERADA,

            buscarReserva(pedido.id()).status
        );

        verificarMovimentacao(

            pedido.id(),

            TipoMovimentacao.LIBERACAO,

            "300"
        );

        assertEquals(

            StatusPedido.CANCELADO,

            pedidos.findById(pedido.id())
                .orElseThrow()
                .status
        );
    }


    /* =========================================
       CT63 - CANCELAMENTO DURANTE PREPARO
       ========================================= */

    @Test
    void cancelamentoDurantePreparoRegistraPerda() {

        PedidoResponse pedido = realizarPedido(2);

        pedidoService.iniciarPreparo(pedido.id());

        cancelarPedido(pedido.id());

        verificarSaldo("700", "0", "700");

        assertEquals(

            StatusReserva.PERDIDA,

            buscarReserva(pedido.id()).status
        );

        verificarMovimentacao(

            pedido.id(),

            TipoMovimentacao.PERDA,

            "300"
        );
    }


    /* =========================================
       CT64 - CANCELAMENTO DE PEDIDO PRONTO
       ========================================= */

    @Test
    void cancelamentoDePedidoProntoNaoDevolveEstoque() {

        PedidoResponse pedido = realizarPedido(2);

        pedidoService.iniciarPreparo(pedido.id());

        pedidoService.marcarPronto(pedido.id());

        cancelarPedido(pedido.id());

        verificarSaldo("700", "0", "700");

        assertEquals(

            StatusReserva.CONSUMIDA,

            buscarReserva(pedido.id()).status
        );

        assertEquals(

            2,

            movimentacoes
                .findByPedido_IdOrderByDataHoraAscIdAsc(
                    pedido.id()
                )
                .size()
        );
    }


    /* =========================================
       CT65 - RN09
       ESTOQUE INSUFICIENTE
       ========================================= */

    @Test
    void impedePedidoComEstoqueInsuficiente() {

        ResponseStatusException erro = assertThrows(

            ResponseStatusException.class,

            () -> realizarPedido(7)
        );

        assertEquals(
            409,
            erro.getStatusCode().value()
        );
    }


    /* =========================================
       CT66 - RESERVAS DE PEDIDOS DIFERENTES
       ========================================= */

    @Test
    void acumulaReservasDePedidosDiferentes() {

        PedidoResponse primeiro = realizarPedido(2);

        PedidoResponse segundo = realizarPedido(3);

        verificarSaldo("1000", "750", "250");

        assertEquals(

            0,

            new BigDecimal("300").compareTo(
                buscarReserva(primeiro.id()).quantidade
            )
        );

        assertEquals(

            0,

            new BigDecimal("450").compareTo(
                buscarReserva(segundo.id()).quantidade
            )
        );
    }


    /* =========================================
       CT67 - INGREDIENTE COMPARTILHADO
       ========================================= */

    @Test
    void somaIngredientesDeProdutosDiferentes() {

        Produto segundoProduto = criarProduto(
            "Segundo hamburguer teste"
        );

        composicaoService.adicionarIngrediente(

            segundoProduto.id,

            new ComposicaoProdutoRequest(

                ingrediente.id,

                new BigDecimal("200")
            )
        );

        PedidoResponse pedido =
            realizarPedidoComItens(

                List.of(

                    new ItemPedidoRequest(
                        produto.id,
                        2
                    ),

                    new ItemPedidoRequest(
                        segundoProduto.id,
                        1
                    )
                )
            );

        verificarSaldo("1000", "500", "500");

        assertEquals(

            0,

            new BigDecimal("500").compareTo(
                buscarReserva(pedido.id()).quantidade
            )
        );
    }


    /* =========================================
       CT68 - SOMA SUPERIOR AO DISPONIVEL
       ========================================= */

    @Test
    void impedeSomaDeIngredientesSuperiorAoEstoque() {

        Produto segundoProduto = criarProduto(
            "Produto compartilhado teste"
        );

        composicaoService.adicionarIngrediente(

            segundoProduto.id,

            new ComposicaoProdutoRequest(

                ingrediente.id,

                new BigDecimal("200")
            )
        );

        ResponseStatusException erro = assertThrows(

            ResponseStatusException.class,

            () -> realizarPedidoComItens(

                List.of(

                    new ItemPedidoRequest(
                        produto.id,
                        4
                    ),

                    new ItemPedidoRequest(
                        segundoProduto.id,
                        3
                    )
                )
            )
        );

        assertEquals(
            409,
            erro.getStatusCode().value()
        );
    }


    /* =========================================
       CT69 - PRESERVACAO DA RESERVA ORIGINAL
       ========================================= */

    @Test
    void alteracaoDaReceitaNaoModificaReservaOriginal() {

        PedidoResponse pedido = realizarPedido(2);

        composicaoService.editarIngrediente(

            produto.id,

            ingrediente.id,

            new ComposicaoProdutoRequest(

                ingrediente.id,

                new BigDecimal("250")
            )
        );

        pedidoService.iniciarPreparo(pedido.id());

        pedidoService.marcarPronto(pedido.id());

        verificarSaldo("700", "0", "700");

        assertEquals(

            0,

            new BigDecimal("300").compareTo(
                buscarReserva(pedido.id()).quantidade
            )
        );
    }


    /* =========================================
       CT70 - RN13
       PRODUTO CONTROLADO SEM COMPOSICAO
       ========================================= */

    @Test
    void impedePedidoDeProdutoControladoSemReceita() {

        composicaoService.removerIngrediente(

            produto.id,

            ingrediente.id
        );

        ResponseStatusException erro = assertThrows(

            ResponseStatusException.class,

            () -> realizarPedido(1)
        );

        assertEquals(
            409,
            erro.getStatusCode().value()
        );
    }


    /* =========================================
       CT71 - PRODUTO SEM CONTROLE
       ========================================= */

    @Test
    void permitePedidoDeProdutoSemControleEstoque() {

        Produto bebida = criarProduto(
            "Bebida sem controle teste"
        );

        PedidoResponse pedido =
            realizarPedidoComItens(

                List.of(

                    new ItemPedidoRequest(
                        bebida.id,
                        2
                    )
                )
            );

        assertEquals(
            StatusPedido.RECEBIDO,
            pedido.status()
        );

        assertTrue(

            reservas
                .findByPedido_IdOrderByIngrediente_IdAsc(
                    pedido.id()
                )
                .isEmpty()
        );

        verificarSaldo("1000", "0", "1000");
    }


    /* =========================================
       CT72 - PROTECAO CONTRA DUPLA BAIXA
       ========================================= */

    @Test
    void impedeMarcarPedidoProntoDuasVezes() {

        PedidoResponse pedido = realizarPedido(2);

        pedidoService.iniciarPreparo(pedido.id());

        pedidoService.marcarPronto(pedido.id());

        ResponseStatusException erro = assertThrows(

            ResponseStatusException.class,

            () -> pedidoService.marcarPronto(
                pedido.id()
            )
        );

        assertEquals(
            409,
            erro.getStatusCode().value()
        );
    }


    /* =========================================
       CT73 - PROTECAO DO INGREDIENTE RESERVADO
       ========================================= */

    @Test
    void identificaExistenciaDeReservaAtiva() {

        PedidoResponse pedido = realizarPedido(2);

        assertTrue(

            reservas.existsByIngrediente_IdAndStatus(

                ingrediente.id,

                StatusReserva.RESERVADA
            )
        );

        cancelarPedido(pedido.id());

        assertFalse(

            reservas.existsByIngrediente_IdAndStatus(

                ingrediente.id,

                StatusReserva.RESERVADA
            )
        );
    }


    /* =========================================
       CT74 - CANCELAMENTO ISOLADO
       ========================================= */

    @Test
    void cancelarPedidoNaoLiberaReservaDeOutroPedido() {

        PedidoResponse primeiro = realizarPedido(2);

        PedidoResponse segundo = realizarPedido(3);

        cancelarPedido(primeiro.id());

        verificarSaldo("1000", "450", "550");

        assertEquals(

            StatusReserva.LIBERADA,

            buscarReserva(primeiro.id()).status
        );

        assertEquals(

            StatusReserva.RESERVADA,

            buscarReserva(segundo.id()).status
        );
    }

    /* =========================================
   CT75 - ROLLBACK EFETIVO DA TRANSACAO
   ========================================= */

@Test
void reverteTransacaoQuandoEstoqueInsuficiente() {

    Long mesaId = mesa.id;
    Long produtoId = produto.id;
    Long ingredienteId = ingrediente.id;
    Long categoriaId = categoria.id;

    String codigoAtendimento =
        mesa.codigoAtendimento;

    Long usuarioId = usuarios
        .findByLogin("gerente_estoque_teste")
        .orElseThrow()
        .id;

    entityManager.flush();

    TestTransaction.flagForCommit();
    TestTransaction.end();

    try {

        long pedidosAntes = pedidos.count();

        long reservasAntes = reservas.count();

        long movimentacoesAntes =
            movimentacoes.count();

        PedidoRequest dados = new PedidoRequest(

            null,

            List.of(

                new ItemPedidoRequest(
                    produtoId,
                    7
                )
            )
        );

        ResponseStatusException erro = assertThrows(

            ResponseStatusException.class,

            () -> pedidoService.realizarPedido(
                mesaId,
                codigoAtendimento,
                dados
            )
        );

        assertEquals(
            409,
            erro.getStatusCode().value()
        );

        assertEquals(
            pedidosAntes,
            pedidos.count()
        );

        assertEquals(
            reservasAntes,
            reservas.count()
        );

        assertEquals(
            movimentacoesAntes,
            movimentacoes.count()
        );

        Ingrediente atual = ingredientes
            .findById(ingredienteId)
            .orElseThrow();

        assertEquals(

            0,

            new BigDecimal("1000")
                .compareTo(atual.estoqueFisico)
        );

        assertEquals(

            0,

            BigDecimal.ZERO
                .compareTo(atual.estoqueReservado)
        );

    } finally {

        TestTransaction.start();

        composicaoService.removerIngrediente(
            produtoId,
            ingredienteId
        );

        produtos.deleteById(produtoId);

        ingredientes.deleteById(ingredienteId);

        categorias.deleteById(categoriaId);

        mesas.deleteById(mesaId);

        usuarios.deleteById(usuarioId);

        entityManager.flush();

        TestTransaction.flagForCommit();
        TestTransaction.end();
    }
}
}