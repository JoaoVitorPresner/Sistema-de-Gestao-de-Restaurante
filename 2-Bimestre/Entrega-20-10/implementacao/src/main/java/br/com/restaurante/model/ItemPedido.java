
package br.com.restaurante.model;

import br.com.restaurante.Produto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_pedido")
    public Long id;


    @JsonIgnore
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
        name = "id_produto",
        nullable = false
    )
    public Produto produto;


    @NotBlank
    @Size(max = 100)
    @Column(
        name = "nome_produto",
        nullable = false,
        length = 100
    )
    public String nomeProduto;


    @NotNull
    @Min(1)
    @Max(99)
    @Column(nullable = false)
    public Integer quantidade;


    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    @Column(
        name = "preco_unitario",
        nullable = false,
        precision = 10,
        scale = 2
    )
    public BigDecimal precoUnitario;


    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Column(
        nullable = false,
        precision = 12,
        scale = 2
    )
    public BigDecimal subtotal;


    public ItemPedido() {
    }


    /* =========================================
       CALCULO DO SUBTOTAL
       ========================================= */

    public void calcularSubtotal() {

        subtotal = precoUnitario.multiply(

            BigDecimal.valueOf(quantidade)
        );
    }
}
