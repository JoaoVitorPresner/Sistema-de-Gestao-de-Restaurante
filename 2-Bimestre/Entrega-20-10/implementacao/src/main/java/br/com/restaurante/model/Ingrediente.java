package br.com.restaurante.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ingrediente")
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingrediente")
    public Long id;


    @NotBlank
    @Size(max = 100)
    @Column(
        nullable = false,
        unique = true,
        length = 100
    )
    public String nome;


    @Size(max = 255)
    @Column(length = 255)
    public String descricao;


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        name = "unidade_medida",
        nullable = false,
        length = 20
    )
    public UnidadeMedida unidadeMedida;


    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 12, fraction = 3)
    @Column(
        name = "estoque_fisico",
        nullable = false,
        precision = 15,
        scale = 3
    )
    public BigDecimal estoqueFisico =
            BigDecimal.ZERO;


    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 12, fraction = 3)
    @Column(
        name = "estoque_reservado",
        nullable = false,
        precision = 15,
        scale = 3
    )
    public BigDecimal estoqueReservado =
            BigDecimal.ZERO;


    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 12, fraction = 3)
    @Column(
        name = "estoque_minimo",
        nullable = false,
        precision = 15,
        scale = 3
    )
    public BigDecimal estoqueMinimo =
            BigDecimal.ZERO;


    @Column(nullable = false)
    public boolean ativo = true;


    @Version
    @Column(nullable = false)
    public Long versao;


    public Ingrediente() {
    }


    /* =========================================
       CALCULO DO ESTOQUE DISPONIVEL
       ========================================= */

    @Transient
    public BigDecimal getEstoqueDisponivel() {

        return estoqueFisico.subtract(
            estoqueReservado
        );
    }


    /* =========================================
       VERIFICACAO DE ESTOQUE BAIXO
       ========================================= */

    @Transient
    public boolean isEstoqueBaixo() {

        return getEstoqueDisponivel()
            .compareTo(estoqueMinimo) <= 0;
    }


    /* =========================================
       VERIFICACAO DE INTEGRIDADE - RN08
       ========================================= */

    @AssertTrue(
        message = "O estoque reservado nao pode superar o estoque fisico"
    )
    @Transient
    public boolean isSaldoValido() {

        if (
            estoqueFisico == null ||
            estoqueReservado == null
        ) {

            return false;
        }

        return estoqueFisico.signum() >= 0 &&

               estoqueReservado.signum() >= 0 &&

               estoqueReservado.compareTo(
                   estoqueFisico
               ) <= 0;
    }
}