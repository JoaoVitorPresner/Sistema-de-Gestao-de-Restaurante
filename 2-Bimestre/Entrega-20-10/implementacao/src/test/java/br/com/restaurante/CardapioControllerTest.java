
package br.com.restaurante;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.StatusMesa;
import br.com.restaurante.repository.MesaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet
        .AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.test.context.support
        .WithMockUser;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

import static org.springframework.security.test.web.servlet
        .request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.security.test.web.servlet
        .response.SecurityMockMvcResultMatchers.authenticated;

import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CardapioControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MesaRepository mesas;

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    ProdutoRepository produtos;


    private static final int NUMERO_MESA = 300;

    private Categoria categoriaAtiva;

    private Categoria categoriaInativa;

    private Categoria categoriaVazia;

    private Produto produtoAtivo;

    private Produto produtoInativo;

    private Produto produtoCategoriaInativa;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        Mesa mesa = mesas.findByNumero(NUMERO_MESA)
                .orElseGet(Mesa::new);

        mesa.numero = NUMERO_MESA;
        mesa.capacidade = 4;
        mesa.status = StatusMesa.LIVRE;
        mesa.codigoAtendimento = null;

        mesas.save(mesa);


        categoriaAtiva = criarCategoria(

            "Lanches Cardapio Teste",

            "Categoria de lanches para testes",

            true
        );


        categoriaInativa = criarCategoria(

            "Categoria Inativa Cardapio Teste",

            "Categoria indisponivel",

            false
        );


        categoriaVazia = criarCategoria(

            "Categoria Vazia Cardapio Teste",

            "Categoria sem produtos",

            true
        );


        produtoAtivo = criarProduto(

            "Hamburguer Cardapio Teste",

            "Pao, carne e queijo",

            "29.90",

            true,

            categoriaAtiva
        );


        produtoInativo = criarProduto(

            "Produto Inativo Cardapio Teste",

            "Produto indisponivel",

            "15.00",

            false,

            categoriaAtiva
        );


        // Simula uma inconsistencia no banco
        // para verificar a protecao do cardapio.

        produtoCategoriaInativa = criarProduto(

            "Produto Categoria Inativa Teste",

            "Produto que nao deve ser exibido",

            "20.00",

            true,

            categoriaInativa
        );
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Categoria criarCategoria(

            String nome,

            String descricao,

            boolean ativo) {

        Categoria categoria = new Categoria();

        categoria.nome = nome;
        categoria.descricao = descricao;
        categoria.ativo = ativo;

        return categorias.save(categoria);
    }


    private Produto criarProduto(

            String nome,

            String descricao,

            String preco,

            boolean ativo,

            Categoria categoria) {

        Produto produto = new Produto();

        produto.nome = nome;
        produto.descricao = descricao;
        produto.preco = new BigDecimal(preco);
        produto.ativo = ativo;
        produto.categoria = categoria;

        return produtos.save(produto);
    }


    private MockHttpSession autenticarCliente()
            throws Exception {

        MvcResult resultado = mvc.perform(

            post("/api/cliente/login")

                .with(csrf())

                .contentType(MediaType.APPLICATION_JSON)

                .content(
                    "{\"numeroMesa\":300}"
                )
        )

        .andExpect(status().isOk())

        .andExpect(authenticated())

        .andReturn();


        return (MockHttpSession)
                resultado.getRequest()
                    .getSession(false);
    }


    /* =========================================
       CT01 - CONSULTA AUTENTICADA
       ========================================= */

    @Test
    void clienteAutenticadoConsultaCardapio()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$").isArray()
        );
    }


    /* =========================================
       CT02 - PRODUTO DISPONIVEL
       ========================================= */

    @Test
    void exibeProdutoAtivoDeCategoriaAtiva()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath("$[*].nome",

                hasItem(categoriaAtiva.nome)
            )
        )

        .andExpect(

            jsonPath("$[*].produtos[*].nome",

                hasItem(produtoAtivo.nome)
            )
        );
    }


    /* =========================================
       CT03 - PRODUTO INATIVO
       ========================================= */

    @Test
    void naoExibeProdutoInativo()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath("$[*].produtos[*].nome",

                not(hasItem(produtoInativo.nome))
            )
        );
    }


    /* =========================================
       CT04 - CATEGORIA INATIVA
       ========================================= */

    @Test
    void naoExibeCategoriaInativa()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath("$[*].nome",

                not(hasItem(categoriaInativa.nome))
            )
        );
    }


    /* =========================================
       CT05 - PRODUTO DE CATEGORIA INATIVA
       ========================================= */

    @Test
    void naoExibeProdutoDeCategoriaInativa()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath("$[*].produtos[*].nome",

                not(
                    hasItem(
                        produtoCategoriaInativa.nome
                    )
                )
            )
        );
    }


    /* =========================================
       CT06 - CATEGORIA VAZIA
       ========================================= */

    @Test
    void naoExibeCategoriaSemProdutosDisponiveis()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath("$[*].nome",

                not(hasItem(categoriaVazia.nome))
            )
        );
    }


    /* =========================================
       CT07 - INFORMACOES DO PRODUTO
       ========================================= */

    @Test
    void retornaInformacoesEPrecoCorretos()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        String caminho =

            "$[?(@.id == " +

            categoriaAtiva.id +

            ")].produtos[*]";


        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath(

                caminho + ".nome",

                contains(produtoAtivo.nome)
            )
        )

        .andExpect(

            jsonPath(

                caminho + ".descricao",

                contains(produtoAtivo.descricao)
            )
        )

        .andExpect(

            jsonPath(

                caminho + ".preco",

                contains(29.90)
            )
        );
    }


    /* =========================================
       CT08 - PROTECAO DOS ATRIBUTOS INTERNOS
       ========================================= */

    @Test
    void naoExibeAtributosInternosDasEntidades()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath("$[*].ativo").isEmpty()
        )

        .andExpect(

            jsonPath(
                "$[*].produtos[*].ativo"
            ).isEmpty()
        )

        .andExpect(

            jsonPath(
                "$[*].produtos[*].categoria"
            ).isEmpty()
        );
    }


    /* =========================================
       CT09 - CLIENTE NAO AUTENTICADO
       ========================================= */

    @Test
    void impedeConsultaSemAutenticacao()
            throws Exception {

        mvc.perform(

            get("/api/cliente/cardapio")
        )

        .andExpect(
            status().is3xxRedirection()
        );
    }


    /* =========================================
       CT10 - ACESSO POR FUNCIONARIO
       ========================================= */

    @Test
    @WithMockUser(roles = "GERENTE")
    void impedeFuncionarioDeUsarCardapioDoCliente()
            throws Exception {

        mvc.perform(

            get("/api/cliente/cardapio")
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT11 - ATENDIMENTO INVALIDO
       ========================================= */

    @Test
    void rejeitaSessaoDeAtendimentoAnterior()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        Mesa mesa = mesas.findByNumero(NUMERO_MESA)
                .orElseThrow();

        mesa.codigoAtendimento =
                UUID.randomUUID().toString();

        mesas.saveAndFlush(mesa);


        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isUnauthorized());
    }


    /* =========================================
       CT12 - AGUARDANDO PAGAMENTO
       ========================================= */

    @Test
    void permiteConsultaEnquantoAguardaPagamento()
            throws Exception {

        MockHttpSession sessao =
                autenticarCliente();

        Mesa mesa = mesas.findByNumero(NUMERO_MESA)
                .orElseThrow();

        mesa.status =
                StatusMesa.AGUARDANDO_PAGAMENTO;

        mesas.saveAndFlush(mesa);


        mvc.perform(

            get("/api/cliente/cardapio")
                .session(sessao)
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath("$[*].produtos[*].nome",

                hasItem(produtoAtivo.nome)
            )
        );
    }
}
