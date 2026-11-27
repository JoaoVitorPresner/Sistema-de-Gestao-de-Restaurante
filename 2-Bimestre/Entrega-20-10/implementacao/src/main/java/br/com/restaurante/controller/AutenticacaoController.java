
package br.com.restaurante.controller;

import br.com.restaurante.model.Usuario;
import br.com.restaurante.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final UsuarioRepository usuarios;

    public AutenticacaoController(
            UsuarioRepository usuarios) {

        this.usuarios = usuarios;
    }

    @GetMapping("/me")
    public Map<String, Object> usuarioAutenticado(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario nao autenticado"
            );
        }

        Usuario usuario = usuarios
            .findByLogin(authentication.getName())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario nao encontrado"
                )
            );

        return Map.of(
            "id", usuario.id,
            "nome", usuario.nome,
            "login", usuario.login,
            "perfil", usuario.perfil.name()
        );
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {

        return Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName(),
            "parameterName", token.getParameterName()
        );
    }
}
