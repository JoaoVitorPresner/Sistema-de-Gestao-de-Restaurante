package br.com.restaurante.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    public Long id;

    @JsonIgnore
    @NotNull
    @ManyToOne(
        optional = false,
        fetch = FetchType.LAZY
    )
    @JoinColumn(
        name = "id_conta",
        nullable = false
    )
    public Conta conta;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        name = "forma_pagamento",
        nullable = false,
        length = 30
    )
    public FormaPagamento formaPagamento;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 12, fraction = 2)
    @Column(
        nullable = false,
        precision = 14,
        scale = 2
    )
    public BigDecimal valor;

    @NotNull
    @Column(
        name = "data_hora",
        nullable = false
    )
    public LocalDateTime dataHora;

    public Pagamento() {
    }

}
