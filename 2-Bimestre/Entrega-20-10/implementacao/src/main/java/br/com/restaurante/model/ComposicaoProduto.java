package br.com.restaurante.model;

import br.com.restaurante.Produto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(
    name = "composicao_produto",
    uniqueConstraints = {

        @UniqueConstraint(
            name = "uk_composicao_produto_ingrediente",
            columnNames = {
                "id_produto",
                "id_ingrediente"
            }
        )
    }
)
public class ComposicaoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_composicao")
    public Long id;


    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "id_produto",
        nullable = false
    )
    public Produto produto;


    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "id_ingrediente",
        nullable = false
    )
    public Ingrediente ingrediente;


    @NotNull
    @DecimalMin("0.001")
    @Digits(integer = 9, fraction = 3)
    @Column(
        nullable = false,
        precision = 12,
        scale = 3
    )
    public BigDecimal quantidade;


    public ComposicaoProduto() {
    }
}