
package br.com.restaurante;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
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
class CardapioAdministrativoTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    ProdutoRepository produtos;

    private Categoria categoriaAtiva;
    private Categoria categoriaInativa;
    private Produto produtoAtivo;
    private Produto produtoInativo;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        categoriaAtiva = new Categoria();

        categoriaAtiva.nome = "Lanches de teste";
        categoriaAtiva.descricao = "Categoria ativa";
        categoriaAtiva.ativo = true;

        categoriaAtiva =
            categorias.save(categoriaAtiva);


        categoriaInativa = new Categoria();

        categoriaInativa.nome = "Categoria inativa de teste";
        categoriaInativa.descricao = "Categoria desativada";
        categoriaInativa.ativo = false;

        categoriaInativa =
            categorias.save(categoriaInativa);


        produtoAtivo = new Produto();

        produtoAtivo.nome = "Hamburguer de teste";
        produtoAtivo.descricao = "Produto ativo";
        produtoAtivo.preco = new BigDecimal("25.00");
        produtoAtivo.categoria = categoriaAtiva;
        produtoAtivo.ativo = true;

        produtoAtivo =
            produtos.save(produtoAtivo);


        produtoInativo = new Produto();

        produtoInativo.nome = "Produto inativo de teste";
        produtoInativo.descricao = "Produto desativado";
        produtoInativo.preco = new BigDecimal("15.00");
        produtoInativo.categoria = categoriaInativa;
        produtoInativo.ativo = false;

        produtoInativo =
            produtos.save(produtoInativo);
    }


    /* =========================================
       TESTES DE CATEGORIAS
       ========================================= */

    @Test
    void consultaCategoriaPorId() throws Exception {

        mvc.perform(
            get("/api/categorias/{id}", categoriaAtiva.id)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome")
            .value("Lanches de teste"))
        .andExpect(jsonPath("$.ativo")
            .value(true));
    }


    @Test
    void cadastraCategoriaComoAtiva() throws Exception {

        mvc.perform(
            post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Sobremesas de teste",
                        "descricao": "Doces",
                        "ativo": false
                    }
                    """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.ativo")
            .value(true));
    }


    @Test
    void editaCategoria() throws Exception {

        mvc.perform(
            put("/api/categorias/{id}", categoriaAtiva.id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Lanches atualizados",
                        "descricao": "Nova descricao"
                    }
                    """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome")
            .value("Lanches atualizados"))
        .andExpect(jsonPath("$.descricao")
            .value("Nova descricao"));

        Categoria atualizada = categorias
            .findById(categoriaAtiva.id)
            .orElseThrow();

        assertEquals(
            "Lanches atualizados",
            atualizada.nome
        );
    }


    @Test
    void impedeDesativacaoDeCategoriaComProdutosAtivos()
            throws Exception {

        mvc.perform(
            delete("/api/categorias/{id}", categoriaAtiva.id)
                .with(csrf())
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.erro").exists());

        Categoria categoria = categorias
            .findById(categoriaAtiva.id)
            .orElseThrow();

        assertTrue(categoria.ativo);
    }


    @Test
    void desativaCategoriaSemProdutosAtivos()
            throws Exception {

        mvc.perform(
            delete("/api/produtos/{id}", produtoAtivo.id)
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        mvc.perform(
            delete("/api/categorias/{id}", categoriaAtiva.id)
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        Categoria categoria = categorias
            .findById(categoriaAtiva.id)
            .orElseThrow();

        assertFalse(categoria.ativo);

        assertTrue(
            produtos.existsById(produtoAtivo.id)
        );
    }


    @Test
    void reativaCategoria() throws Exception {

        mvc.perform(
            patch(
                "/api/categorias/{id}/ativar",
                categoriaInativa.id
            )
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo")
            .value(true));

        Categoria categoria = categorias
            .findById(categoriaInativa.id)
            .orElseThrow();

        assertTrue(categoria.ativo);
    }


    /* =========================================
       TESTES DE PRODUTOS
       ========================================= */

    @Test
    void impedeCadastroEmCategoriaInativa()
            throws Exception {

        String json = """
            {
                "nome": "Produto invalido",
                "preco": 20.00,
                "categoria": {
                    "id": %d
                }
            }
            """.formatted(categoriaInativa.id);

        mvc.perform(
            post("/api/produtos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isConflict());
    }


    @Test
    void cadastraProdutoComoAtivo() throws Exception {

        String json = """
            {
                "nome": "Novo produto de teste",
                "preco": 30.00,
                "ativo": false,
                "categoria": {
                    "id": %d
                }
            }
            """.formatted(categoriaAtiva.id);

        mvc.perform(
            post("/api/produtos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.ativo")
            .value(true));
    }


    @Test
    void editaProdutoSemAlterarStatus()
            throws Exception {

        String json = """
            {
                "nome": "Hamburguer atualizado",
                "descricao": "Nova descricao",
                "preco": 35.00,
                "ativo": false,
                "categoria": {
                    "id": %d
                }
            }
            """.formatted(categoriaAtiva.id);

        mvc.perform(
            put("/api/produtos/{id}", produtoAtivo.id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nome")
            .value("Hamburguer atualizado"))
        .andExpect(jsonPath("$.preco")
            .value(35.00))
        .andExpect(jsonPath("$.ativo")
            .value(true));
    }


    @Test
    void impedeTransferenciaDeProdutoAtivoParaCategoriaInativa()
            throws Exception {

        String json = """
            {
                "nome": "Hamburguer de teste",
                "preco": 25.00,
                "categoria": {
                    "id": %d
                }
            }
            """.formatted(categoriaInativa.id);

        mvc.perform(
            put("/api/produtos/{id}", produtoAtivo.id)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
        .andExpect(status().isConflict());
    }


    @Test
    void desativaProdutoSemExcluirRegistro()
            throws Exception {

        mvc.perform(
            delete("/api/produtos/{id}", produtoAtivo.id)
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        Produto produto = produtos
            .findById(produtoAtivo.id)
            .orElseThrow();

        assertFalse(produto.ativo);

        assertEquals(
            "Hamburguer de teste",
            produto.nome
        );
    }


    @Test
    void reativaProdutoDeCategoriaAtiva()
            throws Exception {

        mvc.perform(
            delete("/api/produtos/{id}", produtoAtivo.id)
                .with(csrf())
        )
        .andExpect(status().isNoContent());

        mvc.perform(
            patch(
                "/api/produtos/{id}/ativar",
                produtoAtivo.id
            )
            .with(csrf())
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ativo")
            .value(true));
    }


    @Test
    void impedeReativacaoDeProdutoComCategoriaInativa()
            throws Exception {

        mvc.perform(
            patch(
                "/api/produtos/{id}/ativar",
                produtoInativo.id
            )
            .with(csrf())
        )
        .andExpect(status().isConflict());

        Produto produto = produtos
            .findById(produtoInativo.id)
            .orElseThrow();

        assertFalse(produto.ativo);
    }


    /* =========================================
       TESTES DE CONSULTA E SEGURANCA
       ========================================= */

    @Test
    void consultaApenasProdutosDisponiveisNoCardapio() {

        var disponiveis = produtos
            .findByAtivoTrueAndCategoria_AtivoTrueOrderByNomeAsc();

        assertTrue(
            disponiveis.stream().anyMatch(
                p -> p.id.equals(produtoAtivo.id)
            )
        );

        assertFalse(
            disponiveis.stream().anyMatch(
                p -> p.id.equals(produtoInativo.id)
            )
        );

        assertTrue(
            disponiveis.stream().allMatch(
                p -> p.ativo && p.categoria.ativo
            )
        );
    }


    @Test
    @WithMockUser(
        username = "atendente",
        roles = "ATENDENTE"
    )
    void impedeAtendenteDeDesativarProduto()
            throws Exception {

        mvc.perform(
            delete("/api/produtos/{id}", produtoAtivo.id)
                .with(csrf())
        )
        .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(
        username = "admin",
        roles = "ADMINISTRADOR"
    )
    void permiteAdministradorConsultarProdutos()
            throws Exception {

        mvc.perform(
            get("/api/produtos")
        )
        .andExpect(status().isOk());
    }
}
