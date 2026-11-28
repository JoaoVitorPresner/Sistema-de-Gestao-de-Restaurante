package br.com.restaurante.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "movimentacao_estoque",

    indexes = {

        @Index(
            name = "idx_movimentacao_ingrediente_data",
            columnList = "id_ingrediente,data_hora"
        )
    }
)
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimentacao")
    public Long id;


    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "id_ingrediente",
        nullable = false
    )
    public Ingrediente ingrediente;


    @ManyToOne
    @JoinColumn(name = "id_pedido")
    public Pedido pedido;


    @ManyToOne
    @JoinColumn(name = "id_usuario")
    public Usuario responsavel;


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 30
    )
    public TipoMovimentacao tipo;


    @NotNull
    @DecimalMin("0.001")
    @Digits(integer = 12, fraction = 3)
    @Column(
        nullable = false,
        precision = 15,
        scale = 3
    )
    public BigDecimal quantidade;


    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 12, fraction = 3)
    @Column(
        name = "fisico_anterior",
        nullable = false,
        precision = 15,
        scale = 3
    )
    public BigDecimal fisicoAnterior;


    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 12, fraction = 3)
    @Column(
        name = "fisico_posterior",
        nullable = false,
        precision = 15,
        scale = 3
    )
    public BigDecimal fisicoPosterior;


    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 12, fraction = 3)
    @Column(
        name = "reservado_anterior",
        nullable = false,
        precision = 15,
        scale = 3
    )
    public BigDecimal reservadoAnterior;


    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 12, fraction = 3)
    @Column(
        name = "reservado_posterior",
        nullable = false,
        precision = 15,
        scale = 3
    )
    public BigDecimal reservadoPosterior;


    @Size(max = 255)
    @Column(length = 255)
    public String motivo;


    @NotNull
    @Column(
        name = "data_hora",
        nullable = false
    )
    public LocalDateTime dataHora =
            LocalDateTime.now();


    public MovimentacaoEstoque() {
    }
}