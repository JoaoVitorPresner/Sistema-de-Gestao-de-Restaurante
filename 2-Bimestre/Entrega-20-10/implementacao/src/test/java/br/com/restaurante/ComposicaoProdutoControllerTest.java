package br.com.restaurante;

import br.com.restaurante.model.Ingrediente;
import br.com.restaurante.model.UnidadeMedida;

import br.com.restaurante.repository.IngredienteRepository;
import br.com.restaurante.repository.ComposicaoProdutoRepository;

import br.com.restaurante.service.ComposicaoProdutoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(
    username = "gerente",
    roles = "GERENTE"
)
class ComposicaoProdutoControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    ProdutoRepository produtos;

    @Autowired
    IngredienteRepository ingredientes;

    @Autowired
    ComposicaoProdutoRepository composicoes;

    @Autowired
    ComposicaoProdutoService composicaoService;

    private Categoria categoria;

    private Produto produto;

    private Ingrediente ingrediente;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        categoria = new Categoria();

        categoria.nome = "Lanches composicao teste";
        categoria.ativo = true;

        categoria = categorias.saveAndFlush(categoria);


        produto = new Produto();

        produto.nome = "Hamburguer composicao teste";
        produto.preco = new BigDecimal("29.90");
        produto.categoria = categoria;
        produto.ativo = true;
        produto.controlaEstoque = false;

        produto = produtos.saveAndFlush(produto);


        ingrediente = new Ingrediente();

        ingrediente.nome = "Carne composicao teste";

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
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private ResultActions adicionarIngrediente(
        String quantidade
    ) throws Exception {

        String json = """
            {
                "ingredienteId": %d,
                "quantidade": %s
            }
            """.formatted(
                ingrediente.id,
                quantidade
            );

        return mvc.perform(

            post(
                "/api/estoque/composicoes/{produtoId}",
                produto.id
            )
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
        );
    }


    private void criarComposicao()
            throws Exception {

        adicionarIngrediente("150")

            .andExpect(status().isCreated());
    }


    private ResultActions configurarControle(
        boolean controlaEstoque
    ) throws Exception {

        return mvc.perform(

            patch(
                "/api/estoque/composicoes/{produtoId}/controle",
                produto.id
            )
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "controlaEstoque": %s
                }
                """.formatted(controlaEstoque))
        );
    }


    private Produto produtoAtual() {

        return produtos.findById(produto.id)
            .orElseThrow();
    }


    /* =========================================
       CT30 - CADASTRO DE COMPOSICAO
       ========================================= */

    @Test
    void adicionaIngredienteNaComposicao()
            throws Exception {

        adicionarIngrediente("150")

            .andExpect(status().isCreated())

            .andExpect(jsonPath("$.produtoId")
                .value(produto.id.intValue()))

            .andExpect(jsonPath("$.ingredienteId")
                .value(ingrediente.id.intValue()))

            .andExpect(jsonPath("$.quantidade")
                .value(150));

        assertTrue(
            composicoes.existsByProduto_IdAndIngrediente_Id(
                produto.id,
                ingrediente.id
            )
        );
    }


    /* =========================================
       CT31 - CONTROLE AUTOMATICO
       ========================================= */

    @Test
    void ativaControleAoAdicionarIngrediente()
            throws Exception {

        assertFalse(produto.controlaEstoque);

        criarComposicao();

        assertTrue(
            produtoAtual().controlaEstoque
        );
    }


    /* =========================================
       CT32 - CONSULTA DA COMPOSICAO
       ========================================= */

    @Test
    void consultaComposicaoDoProduto()
            throws Exception {

        criarComposicao();

        mvc.perform(

            get(
                "/api/estoque/composicoes/{produtoId}",
                produto.id
            )
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.length()")
            .value(1))

        .andExpect(jsonPath("$[0].nomeIngrediente")
            .value("Carne composicao teste"))

        .andExpect(jsonPath("$[0].quantidade")
            .value(150));
    }


    /* =========================================
       CT33 - INGREDIENTE DUPLICADO
       ========================================= */

    @Test
    void impedeIngredienteDuplicado()
            throws Exception {

        criarComposicao();

        adicionarIngrediente("200")

            .andExpect(status().isConflict());

        assertEquals(
            1,
            composicoes
                .findByProduto_IdOrderByIngrediente_IdAsc(
                    produto.id
                )
                .size()
        );
    }


    /* =========================================
       CT34 - INGREDIENTE INATIVO
       ========================================= */

    @Test
    void impedeIngredienteInativo()
            throws Exception {

        ingrediente.ativo = false;

        ingredientes.saveAndFlush(ingrediente);

        adicionarIngrediente("150")

            .andExpect(status().isConflict());
    }


    /* =========================================
       CT35 - QUANTIDADE ZERO
       ========================================= */

    @Test
    void rejeitaQuantidadeZero()
            throws Exception {

        adicionarIngrediente("0")

            .andExpect(status().isBadRequest());
    }


    /* =========================================
       CT36 - QUANTIDADE NEGATIVA
       ========================================= */

    @Test
    void rejeitaQuantidadeNegativa()
            throws Exception {

        adicionarIngrediente("-150")

            .andExpect(status().isBadRequest());
    }


    /* =========================================
       CT37 - EDICAO DA COMPOSICAO
       ========================================= */

    @Test
    void alteraQuantidadeDoIngrediente()
            throws Exception {

        criarComposicao();

        String json = """
            {
                "ingredienteId": %d,
                "quantidade": 200
            }
            """.formatted(ingrediente.id);

        mvc.perform(

            put(
                "/api/estoque/composicoes/{produtoId}/{ingredienteId}",
                produto.id,
                ingrediente.id
            )
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.quantidade")
            .value(200));

        var composicao = composicoes
            .findByProduto_IdAndIngrediente_Id(
                produto.id,
                ingrediente.id
            )
            .orElseThrow();

        assertEquals(
            0,
            new BigDecimal("200")
                .compareTo(composicao.quantidade)
        );
    }


    /* =========================================
       CT38 - REMOCAO DA COMPOSICAO
       ========================================= */

    @Test
    void removeIngredienteDaComposicao()
            throws Exception {

        criarComposicao();

        mvc.perform(

            delete(
                "/api/estoque/composicoes/{produtoId}/{ingredienteId}",
                produto.id,
                ingrediente.id
            )
            .with(csrf())
        )

        .andExpect(status().isNoContent());

        assertFalse(
            composicoes.existsByProduto_Id(
                produto.id
            )
        );

        assertTrue(
            produtoAtual().controlaEstoque
        );
    }


    /* =========================================
       CT39 - PROTECAO DO CONTROLE
       ========================================= */

    @Test
    void impedeDesativacaoDoControleComReceita()
            throws Exception {

        criarComposicao();

        configurarControle(false)

            .andExpect(status().isConflict());

        assertTrue(
            produtoAtual().controlaEstoque
        );
    }


    /* =========================================
       CT40 - ATIVACAO DO CONTROLE
       ========================================= */

    @Test
    void ativaControleSemComposicao()
            throws Exception {

        configurarControle(true)

            .andExpect(status().isOk())

            .andExpect(jsonPath("$.controlaEstoque")
                .value(true));

        assertTrue(
            produtoAtual().controlaEstoque
        );
    }


    /* =========================================
       CT41 - DESATIVACAO DO CONTROLE
       ========================================= */

    @Test
    void desativaControleSemComposicao()
            throws Exception {

        configurarControle(true)

            .andExpect(status().isOk());

        configurarControle(false)

            .andExpect(status().isOk())

            .andExpect(jsonPath("$.controlaEstoque")
                .value(false));

        assertFalse(
            produtoAtual().controlaEstoque
        );
    }


    /* =========================================
       CT42 - RN13
       PRODUTO CONTROLADO SEM RECEITA
       ========================================= */

    @Test
    void produtoControladoSemReceitaFicaIndisponivel()
            throws Exception {

        configurarControle(true)

            .andExpect(status().isOk());

        assertFalse(
            composicaoService.produtoDisponivel(
                produto.id
            )
        );
    }


    /* =========================================
       CT43 - PRODUTO SEM CONTROLE
       ========================================= */

    @Test
    void produtoSemControlePermaneceDisponivel() {

        assertTrue(
            composicaoService.produtoDisponivel(
                produto.id
            )
        );
    }


    /* =========================================
       CT44 - ESTOQUE SUFICIENTE
       ========================================= */

    @Test
    void produtoComEstoqueSuficienteFicaDisponivel()
            throws Exception {

        criarComposicao();

        assertTrue(
            composicaoService.produtoDisponivel(
                produto.id
            )
        );
    }


    /* =========================================
       CT45 - ESTOQUE INSUFICIENTE
       ========================================= */

    @Test
    void produtoSemEstoqueSuficienteFicaIndisponivel()
            throws Exception {

        criarComposicao();

        ingrediente.estoqueFisico =
            new BigDecimal("100");

        ingredientes.saveAndFlush(ingrediente);

        assertFalse(
            composicaoService.produtoDisponivel(
                produto.id
            )
        );
    }


    /* =========================================
       CT46 - ESTOQUE RESERVADO
       ========================================= */

    @Test
    void disponibilidadeConsideraReservas()
            throws Exception {

        criarComposicao();

        ingrediente.estoqueReservado =
            new BigDecimal("900");

        ingredientes.saveAndFlush(ingrediente);

        assertFalse(
            composicaoService.produtoDisponivel(
                produto.id
            )
        );
    }


    /* =========================================
       CT47 - PROTECAO DO INGREDIENTE
       ========================================= */

    @Test
    void impedeDesativacaoDeIngredienteDeProdutoAtivo()
            throws Exception {

        criarComposicao();

        mvc.perform(

            patch(
                "/api/estoque/ingredientes/{id}/desativar",
                ingrediente.id
            )
            .with(csrf())
        )

        .andExpect(status().isConflict());

        assertTrue(
            ingredientes.findById(ingrediente.id)
                .orElseThrow()
                .ativo
        );
    }


    /* =========================================
       CT48 - EDICAO ADMINISTRATIVA
       ========================================= */

    @Test
    void preservaControleNaEdicaoDoProduto()
            throws Exception {

        criarComposicao();

        String json = """
            {
                "nome": "Hamburguer atualizado",
                "preco": 35.00,
                "controlaEstoque": false,
                "categoria": {
                    "id": %d
                }
            }
            """.formatted(categoria.id);

        mvc.perform(

            put(
                "/api/produtos/{id}",
                produto.id
            )
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.nome")
            .value("Hamburguer atualizado"))

        .andExpect(jsonPath("$.controlaEstoque")
            .value(true));

        assertTrue(
            produtoAtual().controlaEstoque
        );
    }


    /* =========================================
       CT49 - PROTECAO NO CADASTRO
       ========================================= */

    @Test
    void ignoraControleEnviadoNoCadastroComum()
            throws Exception {

        String json = """
            {
                "nome": "Novo produto teste",
                "preco": 20.00,
                "controlaEstoque": true,
                "categoria": {
                    "id": %d
                }
            }
            """.formatted(categoria.id);

        mvc.perform(

            post("/api/produtos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )

        .andExpect(status().isCreated())

        .andExpect(jsonPath("$.controlaEstoque")
            .value(false));
    }


    /* =========================================
       CT50 - ADMINISTRADOR
       ========================================= */

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void permiteAdministradorGerenciarComposicao()
            throws Exception {

        adicionarIngrediente("150")

            .andExpect(status().isCreated());
    }


    /* =========================================
       CT51 - ESTOQUISTA
       ========================================= */

    @Test
    @WithMockUser(roles = "ESTOQUISTA")
    void impedeEstoquistaDeAlterarComposicao()
            throws Exception {

        adicionarIngrediente("150")

            .andExpect(status().isForbidden());
    }


    /* =========================================
       CT52 - ATENDENTE
       ========================================= */

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void impedeAtendenteDeAlterarComposicao()
            throws Exception {

        adicionarIngrediente("150")

            .andExpect(status().isForbidden());
    }


    /* =========================================
       CT53 - COZINHEIRO
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void impedeCozinheiroDeAlterarComposicao()
            throws Exception {

        adicionarIngrediente("150")

            .andExpect(status().isForbidden());
    }


    /* =========================================
       CT54 - CLIENTE
       ========================================= */

    @Test
    @WithMockUser(roles = "CLIENTE")
    void impedeClienteDeAlterarComposicao()
            throws Exception {

        adicionarIngrediente("150")

            .andExpect(status().isForbidden());
    }


    /* =========================================
       CT55 - PROTECAO CSRF
       ========================================= */

    @Test
    void impedeAlteracaoSemCsrf()
            throws Exception {

        mvc.perform(

            patch(
                "/api/estoque/composicoes/{produtoId}/controle",
                produto.id
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "controlaEstoque": true
                }
                """)
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT56 - CAMPO OBRIGATORIO
       ========================================= */

    @Test
    void rejeitaConfiguracaoSemCampoObrigatorio()
            throws Exception {

        mvc.perform(

            patch(
                "/api/estoque/composicoes/{produtoId}/controle",
                produto.id
            )
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
        )

        .andExpect(status().isBadRequest());
    }
}