package br.com.restaurante.config;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;
import br.com.restaurante.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class InicializadorDados implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final Environment ambiente;

    public InicializadorDados(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            Environment ambiente) {

        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.ambiente = ambiente;
    }

    @Override
    public void run(String... args) {

        criarUsuario(
                "Administrador",
                "admin",
                "ADMIN_PASSWORD",
                Perfil.ADMINISTRADOR
        );

        criarUsuario(
                "Gerente",
                "gerente",
                "GERENTE_PASSWORD",
                Perfil.GERENTE
        );

        criarUsuario(
                "Atendente",
                "atendente",
                "ATENDENTE_PASSWORD",
                Perfil.ATENDENTE
        );

        criarUsuario(
                "Cozinheiro",
                "cozinheiro",
                "COZINHEIRO_PASSWORD",
                Perfil.COZINHEIRO
        );

        criarUsuario(
                "Estoquista",
                "estoquista",
                "ESTOQUISTA_PASSWORD",
                Perfil.ESTOQUISTA
        );
    }

    private void criarUsuario(
            String nome,
            String login,
            String variavelSenha,
            Perfil perfil) {

        if (usuarios.existsByLogin(login)) {
            return;
        }

        String senha = ambiente.getProperty(variavelSenha);

        if (senha == null || senha.isBlank()) {
            return;
        }

        if (senha.length() < 8 ||
                senha.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new IllegalArgumentException(
                    "Senha inicial invalida para: " + login
            );
        }

        Usuario usuario = new Usuario();

        usuario.nome = nome;
        usuario.login = login;
        usuario.senha = passwordEncoder.encode(senha);
        usuario.perfil = perfil;
        usuario.ativo = true;
        usuario.bloqueado = false;
        usuario.tentativasFalhas = 0;

        usuarios.save(usuario);

        System.out.println("Usuario inicial criado: " + login);
    }
}