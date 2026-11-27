package br.com.restaurante.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "mesa")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesa")
    public Long id;

    @NotNull
    @Min(1)
    @Column(nullable = false, unique = true)
    public Integer numero;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    public Integer capacidade;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public StatusMesa status = StatusMesa.LIVRE;

    @JsonIgnore
    @Column(name = "codigo_atendimento", length = 36)
    public String codigoAtendimento;

    @Version
    @Column(nullable = false)
    public Long versao;

    public Mesa() {
    }
}