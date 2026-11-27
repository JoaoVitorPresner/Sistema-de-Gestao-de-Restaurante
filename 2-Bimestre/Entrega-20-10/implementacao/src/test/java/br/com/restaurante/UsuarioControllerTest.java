package br.com.restaurante;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;
import br.com.restaurante.repository.UsuarioRepository;
import br.com.restaurante.service.TentativasLoginService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerTest {

    private static final String BASE = "/api/usuarios";

    private static final String ADMIN = "admin.teste.usuarios";
    private static final String FUNCIONARIO = "funcionario.teste.usuarios";

    private static final String SENHA = "SenhaTeste123";
    private static final String NOVA_SENHA = "NovaSenhaTeste456";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private TentativasLoginService tentativas;

    private Usuario administrador;
    private Usuario funcionario;

    @BeforeEach
    void preparar() {

        administrador = criarUsuario(
                "Administrador de Teste",
                ADMIN,
                Perfil.ADMINISTRADOR
        );

        funcionario = criarUsuario(
                "Funcionario de Teste",
                FUNCIONARIO,
                Perfil.ATENDENTE
        );
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

        return usuarios.saveAndFlush(usuario);
    }

    private Usuario consultar(Long id) {

        return usuarios.findById(id).orElseThrow();
    }

    private String cadastro(
            String nome,
            String login,
            String senha,
            String perfil) throws Exception {

        return json.writeValueAsString(Map.of(
                "nome", nome,
                "login", login,
                "senha", senha,
                "perfil", perfil
        ));
    }

    private String edicao(
            String nome,
            String login,
            String perfil) throws Exception {

        return json.writeValueAsString(Map.of(
                "nome", nome,
                "login", login,
                "perfil", perfil
        ));
    }

    // ========================================
    // GRUPO A - ACESSO E PERMISSOES
    // ========================================

    @Test
    void A01_administradorPodeListar() throws Exception {

        mvc.perform(get(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void A02_gerenteNaoPodeAcessar() throws Exception {

        mvc.perform(get(BASE)
                .with(user("gerente").roles("GERENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void A03_atendenteNaoPodeAcessar() throws Exception {

        mvc.perform(get(BASE)
                .with(user(FUNCIONARIO).roles("ATENDENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void A04_cozinheiroNaoPodeAcessar() throws Exception {

        mvc.perform(get(BASE)
                .with(user("cozinheiro").roles("COZINHEIRO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void A05_estoquistaNaoPodeAcessar() throws Exception {

        mvc.perform(get(BASE)
                .with(user("estoquista").roles("ESTOQUISTA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void A06_clienteNaoPodeAcessar() throws Exception {

        mvc.perform(get(BASE)
                .with(user("mesa_1").roles("CLIENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void A07_naoAutenticadoNaoPodeAcessar() throws Exception {

        mvc.perform(get(BASE))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void A08_mutacaoSemCsrfBloqueada() throws Exception {

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "Novo Funcionario",
                        "novo.teste",
                        SENHA,
                        "GERENTE"
                )))
                .andExpect(status().isForbidden());
    }

    // ========================================
    // GRUPO B - CADASTRO E VALIDACAO
    // ========================================

    @Test
    void B01_cadastrarFuncionario() throws Exception {

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "Novo Funcionario",
                        "novo.teste",
                        SENHA,
                        "GERENTE"
                )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("novo.teste"))
                .andExpect(jsonPath("$.perfil").value("GERENTE"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.bloqueado").value(false))
                .andExpect(jsonPath("$.tentativasFalhas").value(0))
                .andExpect(jsonPath("$.senha").doesNotExist());

        assertTrue(usuarios.existsByLogin("novo.teste"));
    }

    @Test
    void B02_senhaArmazenadaComoHash() throws Exception {

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "Funcionario Hash",
                        "hash.teste",
                        SENHA,
                        "COZINHEIRO"
                )))
                .andExpect(status().isCreated());

        Usuario salvo = usuarios.findByLogin("hash.teste")
                .orElseThrow();

        assertNotEquals(SENHA, salvo.senha);
        assertTrue(encoder.matches(SENHA, salvo.senha));
    }

    @Test
    void B03_loginDuplicadoBloqueado() throws Exception {

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "Duplicado",
                        FUNCIONARIO,
                        SENHA,
                        "GERENTE"
                )))
                .andExpect(status().isConflict());
    }

    @Test
    void B04_loginDuplicadoIgnoraMaiusculas() throws Exception {

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "Duplicado",
                        FUNCIONARIO.toUpperCase(),
                        SENHA,
                        "GERENTE"
                )))
                .andExpect(status().isConflict());
    }

    @Test
    void B05_nomeObrigatorio() throws Exception {

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "",
                        "novo.teste",
                        SENHA,
                        "ATENDENTE"
                )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void B06_loginInvalido() throws Exception {

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "Funcionario",
                        "login invalido",
                        SENHA,
                        "ATENDENTE"
                )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void B07_senhaCurtaBloqueada() throws Exception {

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "Funcionario",
                        "novo.teste",
                        "123",
                        "ATENDENTE"
                )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void B08_senhaAcimaDe72BytesBloqueada() throws Exception {

        String senhaLonga = "\u00e1".repeat(40);

        mvc.perform(post(BASE)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cadastro(
                        "Funcionario",
                        "novo.teste",
                        senhaLonga,
                        "ATENDENTE"
                )))
                .andExpect(status().isBadRequest());
    }

    // ========================================
    // GRUPO C - CONSULTA E EDICAO
    // ========================================

    @Test
    void C01_consultarUsuario() throws Exception {

        mvc.perform(get(BASE + "/" + funcionario.id)
                .with(user(ADMIN).roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(funcionario.id))
                .andExpect(jsonPath("$.login").value(FUNCIONARIO))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void C02_usuarioInexistente() throws Exception {

        mvc.perform(get(BASE + "/999999999")
                .with(user(ADMIN).roles("ADMINISTRADOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void C03_editarFuncionario() throws Exception {

        String hashAnterior = funcionario.senha;

        mvc.perform(put(BASE + "/" + funcionario.id)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(edicao(
                        "Funcionario Editado",
                        "funcionario.editado",
                        "ESTOQUISTA"
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(
                        "Funcionario Editado"))
                .andExpect(jsonPath("$.login").value(
                        "funcionario.editado"))
                .andExpect(jsonPath("$.perfil").value(
                        "ESTOQUISTA"));

        Usuario salvo = consultar(funcionario.id);

        assertEquals(Perfil.ESTOQUISTA, salvo.perfil);
        assertEquals(hashAnterior, salvo.senha);
    }

    @Test
    void C04_edicaoComLoginDuplicado() throws Exception {

        mvc.perform(put(BASE + "/" + funcionario.id)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(edicao(
                        "Funcionario",
                        ADMIN,
                        "ATENDENTE"
                )))
                .andExpect(status().isConflict());
    }

    @Test
    void C05_administradorNaoPodeAlterarProprioPerfil()
            throws Exception {

        mvc.perform(put(BASE + "/" + administrador.id)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(edicao(
                        administrador.nome,
                        ADMIN,
                        "GERENTE"
                )))
                .andExpect(status().isConflict());

        assertEquals(
                Perfil.ADMINISTRADOR,
                consultar(administrador.id).perfil
        );
    }

    @Test
    void C06_administradorNaoPodeAlterarProprioLogin()
            throws Exception {

        mvc.perform(put(BASE + "/" + administrador.id)
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(edicao(
                        administrador.nome,
                        "admin.alterado",
                        "ADMINISTRADOR"
                )))
                .andExpect(status().isConflict());
    }

    // ========================================
    // GRUPO D - REDEFINICAO DE SENHA
    // ========================================

    @Test
    void D01_redefinirSenha() throws Exception {

        mvc.perform(patch(BASE + "/" + funcionario.id + "/senha")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        Map.of("senha", NOVA_SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist());

        Usuario salvo = consultar(funcionario.id);

        assertTrue(encoder.matches(NOVA_SENHA, salvo.senha));
        assertFalse(encoder.matches(SENHA, salvo.senha));
    }

    @Test
    void D02_redefinirSenhaInvalida() throws Exception {

        mvc.perform(patch(BASE + "/" + funcionario.id + "/senha")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        Map.of("senha", "123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void D03_redefinicaoRemoveBloqueio() throws Exception {

        funcionario.bloqueado = true;
        funcionario.tentativasFalhas = 5;

        usuarios.saveAndFlush(funcionario);

        mvc.perform(patch(BASE + "/" + funcionario.id + "/senha")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(
                        Map.of("senha", NOVA_SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueado").value(false))
                .andExpect(jsonPath("$.tentativasFalhas").value(0));
    }

    // ========================================
    // GRUPO E - ATIVACAO E DESATIVACAO
    // ========================================

    @Test
    void E01_desativarFuncionario() throws Exception {

        mvc.perform(patch(BASE + "/" + funcionario.id + "/desativar")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        assertFalse(consultar(funcionario.id).ativo);
        assertTrue(usuarios.existsById(funcionario.id));
    }

    @Test
    void E02_reativarFuncionario() throws Exception {

        funcionario.ativo = false;
        usuarios.saveAndFlush(funcionario);

        mvc.perform(patch(BASE + "/" + funcionario.id + "/ativar")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void E03_desativacaoDuplicadaBloqueada() throws Exception {

        funcionario.ativo = false;
        usuarios.saveAndFlush(funcionario);

        mvc.perform(patch(BASE + "/" + funcionario.id + "/desativar")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void E04_ativacaoDuplicadaBloqueada() throws Exception {

        mvc.perform(patch(BASE + "/" + funcionario.id + "/ativar")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void E05_administradorNaoPodeSeDesativar()
            throws Exception {

        mvc.perform(patch(BASE + "/" + administrador.id + "/desativar")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf()))
                .andExpect(status().isConflict());

        assertTrue(consultar(administrador.id).ativo);
    }

    // ========================================
    // GRUPO F - BLOQUEIO E DESBLOQUEIO
    // ========================================

    @Test
    void F01_bloqueioAposCincoTentativas() {

        for (int i = 0; i < 5; i++) {

            tentativas.registrarFalha(FUNCIONARIO);
        }

        Usuario salvo = consultar(funcionario.id);

        assertTrue(salvo.bloqueado);
        assertEquals(5, salvo.tentativasFalhas);
    }

    @Test
    void F02_desbloquearFuncionario() throws Exception {

        funcionario.bloqueado = true;
        funcionario.tentativasFalhas = 5;

        usuarios.saveAndFlush(funcionario);

        mvc.perform(patch(BASE + "/" + funcionario.id + "/desbloquear")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueado").value(false))
                .andExpect(jsonPath("$.tentativasFalhas").value(0));
    }

    @Test
    void F03_desbloqueioDesnecessarioBloqueado()
            throws Exception {

        mvc.perform(patch(BASE + "/" + funcionario.id + "/desbloquear")
                .with(user(ADMIN).roles("ADMINISTRADOR"))
                .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void F04_loginBemSucedidoReiniciaTentativas() {

        tentativas.registrarFalha(FUNCIONARIO);
        tentativas.registrarFalha(FUNCIONARIO);

        assertEquals(
                2,
                consultar(funcionario.id).tentativasFalhas
        );

        tentativas.registrarSucesso(FUNCIONARIO);

        assertEquals(
                0,
                consultar(funcionario.id).tentativasFalhas
        );

        assertFalse(consultar(funcionario.id).bloqueado);
    }

    // ========================================
    // GRUPO G - INDICADORES
    // ========================================

    @Test
    void G01_resumoAdministrativo() throws Exception {

        long total = usuarios.count();

        long ativos = usuarios.findAll()
                .stream()
                .filter(u -> u.ativo)
                .count();

        long bloqueados = usuarios.findAll()
                .stream()
                .filter(u -> u.bloqueado)
                .count();

        long administradoresDisponiveis =
                usuarios.countByPerfilAndAtivoTrueAndBloqueadoFalse(
                        Perfil.ADMINISTRADOR
                );

        mvc.perform(get(BASE + "/resumo")
                .with(user(ADMIN).roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(total))
                .andExpect(jsonPath("$.usuariosAtivos").value(ativos))
                .andExpect(jsonPath("$.usuariosInativos")
                        .value(total - ativos))
                .andExpect(jsonPath("$.usuariosBloqueados")
                        .value(bloqueados))
                .andExpect(jsonPath("$.administradoresDisponiveis")
                        .value(administradoresDisponiveis));
    }
}