package br.com.restaurante.service;

import br.com.restaurante.model.Usuario;

import org.springframework.security.core.authority
        .SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.User;

import java.util.List;

public class UsuarioAutenticado extends User {

    private static final long serialVersionUID = 1L;

    private final Long usuarioId;

    private final long versaoSeguranca;

    public UsuarioAutenticado(Usuario usuario) {

        super(

            usuario.login,

            usuario.senha,

            usuario.ativo,

            true,

            true,

            !usuario.bloqueado,

            List.of(
                new SimpleGrantedAuthority(
                    "ROLE_" + usuario.perfil.name()
                )
            )
        );

        this.usuarioId = usuario.id;

        this.versaoSeguranca = usuario.versaoSeguranca;
    }

    public Long getUsuarioId() {

        return usuarioId;
    }

    public long getVersaoSeguranca() {

        return versaoSeguranca;
    }
}