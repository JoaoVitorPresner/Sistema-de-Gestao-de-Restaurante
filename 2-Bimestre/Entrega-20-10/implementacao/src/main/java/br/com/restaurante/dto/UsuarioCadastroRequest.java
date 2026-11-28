package br.com.restaurante.dto;

import br.com.restaurante.model.Perfil;

import jakarta.validation.constraints.*;

public record UsuarioCadastroRequest(

    @NotBlank(message = "O nome e obrigatorio.")
    @Size(max = 100, message = "O nome deve possuir ate 100 caracteres.")
    String nome,

    @NotBlank(message = "O login e obrigatorio.")
    @Size(min = 3, max = 50,
          message = "O login deve possuir entre 3 e 50 caracteres.")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "O login possui caracteres invalidos."
    )
    String login,

    @NotBlank(message = "A senha e obrigatoria.")
    @Size(min = 8, max = 72,
          message = "A senha deve possuir entre 8 e 72 caracteres.")
    String senha,

    @NotNull(message = "O perfil e obrigatorio.")
    Perfil perfil

) {
}