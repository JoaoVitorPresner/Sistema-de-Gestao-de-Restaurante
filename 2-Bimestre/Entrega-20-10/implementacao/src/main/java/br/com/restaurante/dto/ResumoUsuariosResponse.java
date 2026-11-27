package br.com.restaurante.dto;

public record ResumoUsuariosResponse(

    long totalUsuarios,
    long usuariosAtivos,
    long usuariosInativos,
    long usuariosBloqueados,
    long administradoresDisponiveis

) {
}