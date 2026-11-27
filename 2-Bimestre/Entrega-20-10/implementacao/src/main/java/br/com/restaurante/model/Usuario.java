package br.com.restaurante.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    public Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    public String nome;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    public String login;

    @JsonIgnore
    @NotBlank
    @Column(nullable = false, length = 255)
    public String senha;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public Perfil perfil;

    @Column(nullable = false)
    public boolean ativo = true;

    @Column(name = "tentativas_falhas", nullable = false)
    public int tentativasFalhas = 0;

    @Column(nullable = false)
    public boolean bloqueado = false;

    @JsonIgnore
    @Column(
        name = "versao_seguranca",
        nullable = false,
        columnDefinition = "BIGINT DEFAULT 0"
    )
    public long versaoSeguranca = 0L;

    public Usuario() {
    }

    public void invalidarSessoes() {

        this.versaoSeguranca++;
    }
}