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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet
        .request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.security.test.web.servlet
        .response.SecurityMockMvcResultMatchers.*;

import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MesaRepository mesas;

    private static final int NUMERO_MESA = 200;

    @BeforeEach
    void prepararMesa() {

        Mesa mesa = mesas.findByNumero(NUMERO_MESA)
                .orElseGet(Mesa::new);

        mesa.numero = NUMERO_MESA;
        mesa.capacidade = 4;
        mesa.status = StatusMesa.LIVRE;
        mesa.codigoAtendimento = null;

        mesas.save(mesa);
    }

    private MockHttpSession autenticarCliente()
            throws Exception {

        MvcResult resultado = mvc.perform(
                post("/api/cliente/login")
                    .with(csrf())
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(
                        "{\"numeroMesa\":200}"
                    )
                )

                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andReturn();

        return (MockHttpSession)
                resultado.getRequest().getSession(false);
    }

    @Test
    void clienteAcessaMesaLivre() throws Exception {

        autenticarCliente();

        Mesa mesa = mesas.findByNumero(NUMERO_MESA)
                .orElseThrow();

        assertEquals(
                StatusMesa.OCUPADA,
                mesa.status
        );

        assertNotNull(mesa.codigoAtendimento);
    }

    @Test
    void clienteAcessaMesaOcupada()
            throws Exception {

        autenticarCliente();

        String codigoOriginal =
                mesas.findByNumero(NUMERO_MESA)
                    .orElseThrow()
                    .codigoAtendimento;

        autenticarCliente();

        String codigoAtual =
                mesas.findByNumero(NUMERO_MESA)
                    .orElseThrow()
                    .codigoAtendimento;

        assertEquals(codigoOriginal, codigoAtual);
    }

    @Test
    void rejeitaMesaInexistente() throws Exception {

        mvc.perform(post("/api/cliente/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroMesa\":99999}"))

                .andExpect(status().isNotFound());
    }

    @Test
    void rejeitaMesaInativa() throws Exception {

        Mesa mesa = mesas.findByNumero(NUMERO_MESA)
                .orElseThrow();

        mesa.status = StatusMesa.INATIVA;

        mesas.save(mesa);

        mvc.perform(post("/api/cliente/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroMesa\":200}"))

                .andExpect(status().isConflict());
    }

    @Test
    void rejeitaMesaAguardandoPagamento()
            throws Exception {

        Mesa mesa = mesas.findByNumero(NUMERO_MESA)
                .orElseThrow();

        mesa.status =
                StatusMesa.AGUARDANDO_PAGAMENTO;

        mesas.save(mesa);

        mvc.perform(post("/api/cliente/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"numeroMesa\":200}"))

                .andExpect(status().isConflict());
    }

    @Test
    void consultaSessaoDoCliente() throws Exception {

        MockHttpSession sessao = autenticarCliente();

        mvc.perform(get("/api/cliente/me")
                .session(sessao))

                .andExpect(status().isOk())
                .andExpect(
                    jsonPath("$.numeroMesa").value(200)
                )
                .andExpect(
                    jsonPath("$.perfil").value("CLIENTE")
                );
    }

    @Test
    void impedeClienteDeAcessarProdutosAdministrativos()
            throws Exception {

        MockHttpSession sessao = autenticarCliente();

        mvc.perform(get("/api/produtos")
                .session(sessao))

                .andExpect(status().isForbidden());
    }

    @Test
    void impedeClienteDeAcessarDashboard()
            throws Exception {

        MockHttpSession sessao = autenticarCliente();

        mvc.perform(get("/dashboard.html")
                .session(sessao))

                .andExpect(status().isForbidden());
    }

    @Test
    void rejeitaSessaoDeAtendimentoAnterior()
            throws Exception {

        MockHttpSession sessao = autenticarCliente();

        Mesa mesa = mesas.findByNumero(NUMERO_MESA)
                .orElseThrow();

        mesa.codigoAtendimento =
                UUID.randomUUID().toString();

        mesas.save(mesa);

        mvc.perform(get("/api/cliente/me")
                .session(sessao))

                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "GERENTE")
    void impedeFuncionarioDeUsarEndpointExclusivoCliente()
            throws Exception {

        mvc.perform(get("/api/cliente/me"))

                .andExpect(status().isForbidden());
    }
}