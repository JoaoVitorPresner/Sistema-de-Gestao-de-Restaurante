package br.com.restaurante.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "reserva_estoque",

    uniqueConstraints = {

        @UniqueConstraint(
            name = "uk_reserva_pedido_ingrediente",
            columnNames = {
                "id_pedido",
                "id_ingrediente"
            }
        )
    },

    indexes = {

        @Index(
            name = "idx_reserva_status",
            columnList = "status"
        )
    }
)
public class ReservaEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    public Long id;


    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "id_pedido",
        nullable = false
    )
    public Pedido pedido;


    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "id_ingrediente",
        nullable = false
    )
    public Ingrediente ingrediente;


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
    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    public StatusReserva status =
            StatusReserva.RESERVADA;


    @NotNull
    @Column(
        name = "data_reserva",
        nullable = false
    )
    public LocalDateTime dataReserva =
            LocalDateTime.now();


    @Column(name = "data_finalizacao")
    public LocalDateTime dataFinalizacao;


    @Version
    @Column(nullable = false)
    public Long versao;


    public ReservaEstoque() {
    }
}