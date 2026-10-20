package br.com.restaurante;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc class ApiControllerTest {
 @Autowired MockMvc mvc;
 @Test void cadastraCategoriaEConsulta() throws Exception {mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Bebidas\"}")).andExpect(status().isCreated());mvc.perform(get("/api/categorias")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Bebidas"));}
 @Test void rejeitaProdutoSemPreco() throws Exception {mvc.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Suco\",\"preco\":0,\"categoria\":{\"id\":1}}")).andExpect(status().isBadRequest());}
}
