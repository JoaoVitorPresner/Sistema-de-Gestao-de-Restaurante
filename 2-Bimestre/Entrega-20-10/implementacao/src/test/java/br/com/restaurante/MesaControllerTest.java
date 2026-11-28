package br.com.restaurante;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.StatusMesa;
import br.com.restaurante.repository.MesaRepository;

import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest
@AutoConfigureMockMvc
class MesaControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MesaRepository mesas;

    private Long mesaId;

    @BeforeEach
    void prepararMesa() {

        Mesa mesa = mesas.findByNumero(99)
                .orElseGet(Mesa::new);

        mesa.numero = 99;
        mesa.capacidade = 4;
        mesa.status = StatusMesa.LIVRE;

        mesaId = mesas.save(mesa).id;
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void listaMesas() throws Exception {

        mvc.perform(get("/api/mesas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void cadastraMesa() throws Exception {

        mvc.perform(post("/api/mesas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"numero\":100,\"capacidade\":6}"
                ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(100))
                .andExpect(jsonPath("$.status").value("LIVRE"));
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void rejeitaNumeroDuplicado() throws Exception {

        mvc.perform(post("/api/mesas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"numero\":99,\"capacidade\":4}"
                ))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void ocupaMesaLivre() throws Exception {

        mvc.perform(patch(
                    "/api/mesas/{id}/ocupar", mesaId
                )
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.status").value("OCUPADA")
                );
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void impedeOcuparMesaJaOcupada()
            throws Exception {

        Mesa mesa = mesas.findById(mesaId)
                .orElseThrow();

        mesa.status = StatusMesa.OCUPADA;

        mesas.save(mesa);

        mvc.perform(patch(
                    "/api/mesas/{id}/ocupar", mesaId
                )
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void impedeDesativarMesaOcupada()
            throws Exception {

        Mesa mesa = mesas.findById(mesaId)
                .orElseThrow();

        mesa.status = StatusMesa.OCUPADA;

        mesas.save(mesa);

        mvc.perform(patch(
                    "/api/mesas/{id}/desativar", mesaId
                )
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void desativaEReativaMesa() throws Exception {

        mvc.perform(patch(
                    "/api/mesas/{id}/desativar", mesaId
                )
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.status").value("INATIVA")
                );

        mvc.perform(patch(
                    "/api/mesas/{id}/ativar", mesaId
                )
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.status").value("LIVRE")
                );
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void solicitaPagamento() throws Exception {

        Mesa mesa = mesas.findById(mesaId)
                .orElseThrow();

        mesa.status = StatusMesa.OCUPADA;

        mesas.save(mesa);

        mvc.perform(patch(
                    "/api/mesas/{id}/solicitar-pagamento",
                    mesaId
                )
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.status")
                        .value("AGUARDANDO_PAGAMENTO")
                );
    }

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void impedeAtendenteDeCadastrarMesa()
            throws Exception {

        mvc.perform(post("/api/mesas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"numero\":101,\"capacidade\":4}"
                ))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void impedeCozinheiroDeGerenciarMesas()
            throws Exception {

        mvc.perform(get("/api/mesas"))
                .andExpect(status().isForbidden());
    }
}