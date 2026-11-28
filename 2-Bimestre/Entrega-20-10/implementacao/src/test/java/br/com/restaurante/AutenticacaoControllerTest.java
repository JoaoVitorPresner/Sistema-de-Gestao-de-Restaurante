
package br.com.restaurante;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;
import br.com.restaurante.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet
    .AutoConfigureMockMvc;

import org.springframework.boot.test.context
    .SpringBootTest;

import org.springframework.security.crypto.password
    .PasswordEncoder;

import org.springframework.security.test.context.support
    .WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request
    .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result
    .MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararUsuario() {

        Usuario usuario = usuarios
            .findByLogin("funcionario_teste")
            .orElseGet(Usuario::new);

        usuario.nome = "Funcionario Teste";
        usuario.login = "funcionario_teste";

        usuario.senha = passwordEncoder.encode(
            "SenhaTeste123"
        );

        usuario.perfil = Perfil.GERENTE;
        usuario.ativo = true;
        usuario.bloqueado = false;
        usuario.tentativasFalhas = 0;

        usuarios.save(usuario);
    }

    @Test
    void disponibilizaTokenCsrfSemAutenticacao()
            throws Exception {

        mvc.perform(get("/api/auth/csrf"))

            .andExpect(status().isOk())

            .andExpect(
                jsonPath("$.token").isNotEmpty()
            )

            .andExpect(
    jsonPath("$.headerName")
        .value(
            org.hamcrest.Matchers.isOneOf(
                "X-XSRF-TOKEN",
                "X-CSRF-TOKEN"
            )
        )
)

            .andExpect(
                jsonPath("$.parameterName")
                    .value("_csrf")
            );
    }

    @Test
    @WithMockUser(
        username = "funcionario_teste",
        roles = "GERENTE"
    )
    void identificaFuncionarioAutenticado()
            throws Exception {

        mvc.perform(get("/api/auth/me"))

            .andExpect(status().isOk())

            .andExpect(
                jsonPath("$.id").isNumber()
            )

            .andExpect(
                jsonPath("$.nome")
                    .value("Funcionario Teste")
            )

            .andExpect(
                jsonPath("$.login")
                    .value("funcionario_teste")
            )

            .andExpect(
                jsonPath("$.perfil")
                    .value("GERENTE")
            )

            .andExpect(
                jsonPath("$.senha").doesNotExist()
            );
    }

    @Test
    void impedeConsultaSemAutenticacao()
            throws Exception {

        mvc.perform(get("/api/auth/me"))

            .andExpect(
                status().is3xxRedirection()
            );
    }

    @Test
    @WithMockUser(
        username = "mesa_5",
        roles = "CLIENTE"
    )
    void impedeClienteDeConsultarDadosDeFuncionario()
            throws Exception {

        mvc.perform(get("/api/auth/me"))

            .andExpect(status().isForbidden());
    }
}
