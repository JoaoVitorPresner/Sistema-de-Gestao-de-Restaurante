package br.com.restaurante.service;

import br.com.restaurante.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TentativasLoginService {

    private static final int LIMITE_TENTATIVAS = 5;

    private final UsuarioRepository usuarios;

    public TentativasLoginService(
            UsuarioRepository usuarios) {

        this.usuarios = usuarios;
    }

    @Transactional
    public void registrarFalha(String login) {

        usuarios.buscarPorLoginParaAtualizacao(login)
                .ifPresent(usuario -> {

            if (usuario.ativo && !usuario.bloqueado) {

                usuario.tentativasFalhas++;

                if (usuario.tentativasFalhas >= LIMITE_TENTATIVAS) {

                    usuario.tentativasFalhas = LIMITE_TENTATIVAS;

                    usuario.bloqueado = true;

                    usuario.invalidarSessoes();
                }

                usuarios.save(usuario);
            }
        });
    }

    @Transactional
    public void registrarSucesso(String login) {

        usuarios.buscarPorLoginParaAtualizacao(login)
                .ifPresent(usuario -> {

            if (usuario.ativo && !usuario.bloqueado) {

                usuario.tentativasFalhas = 0;

                usuarios.save(usuario);
            }
        });
    }
}