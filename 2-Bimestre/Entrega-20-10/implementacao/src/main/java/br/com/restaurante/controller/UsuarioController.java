package br.com.restaurante.controller;

import br.com.restaurante.dto.UsuarioCadastroRequest;
import br.com.restaurante.dto.UsuarioEdicaoRequest;
import br.com.restaurante.dto.UsuarioSenhaRequest;
import br.com.restaurante.dto.UsuarioResponse;
import br.com.restaurante.dto.ResumoUsuariosResponse;

import br.com.restaurante.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {

        this.usuarios = usuarios;
    }

    // ========================================
    // LISTAGEM DE USUARIOS
    // ========================================

    @GetMapping
    public List<UsuarioResponse> listarUsuarios(
            Authentication authentication) {

        return usuarios.listarUsuarios(
                authentication.getName()
        );
    }

    // ========================================
    // INDICADORES ADMINISTRATIVOS
    // ========================================

    @GetMapping("/resumo")
    public ResumoUsuariosResponse obterResumo(
            Authentication authentication) {

        return usuarios.obterResumo(
                authentication.getName()
        );
    }

    // ========================================
    // CONSULTA INDIVIDUAL
    // ========================================

    @GetMapping("/{id}")
    public UsuarioResponse buscarUsuario(
            @PathVariable Long id,
            Authentication authentication) {

        return usuarios.buscarUsuario(
                id,
                authentication.getName()
        );
    }

    // ========================================
    // CADASTRO
    // ========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrarUsuario(
            @Valid @RequestBody UsuarioCadastroRequest dados,
            Authentication authentication) {

        return usuarios.cadastrarUsuario(
                dados,
                authentication.getName()
        );
    }

    // ========================================
    // EDICAO
    // ========================================

    @PutMapping("/{id}")
    public UsuarioResponse editarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioEdicaoRequest dados,
            Authentication authentication) {

        return usuarios.editarUsuario(
                id,
                dados,
                authentication.getName()
        );
    }

    // ========================================
    // REDEFINICAO DE SENHA
    // ========================================

    @PatchMapping("/{id}/senha")
    public UsuarioResponse redefinirSenha(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioSenhaRequest dados,
            Authentication authentication) {

        return usuarios.redefinirSenha(
                id,
                dados,
                authentication.getName()
        );
    }

    // ========================================
    // DESATIVACAO
    // ========================================

    @PatchMapping("/{id}/desativar")
    public UsuarioResponse desativarUsuario(
            @PathVariable Long id,
            Authentication authentication) {

        return usuarios.desativarUsuario(
                id,
                authentication.getName()
        );
    }

    // ========================================
    // REATIVACAO
    // ========================================

    @PatchMapping("/{id}/ativar")
    public UsuarioResponse ativarUsuario(
            @PathVariable Long id,
            Authentication authentication) {

        return usuarios.ativarUsuario(
                id,
                authentication.getName()
        );
    }

    // ========================================
    // DESBLOQUEIO
    // ========================================

    @PatchMapping("/{id}/desbloquear")
    public UsuarioResponse desbloquearUsuario(
            @PathVariable Long id,
            Authentication authentication) {

        return usuarios.desbloquearUsuario(
                id,
                authentication.getName()
        );
    }
}