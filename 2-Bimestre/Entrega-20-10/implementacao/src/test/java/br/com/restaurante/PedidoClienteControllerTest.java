
package br.com.restaurante;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.Pedido;
import br.com.restaurante.model.StatusMesa;
import br.com.restaurante.model.StatusPedido;

import br.com.restaurante.repository.MesaRepository;
import br.com.restaurante.repository.PedidoRepository;

import br.com.restaurante.dto.ItemPedidoRequest;
import br.com.restaurante.dto.PedidoRequest;

import br.com.restaurante.service.PedidoService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet
        .request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoClienteControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MesaRepository mesas;

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    ProdutoRepository produtos;

    @Autowired
    PedidoRepository pedidos;

    @Autowired
    PedidoService pedidoService;

    @Autowired
    ObjectMapper objectMapper;

    private Mesa mesa;

    private Categoria categoria;

    private Produto hamburguer;

    private Produto refrigerante;

    private Produto produtoInativo;

    private Produto produtoCategoriaInativa;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        mesa = criarMesa(400);

        categoria = criarCategoria(
            "Lanches Pedido Teste",
            true
        );

        Categoria categoriaInativa =
            criarCategoria(
                "Categoria Inativa Pedido Teste",
                false
            );

        hamburguer = criarProduto(
            "Hamburguer Pedido Teste",
            "29.90",
            true,
            categoria
        );

        refrigerante = criarProduto(
            "Refrigerante Pedido Teste",
            "7.00",
            true,
            categoria
        );

        produtoInativo = criarProduto(
            "Produto Inativo Pedido Teste",
            "15.00",
            false,
            categoria
        );

        produtoCategoriaInativa = criarProduto(
            "Produto Categoria Inativa Pedido Teste",
            "20.00",
            true,
            categoriaInativa
        );
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Mesa criarMesa(int numero) {

        Mesa novaMesa = mesas.findByNumero(numero)
            .orElseGet(Mesa::new);

        novaMesa.numero = numero;
        novaMesa.capacidade = 4;
        novaMesa.status = StatusMesa.LIVRE;
        novaMesa.codigoAtendimento = null;

        return mesas.saveAndFlush(novaMesa);
    }


    private Categoria criarCategoria(
            String nome,
            boolean ativo) {

        Categoria novaCategoria = new Categoria();

        novaCategoria.nome = nome;
        novaCategoria.ativo = ativo;

        return categorias.save(novaCategoria);
    }


    private Produto criarProduto(
            String nome,
            String preco,
            boolean ativo,
            Categoria categoria) {

        Produto produto = new Produto();

        produto.nome = nome;
        produto.preco = new BigDecimal(preco);
        produto.ativo = ativo;
        produto.categoria = categoria;

        return produtos.save(produto);
    }


    private MockHttpSession autenticarCliente(
            int numeroMesa) throws Exception {

        MvcResult resultado = mvc.perform(

            post("/api/cliente/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"numeroMesa\":" +
                    numeroMesa + "}"
                )
        )

        .andExpect(status().isOk())

        .andReturn();

        return (MockHttpSession)
            resultado.getRequest().getSession(false);
    }


    private ItemPedidoRequest item(
            Produto produto,
            int quantidade) {

        return new ItemPedidoRequest(
            produto.id,
            quantidade
        );
    }


    private PedidoRequest pedidoPadrao() {

        return new PedidoRequest(

            "Sem cebola",

            List.of(
                item(hamburguer, 2),
                item(refrigerante, 1)
            )
        );
    }


    private ResultActions enviarPedido(
            MockHttpSession sessao,
            PedidoRequest dados) throws Exception {

        return mvc.perform(

            post("/api/cliente/pedidos")
                .session(sessao)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(dados)
                )
        );
    }


    private Long criarPedidoValido(
            MockHttpSession sessao) throws Exception {

        MvcResult resultado =
            enviarPedido(sessao, pedidoPadrao())

                .andExpect(status().isCreated())

                .andReturn();

        return objectMapper.readTree(

            resultado.getResponse().getContentAsString()

        ).get("id").asLong();
    }


    /* =========================================
       CT01 - CRIACAO DO PEDIDO
       ========================================= */

    @Test
    void criaPedidoComSucesso() throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(sessao, pedidoPadrao())

            .andExpect(status().isCreated())

            .andExpect(jsonPath("$.id").isNumber())

            .andExpect(jsonPath("$.numeroMesa").value(400))

            .andExpect(jsonPath("$.status").value("RECEBIDO"))

            .andExpect(jsonPath("$.observacoes").value("Sem cebola"))

            .andExpect(jsonPath("$.itens.length()").value(2))

            .andExpect(jsonPath("$.codigoAtendimento").doesNotExist());
    }


    /* =========================================
       CT02 - PERSISTENCIA
       ========================================= */

    @Test
    void persistePedidoEItens() throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        Long id = criarPedidoValido(sessao);

        Pedido pedido = pedidos.findById(id)
            .orElseThrow();

        assertEquals(mesa.id, pedido.mesa.id);

        assertEquals(
            StatusPedido.RECEBIDO,
            pedido.status
        );

        assertEquals(2, pedido.itens.size());

        assertNotNull(pedido.itens.get(0).id);

        assertNotNull(pedido.itens.get(1).id);

        assertEquals(
            sessao.getAttribute("codigoAtendimento"),
            pedido.codigoAtendimento
        );
    }


    /* =========================================
       CT03 - CALCULO DOS VALORES
       ========================================= */

    @Test
    void calculaValorTotalCorretamente()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(sessao, pedidoPadrao())

            .andExpect(status().isCreated())

            .andExpect(
                jsonPath("$.valorTotal").value(66.80)
            )

            .andExpect(
                jsonPath("$.itens[0].subtotal").value(59.80)
            )

            .andExpect(
                jsonPath("$.itens[1].subtotal").value(7.00)
            );
    }


    /* =========================================
       CT04 - PRECO MANIPULADO
       ========================================= */

    @Test
    void ignoraPrecoEnviadoPeloCliente()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        String json = """
            {
                "mesaId": 999,
                "status": "ENTREGUE",
                "valorTotal": 0.01,
                "itens": [
                    {
                        "produtoId": %d,
                        "quantidade": 2,
                        "precoUnitario": 0.01,
                        "subtotal": 0.02
                    }
                ]
            }
            """.formatted(hamburguer.id);

        mvc.perform(

            post("/api/cliente/pedidos")
                .session(sessao)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )

        .andExpect(status().isCreated())

        .andExpect(jsonPath("$.numeroMesa").value(400))

        .andExpect(jsonPath("$.status").value("RECEBIDO"))

        .andExpect(jsonPath("$.valorTotal").value(59.80))

        .andExpect(
            jsonPath("$.itens[0].precoUnitario").value(29.90)
        );
    }


    /* =========================================
       CT05 - HISTORICO DE PRECOS
       ========================================= */

    @Test
    void preservaPrecoHistorico()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        Long id = criarPedidoValido(sessao);

        hamburguer.preco = new BigDecimal("35.00");

        hamburguer.nome = "Hamburguer Atualizado";

        produtos.saveAndFlush(hamburguer);

        mvc.perform(

            get("/api/cliente/pedidos/{id}", id)
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.valorTotal").value(66.80))

        .andExpect(
            jsonPath("$.itens[0].precoUnitario").value(29.90)
        )

        .andExpect(
            jsonPath("$.itens[0].nomeProduto")
                .value("Hamburguer Pedido Teste")
        );
    }


    /* =========================================
       CT06 - AGRUPAMENTO
       ========================================= */

    @Test
    void agrupaProdutosRepetidos()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        PedidoRequest dados = new PedidoRequest(

            null,

            List.of(
                item(hamburguer, 2),
                item(hamburguer, 3)
            )
        );

        enviarPedido(sessao, dados)

            .andExpect(status().isCreated())

            .andExpect(jsonPath("$.itens.length()").value(1))

            .andExpect(
                jsonPath("$.itens[0].quantidade").value(5)
            )

            .andExpect(
                jsonPath("$.valorTotal").value(149.50)
            );
    }


    /* =========================================
       CT07 - PEDIDO VAZIO
       ========================================= */

    @Test
    void rejeitaPedidoVazio() throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(

            sessao,

            new PedidoRequest(null, List.of())
        )

        .andExpect(status().isBadRequest());
    }


    /* =========================================
       CT08 - QUANTIDADE ZERO
       ========================================= */

    @Test
    void rejeitaQuantidadeZero() throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(

            sessao,

            new PedidoRequest(
                null,
                List.of(item(hamburguer, 0))
            )
        )

        .andExpect(status().isBadRequest());
    }


    /* =========================================
       CT09 - QUANTIDADE SUPERIOR AO LIMITE
       ========================================= */

    @Test
    void rejeitaQuantidadeSuperiorAoLimite()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(

            sessao,

            new PedidoRequest(
                null,
                List.of(item(hamburguer, 100))
            )
        )

        .andExpect(status().isBadRequest());
    }


    /* =========================================
       CT10 - LIMITE APOS AGRUPAMENTO
       ========================================= */

    @Test
    void rejeitaQuantidadeAgrupadaSuperiorAoLimite()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(

            sessao,

            new PedidoRequest(

                null,

                List.of(
                    item(hamburguer, 60),
                    item(hamburguer, 40)
                )
            )
        )

        .andExpect(status().isBadRequest());
    }


    /* =========================================
       CT11 - PRODUTO INEXISTENTE
       ========================================= */

    @Test
    void rejeitaProdutoInexistente()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(

            sessao,

            new PedidoRequest(

                null,

                List.of(
                    new ItemPedidoRequest(
                        Long.MAX_VALUE,
                        1
                    )
                )
            )
        )

        .andExpect(status().isNotFound());
    }


    /* =========================================
       CT12 - PRODUTO INATIVO
       ========================================= */

    @Test
    void rejeitaProdutoInativo()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(

            sessao,

            new PedidoRequest(
                null,
                List.of(item(produtoInativo, 1))
            )
        )

        .andExpect(status().isConflict());
    }


    /* =========================================
       CT13 - CATEGORIA INATIVA
       ========================================= */

    @Test
    void rejeitaProdutoDeCategoriaInativa()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        enviarPedido(

            sessao,

            new PedidoRequest(
                null,
                List.of(item(produtoCategoriaInativa, 1))
            )
        )

        .andExpect(status().isConflict());
    }


    /* =========================================
       CT14 - PERSISTENCIA PARCIAL
       ========================================= */

    @Test
    void naoRegistraPedidoParcial()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        long quantidadeAnterior = pedidos.count();

        enviarPedido(

            sessao,

            new PedidoRequest(

                null,

                List.of(
                    item(hamburguer, 2),
                    item(produtoInativo, 1)
                )
            )
        )

        .andExpect(status().isConflict());

        assertEquals(
            quantidadeAnterior,
            pedidos.count()
        );
    }


    /* =========================================
       CT15 - LISTAGEM DO ATENDIMENTO
       ========================================= */

    @Test
    void listaPedidosDoAtendimento()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        criarPedidoValido(sessao);

        criarPedidoValido(sessao);

        mvc.perform(

            get("/api/cliente/pedidos")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.length()").value(2));
    }


    /* =========================================
       CT16 - ISOLAMENTO ENTRE MESAS
       ========================================= */

    @Test
    void impedeConsultaDePedidoDeOutraMesa()
            throws Exception {

        MockHttpSession sessaoMesa400 =
            autenticarCliente(400);

        Long id = criarPedidoValido(sessaoMesa400);

        criarMesa(401);

        MockHttpSession sessaoMesa401 =
            autenticarCliente(401);

        mvc.perform(

            get("/api/cliente/pedidos/{id}", id)
                .session(sessaoMesa401)
        )

        .andExpect(status().isNotFound());
    }


    /* =========================================
       CT17 - ISOLAMENTO ENTRE ATENDIMENTOS
       ========================================= */

    @Test
    void impedeConsultaDeAtendimentoAnterior()
            throws Exception {

        MockHttpSession sessaoAnterior =
            autenticarCliente(400);

        Long id = criarPedidoValido(sessaoAnterior);

        Mesa atual = mesas.findByNumero(400)
            .orElseThrow();

        atual.codigoAtendimento =
            UUID.randomUUID().toString();

        mesas.saveAndFlush(atual);

        MockHttpSession novaSessao =
            autenticarCliente(400);

        mvc.perform(

            get("/api/cliente/pedidos/{id}", id)
                .session(novaSessao)
        )

        .andExpect(status().isNotFound());

        mvc.perform(

            get("/api/cliente/pedidos")
                .session(novaSessao)
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(

            get("/api/cliente/pedidos")
                .session(sessaoAnterior)
        )

        .andExpect(status().isUnauthorized());
    }


    /* =========================================
       CT18 - AGUARDANDO PAGAMENTO
       ========================================= */

    @Test
    void bloqueiaPedidoEnquantoAguardaPagamento()
            throws Exception {

        MockHttpSession sessao =
            autenticarCliente(400);

        Mesa atual = mesas.findByNumero(400)
            .orElseThrow();

        atual.status =
            StatusMesa.AGUARDANDO_PAGAMENTO;

        mesas.saveAndFlush(atual);

        enviarPedido(sessao, pedidoPadrao())

            .andExpect(status().isConflict());
    }


    /* =========================================
       CT19 - SEM AUTENTICACAO
       ========================================= */

    @Test
    void impedeAcessoSemAutenticacao()
            throws Exception {

        mvc.perform(

            get("/api/cliente/pedidos")
        )

        .andExpect(status().is3xxRedirection());
    }


    /* =========================================
       CT20 - ACESSO POR FUNCIONARIO
       ========================================= */

    @Test
    @WithMockUser(roles = "GERENTE")
    void impedeFuncionarioDeRealizarPedidoComoCliente()
            throws Exception {

        mvc.perform(

            post("/api/cliente/pedidos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        pedidoPadrao()
                    )
                )
        )

        .andExpect(status().isForbidden());
    }
}
