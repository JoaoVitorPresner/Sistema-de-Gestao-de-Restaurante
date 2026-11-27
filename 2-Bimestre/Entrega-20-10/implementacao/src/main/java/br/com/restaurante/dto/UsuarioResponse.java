package br.com.restaurante.dto;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;

public record UsuarioResponse(

    Long id,
    String nome,
    String login,
    Perfil perfil,
    boolean ativo,
    boolean bloqueado,
    int tentativasFalhas

) {

    public static UsuarioResponse de(Usuario usuario) {

        return new UsuarioResponse(
            usuario.id,
            usuario.nome,
            usuario.login,
            usuario.perfil,
            usuario.ativo,
            usuario.bloqueado,
            usuario.tentativasFalhas
        );
    }
}