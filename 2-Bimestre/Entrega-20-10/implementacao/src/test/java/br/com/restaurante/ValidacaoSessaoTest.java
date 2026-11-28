package br.com.restaurante;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;
import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.StatusMesa;

import br.com.restaurante.repository.UsuarioRepository;
import br.com.restaurante.repository.MesaRepository;
import br.com.restaurante.service.UsuarioAutenticado;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ValidacaoSessaoTest {

    private static final String ADMIN =
            "admin.teste.sessao";

    private static final String FUNCIONARIO =
            "funcionario.teste.sessao";

    private static final String NOVO_LOGIN =
            "funcionario.novo.sessao";

    private static final String SENHA =
            "SenhaTeste123";

    private static final String NOVA_SENHA =
            "NovaSenhaTeste456";

    private static final String BASE =
            "/api/usuarios";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private MesaRepository mesas;

    @Autowired
    private PasswordEncoder encoder;

    private Usuario funcionario;

    private MockHttpSession sessaoAdmin;

    @BeforeEach
    void preparar() throws Exception {

        criarUsuario(
                "Administrador de Teste",
                ADMIN,
                Perfil.ADMINISTRADOR
        );

        funcionario = criarUsuario(
                "Funcionario de Teste",
                FUNCIONARIO,
                Perfil.ATENDENTE
        );

        sessaoAdmin = autenticar(ADMIN, SENHA);
    }

    private Usuario criarUsuario(
            String nome,
            String login,
            Perfil perfil) {

        Usuario usuario = new Usuario();

        usuario.nome = nome;
        usuario.login = login;
        usuario.senha = encoder.encode(SENHA);
        usuario.perfil = perfil;
        usuario.ativo = true;
        usuario.bloqueado = false;
        usuario.tentativasFalhas = 0;
        usuario.versaoSeguranca = 0L;

        return usuarios.saveAndFlush(usuario);
    }

    private MockHttpSession autenticar(
            String login,
            String senha) throws Exception {

        MvcResult resultado = mvc.perform(
                post("/login")
                    .with(csrf())
                    .param("username", login)
                    .param("password", senha)
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/dashboard.html"))
        .andExpect(authenticated().withUsername(login))
        .andReturn();

        MockHttpSession sessao =
                (MockHttpSession) resultado
                        .getRequest()
                        .getSession(false);

        assertNotNull(sessao);

        SecurityContext contexto =
                (SecurityContext) sessao.getAttribute(
                        "SPRING_SECURITY_CONTEXT"
                );

        assertNotNull(contexto);

        assertInstanceOf(
                UsuarioAutenticado.class,
                contexto.getAuthentication().getPrincipal()
        );

        return sessao;
    }

    private MockHttpSession autenticarFuncionario()
            throws Exception {

        return autenticar(FUNCIONARIO, SENHA);
    }

    private Usuario consultarFuncionario() {

        return usuarios.findById(funcionario.id)
                .orElseThrow();
    }

    private void verificarSessaoValida(
            MockHttpSession sessao) throws Exception {

        mvc.perform(
                get("/api/auth/me")
                    .session(sessao)
        )
        .andExpect(status().isOk());

        assertFalse(sessao.isInvalid());
    }

    private void verificarSessaoInvalidada(
            MockHttpSession sessao) throws Exception {

        mvc.perform(
                get("/api/auth/me")
                    .session(sessao)
        )
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401));

        assertTrue(sessao.isInvalid());
    }

    private void editarFuncionario(
            String nome,
            String login,
            Perfil perfil) throws Exception {

        mvc.perform(
                put(BASE + "/" + funcionario.id)
                    .session(sessaoAdmin)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(
                            Map.of(
                                "nome", nome,
                                "login", login,
                                "perfil", perfil.name()
                            )
                    ))
        )
        .andExpect(status().isOk());
    }

    private void redefinirSenha() throws Exception {

        mvc.perform(
                patch(BASE + "/" + funcionario.id + "/senha")
                    .session(sessaoAdmin)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(
                            Map.of("senha", NOVA_SENHA)
                    ))
        )
        .andExpect(status().isOk());
    }

    private void executarAcao(
            String acao) throws Exception {

        mvc.perform(
                patch(BASE + "/" + funcionario.id + "/" + acao)
                    .session(sessaoAdmin)
                    .with(csrf())
        )
        .andExpect(status().isOk());
    }

    private void bloquearFuncionario() throws Exception {

        for (int i = 0; i < 5; i++) {

            mvc.perform(
                    post("/login")
                        .with(csrf())
                        .param("username", FUNCIONARIO)
                        .param("password", "SenhaIncorreta")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/index.html?erro"))
            .andExpect(unauthenticated());
        }

        Usuario atualizado = consultarFuncionario();

        assertTrue(atualizado.bloqueado);
        assertEquals(5, atualizado.tentativasFalhas);
        assertEquals(1L, atualizado.versaoSeguranca);
    }

    // ========================================
    // CT01 - SESSAO VALIDA
    // ========================================

    @Test
    void CT01_loginValidoPreservaSessao()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        verificarSessaoValida(sessao);

        assertEquals(
                0L,
                consultarFuncionario().versaoSeguranca
        );
    }

    // ========================================
    // CT02 - ALTERACAO SOMENTE DO NOME
    // ========================================

    @Test
    void CT02_alteracaoNomePreservaSessao()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        editarFuncionario(
                "Nome Atualizado",
                FUNCIONARIO,
                Perfil.ATENDENTE
        );

        verificarSessaoValida(sessao);

        assertEquals(
                "Nome Atualizado",
                consultarFuncionario().nome
        );

        assertEquals(
                0L,
                consultarFuncionario().versaoSeguranca
        );
    }

    // ========================================
    // CT03 - ALTERACAO DO LOGIN
    // ========================================

    @Test
    void CT03_alteracaoLoginInvalidaSessao()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        editarFuncionario(
                "Funcionario de Teste",
                NOVO_LOGIN,
                Perfil.ATENDENTE
        );

        assertEquals(
                1L,
                consultarFuncionario().versaoSeguranca
        );

        verificarSessaoInvalidada(sessao);
    }

    // ========================================
    // CT04 - ALTERACAO DO PERFIL
    // ========================================

    @Test
    void CT04_alteracaoPerfilInvalidaSessao()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        editarFuncionario(
                "Funcionario de Teste",
                FUNCIONARIO,
                Perfil.COZINHEIRO
        );

        assertEquals(
                Perfil.COZINHEIRO,
                consultarFuncionario().perfil
        );

        assertEquals(
                1L,
                consultarFuncionario().versaoSeguranca
        );

        verificarSessaoInvalidada(sessao);
    }

    // ========================================
    // CT05 - REDEFINICAO DA SENHA
    // ========================================

    @Test
    void CT05_redefinicaoSenhaInvalidaSessao()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        redefinirSenha();

        assertEquals(
                1L,
                consultarFuncionario().versaoSeguranca
        );

        verificarSessaoInvalidada(sessao);
    }

    // ========================================
    // CT06 - DESATIVACAO
    // ========================================

    @Test
    void CT06_desativacaoInvalidaSessao()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        executarAcao("desativar");

        assertFalse(
                consultarFuncionario().ativo
        );

        assertEquals(
                1L,
                consultarFuncionario().versaoSeguranca
        );

        verificarSessaoInvalidada(sessao);
    }

    // ========================================
    // CT07 - DESATIVACAO E REATIVACAO
    // ========================================

    @Test
    void CT07_reativacaoNaoRestauraSessaoAntiga()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        executarAcao("desativar");
        executarAcao("ativar");

        assertTrue(
                consultarFuncionario().ativo
        );

        assertEquals(
                2L,
                consultarFuncionario().versaoSeguranca
        );

        verificarSessaoInvalidada(sessao);

        verificarSessaoValida(
                autenticarFuncionario()
        );
    }

    // ========================================
    // CT08 - BLOQUEIO AUTOMATICO
    // ========================================

    @Test
    void CT08_bloqueioAutomaticoInvalidaSessao()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        bloquearFuncionario();

        verificarSessaoInvalidada(sessao);
    }

    // ========================================
    // CT09 - BLOQUEIO E DESBLOQUEIO
    // ========================================

    @Test
    void CT09_desbloqueioNaoRestauraSessaoAntiga()
            throws Exception {

        MockHttpSession sessao =
                autenticarFuncionario();

        bloquearFuncionario();

        executarAcao("desbloquear");

        Usuario atualizado =
                consultarFuncionario();

        assertFalse(atualizado.bloqueado);
        assertEquals(0, atualizado.tentativasFalhas);
        assertEquals(2L, atualizado.versaoSeguranca);

        verificarSessaoInvalidada(sessao);

        verificarSessaoValida(
                autenticarFuncionario()
        );
    }

    // ========================================
    // CT10 - NOVA AUTENTICACAO
    // ========================================

    @Test
    void CT10_novaSenhaPermiteNovaSessao()
            throws Exception {

        MockHttpSession antiga =
                autenticarFuncionario();

        redefinirSenha();

        verificarSessaoInvalidada(antiga);

        mvc.perform(
                post("/login")
                    .with(csrf())
                    .param("username", FUNCIONARIO)
                    .param("password", SENHA)
        )
        .andExpect(redirectedUrl("/index.html?erro"))
        .andExpect(unauthenticated());

        MockHttpSession nova =
                autenticar(FUNCIONARIO, NOVA_SENHA);

        verificarSessaoValida(nova);
    }

    // ========================================
    // CT11 - PRESERVACAO DA SESSAO DO CLIENTE
    // ========================================

    @Test
    void CT11_sessaoClienteContinuaFuncionando()
            throws Exception {

        Mesa mesa = new Mesa();

        mesa.numero = 9876;
        mesa.capacidade = 4;
        mesa.status = StatusMesa.LIVRE;

        mesas.saveAndFlush(mesa);

        MvcResult resultado = mvc.perform(
                post("/api/cliente/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"numeroMesa\":9876}")
        )
        .andExpect(status().isOk())
        .andReturn();

        MockHttpSession sessao =
                (MockHttpSession) resultado
                        .getRequest()
                        .getSession(false);

        assertNotNull(sessao);

        mvc.perform(
                get("/api/cliente/me")
                    .session(sessao)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.numeroMesa").value(9876))
        .andExpect(jsonPath("$.perfil").value("CLIENTE"));

        assertFalse(sessao.isInvalid());
    }
}