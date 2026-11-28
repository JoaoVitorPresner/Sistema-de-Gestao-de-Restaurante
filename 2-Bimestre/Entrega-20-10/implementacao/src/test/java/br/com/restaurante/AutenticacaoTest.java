package br.com.restaurante;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;
import br.com.restaurante.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacaoTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String LOGIN = "gerente_teste";

    private static final String SENHA = "SenhaTeste123!";

    @BeforeEach
    void prepararUsuario() {

        Usuario usuario = usuarios.findByLogin(LOGIN)
                .orElseGet(Usuario::new);

        usuario.nome = "Gerente de Teste";
        usuario.login = LOGIN;
        usuario.senha = passwordEncoder.encode(SENHA);
        usuario.perfil = Perfil.GERENTE;
        usuario.ativo = true;
        usuario.bloqueado = false;
        usuario.tentativasFalhas = 0;

        usuarios.save(usuario);
    }

    @Test
    void autenticaComCredenciaisCorretas()
            throws Exception {

        mvc.perform(post("/login")
                .with(csrf())
                .param("username", LOGIN)
                .param("password", SENHA))

                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard.html"))
                .andExpect(authenticated().withUsername(LOGIN));
    }

    @Test
    void rejeitaSenhaIncorreta()
            throws Exception {

        mvc.perform(post("/login")
                .with(csrf())
                .param("username", LOGIN)
                .param("password", "SenhaIncorreta"))

                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index.html?erro"))
                .andExpect(unauthenticated());

        Usuario usuario = usuarios.findByLogin(LOGIN)
                .orElseThrow();

        assertEquals(1, usuario.tentativasFalhas);
        assertFalse(usuario.bloqueado);
    }

    @Test
    void armazenaSenhaComBCrypt() {

        Usuario usuario = usuarios.findByLogin(LOGIN)
                .orElseThrow();

        assertNotEquals(SENHA, usuario.senha);

        assertTrue(
                passwordEncoder.matches(
                        SENHA,
                        usuario.senha
                )
        );

        assertFalse(
                passwordEncoder.matches(
                        "SenhaIncorreta",
                        usuario.senha
                )
        );
    }

   @Test
void bloqueiaUsuarioAposCincoTentativas()
        throws Exception {

    for (int i = 1; i <= 5; i++) {

        mvc.perform(post("/login")
                .with(csrf())
                .param("username", LOGIN)
                .param("password", "SenhaIncorreta"))

                .andExpect(status().is3xxRedirection())
                .andExpect(unauthenticated());

        Usuario usuario = usuarios.findByLogin(LOGIN)
                .orElseThrow();

        assertEquals(i, usuario.tentativasFalhas);

        if (i < 5) {
            assertFalse(usuario.bloqueado);
        } else {
            assertTrue(usuario.bloqueado);
        }
    }

    // Mesmo com a senha correta, o acesso
    // deve continuar bloqueado.

    mvc.perform(post("/login")
            .with(csrf())
            .param("username", LOGIN)
            .param("password", SENHA))

            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/index.html?erro"))
            .andExpect(unauthenticated());
}

    @Test
    void zeraTentativasAposLoginCorreto()
            throws Exception {

        for (int i = 0; i < 3; i++) {

            mvc.perform(post("/login")
                    .with(csrf())
                    .param("username", LOGIN)
                    .param("password", "SenhaIncorreta"))

                    .andExpect(unauthenticated());
        }

        Usuario antes = usuarios.findByLogin(LOGIN)
                .orElseThrow();

        assertEquals(3, antes.tentativasFalhas);

        mvc.perform(post("/login")
                .with(csrf())
                .param("username", LOGIN)
                .param("password", SENHA))

                .andExpect(authenticated().withUsername(LOGIN));

        Usuario depois = usuarios.findByLogin(LOGIN)
                .orElseThrow();

        assertEquals(0, depois.tentativasFalhas);
        assertFalse(depois.bloqueado);
    }

    @Test
    void impedeLoginDeUsuarioDesativado()
            throws Exception {

        Usuario usuario = usuarios.findByLogin(LOGIN)
                .orElseThrow();

        usuario.ativo = false;

        usuarios.save(usuario);

        mvc.perform(post("/login")
                .with(csrf())
                .param("username", LOGIN)
                .param("password", SENHA))

                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index.html?erro"))
                .andExpect(unauthenticated());
    }
}