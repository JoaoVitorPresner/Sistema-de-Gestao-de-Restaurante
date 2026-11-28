package br.com.restaurante;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasItem;

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser(
            username = "gerente",
            roles = "GERENTE"
    )
    void cadastraCategoriaEConsulta() throws Exception {

        mvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Bebidas\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$[*].nome").value(hasItem("Bebidas"))
                );
    }

    @Test
    @WithMockUser(
            username = "gerente",
            roles = "GERENTE"
    )
    void rejeitaProdutoSemPreco() throws Exception {

        mvc.perform(post("/api/produtos")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nome\":\"Suco\",\"preco\":0," +
                    "\"categoria\":{\"id\":1}}"
                ))
                .andExpect(status().isBadRequest());
    }

    @Test
    void impedeAcessoSemAutenticacao() throws Exception {

        mvc.perform(get("/api/produtos"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(
            username = "cozinheiro",
            roles = "COZINHEIRO"
    )
    void impedeCozinheiroDeCadastrarCategoria()
            throws Exception {

        mvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Sobremesas\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(
            username = "gerente",
            roles = "GERENTE"
    )
    void impedeCadastroSemTokenCsrf() throws Exception {

        mvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Lanches\"}"))
                .andExpect(status().isForbidden());
    }
}