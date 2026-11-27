package br.com.restaurante.service;

import br.com.restaurante.dto.UsuarioCadastroRequest;
import br.com.restaurante.dto.UsuarioEdicaoRequest;
import br.com.restaurante.dto.UsuarioSenhaRequest;
import br.com.restaurante.dto.UsuarioResponse;
import br.com.restaurante.dto.ResumoUsuariosResponse;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;
import br.com.restaurante.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder) {

        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    // ========================================
    // CONSULTAS
    // ========================================

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios(
            String loginResponsavel) {

        validarAdministrador(loginResponsavel);

        return usuarios.findAllByOrderByNomeAsc()
                .stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarUsuario(
            Long id,
            String loginResponsavel) {

        validarAdministrador(loginResponsavel);

        return UsuarioResponse.de(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public ResumoUsuariosResponse obterResumo(
            String loginResponsavel) {

        validarAdministrador(loginResponsavel);

        List<Usuario> registros = usuarios.findAll();

        long total = registros.size();

        long ativos = registros.stream()
                .filter(usuario -> usuario.ativo)
                .count();

        long inativos = total - ativos;

        long bloqueados = registros.stream()
                .filter(usuario -> usuario.bloqueado)
                .count();

        long administradoresDisponiveis = registros.stream()
                .filter(usuario ->
                        usuario.perfil == Perfil.ADMINISTRADOR
                        && usuario.ativo
                        && !usuario.bloqueado)
                .count();

        return new ResumoUsuariosResponse(
                total,
                ativos,
                inativos,
                bloqueados,
                administradoresDisponiveis
        );
    }

    // ========================================
    // CADASTRO
    // ========================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UsuarioResponse cadastrarUsuario(
            UsuarioCadastroRequest dados,
            String loginResponsavel) {

        bloquearAdministradores();
        validarAdministrador(loginResponsavel);

        String nome = normalizarNome(dados.nome());
        String login = normalizarLogin(dados.login());

        validarPerfil(dados.perfil());
        validarSenha(dados.senha());

        if (usuarios.existsByLoginIgnoreCase(login)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ja existe um usuario com este login."
            );
        }

        Usuario usuario = new Usuario();

        usuario.nome = nome;
        usuario.login = login;
        usuario.senha = passwordEncoder.encode(dados.senha());
        usuario.perfil = dados.perfil();

        usuario.ativo = true;
        usuario.bloqueado = false;
        usuario.tentativasFalhas = 0;

        return UsuarioResponse.de(
                usuarios.saveAndFlush(usuario)
        );
    }

    // ========================================
    // EDICAO CADASTRAL
    // ========================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UsuarioResponse editarUsuario(
            Long id,
            UsuarioEdicaoRequest dados,
            String loginResponsavel) {

        bloquearAdministradores();

        Usuario responsavel =
                validarAdministrador(loginResponsavel);

        Usuario usuario = buscarParaAtualizacao(id);

        String nome = normalizarNome(dados.nome());
        String login = normalizarLogin(dados.login());

        validarPerfil(dados.perfil());

        boolean propriaConta =
                Objects.equals(usuario.id, responsavel.id);

        if (propriaConta) {

            if (dados.perfil() != Perfil.ADMINISTRADOR) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Nao e permitido alterar o proprio perfil."
                );
            }

            if (!usuario.login.equals(login)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Nao e permitido alterar o proprio login."
                );
            }
        }

        if (usuarios.existsByLoginIgnoreCaseAndIdNot(
                login, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ja existe outro usuario com este login."
            );
        }

        if (usuario.perfil == Perfil.ADMINISTRADOR
                && dados.perfil() != Perfil.ADMINISTRADOR) {

            protegerUltimoAdministrador(usuario);
        }

        boolean alteracaoSeguranca =
                !Objects.equals(usuario.login, login)
                || usuario.perfil != dados.perfil();

        if (alteracaoSeguranca) {

            usuario.invalidarSessoes();
        }

        usuario.nome = nome;
        usuario.login = login;
        usuario.perfil = dados.perfil();

        return UsuarioResponse.de(
                usuarios.saveAndFlush(usuario)
        );
    }

    // ========================================
    // REDEFINICAO DE SENHA
    // ========================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UsuarioResponse redefinirSenha(
            Long id,
            UsuarioSenhaRequest dados,
            String loginResponsavel) {

        bloquearAdministradores();
        validarAdministrador(loginResponsavel);

        Usuario usuario = buscarParaAtualizacao(id);

        validarSenha(dados.senha());

        usuario.senha =
                passwordEncoder.encode(dados.senha());

        usuario.bloqueado = false;
        usuario.tentativasFalhas = 0;

        usuario.invalidarSessoes();

        return UsuarioResponse.de(
                usuarios.saveAndFlush(usuario)
        );
    }

    // ========================================
    // DESATIVACAO
    // ========================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UsuarioResponse desativarUsuario(
            Long id,
            String loginResponsavel) {

        bloquearAdministradores();

        Usuario responsavel =
                validarAdministrador(loginResponsavel);

        Usuario usuario = buscarParaAtualizacao(id);

        if (Objects.equals(usuario.id, responsavel.id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Nao e permitido desativar a propria conta."
            );
        }

        if (!usuario.ativo) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuario ja esta inativo."
            );
        }

        protegerUltimoAdministrador(usuario);

        usuario.ativo = false;

        usuario.invalidarSessoes();

        return UsuarioResponse.de(
                usuarios.saveAndFlush(usuario)
        );
    }

    // ========================================
    // REATIVACAO
    // ========================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UsuarioResponse ativarUsuario(
            Long id,
            String loginResponsavel) {

        bloquearAdministradores();
        validarAdministrador(loginResponsavel);

        Usuario usuario = buscarParaAtualizacao(id);

        if (usuario.ativo) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuario ja esta ativo."
            );
        }

        usuario.ativo = true;

        usuario.invalidarSessoes();

        return UsuarioResponse.de(
                usuarios.saveAndFlush(usuario)
        );
    }

    // ========================================
    // DESBLOQUEIO
    // ========================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UsuarioResponse desbloquearUsuario(
            Long id,
            String loginResponsavel) {

        bloquearAdministradores();
        validarAdministrador(loginResponsavel);

        Usuario usuario = buscarParaAtualizacao(id);

        if (!usuario.bloqueado
                && usuario.tentativasFalhas == 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuario nao possui bloqueio ou tentativas falhas."
            );
        }

        if (usuario.bloqueado) {

            usuario.invalidarSessoes();
        }

        usuario.bloqueado = false;
        usuario.tentativasFalhas = 0;

        return UsuarioResponse.de(
                usuarios.saveAndFlush(usuario)
        );
    }

    // ========================================
    // METODOS AUXILIARES
    // ========================================

    private Usuario buscarPorId(Long id) {

        return usuarios.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario nao encontrado."
                        ));
    }

    private Usuario buscarParaAtualizacao(Long id) {

        return usuarios.buscarParaAtualizacao(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario nao encontrado."
                        ));
    }

    private void bloquearAdministradores() {

        usuarios.bloquearUsuariosPorPerfil(
                Perfil.ADMINISTRADOR
        );
    }

    private Usuario validarAdministrador(String login) {

        if (login == null || login.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario nao autenticado."
            );
        }

        Usuario usuario = usuarios.findByLogin(login)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Usuario nao encontrado."
                        ));

        if (!usuario.ativo
                || usuario.bloqueado
                || usuario.perfil != Perfil.ADMINISTRADOR) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Operacao exclusiva de administradores ativos e desbloqueados."
            );
        }

        return usuario;
    }

    private void protegerUltimoAdministrador(
            Usuario usuario) {

        boolean administradorDisponivel =
                usuario.perfil == Perfil.ADMINISTRADOR
                && usuario.ativo
                && !usuario.bloqueado;

        if (!administradorDisponivel) {
            return;
        }

        long quantidade =
                usuarios.countByPerfilAndAtivoTrueAndBloqueadoFalse(
                        Perfil.ADMINISTRADOR
                );

        if (quantidade <= 1) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O sistema deve possuir pelo menos um administrador ativo e desbloqueado."
            );
        }
    }

    private String normalizarNome(String nome) {

        if (nome == null
                || nome.isBlank()
                || nome.trim().length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe um nome valido de ate 100 caracteres."
            );
        }

        return nome.trim();
    }

    private String normalizarLogin(String login) {

        if (login == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O login e obrigatorio."
            );
        }

        String normalizado =
                login.trim().toLowerCase(Locale.ROOT);

        if (!normalizado.matches("^[a-z0-9._-]{3,50}$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O login deve possuir entre 3 e 50 caracteres validos."
            );
        }

        return normalizado;
    }

    private void validarPerfil(Perfil perfil) {

        if (perfil == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O perfil e obrigatorio."
            );
        }
    }

    private void validarSenha(String senha) {

        if (senha == null
                || senha.isBlank()
                || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve possuir pelo menos 8 caracteres e no maximo 72 bytes."
            );
        }
    }
}