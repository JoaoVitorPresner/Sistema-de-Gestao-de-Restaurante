package br.com.restaurante.dto;

import jakarta.validation.constraints.*;

public record UsuarioSenhaRequest(

    @NotBlank(message = "A nova senha e obrigatoria.")
    @Size(min = 8, max = 72,
          message = "A senha deve possuir entre 8 e 72 caracteres.")
    String senha

) {
}