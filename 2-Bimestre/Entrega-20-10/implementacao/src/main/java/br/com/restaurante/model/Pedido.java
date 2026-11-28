
package br.com.restaurante.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "pedido",
    indexes = {
        @Index(
            name = "idx_pedido_atendimento",
            columnList = "id_mesa,codigo_atendimento"
        ),
        @Index(
            name = "idx_pedido_status",
            columnList = "status"
        )
    }
)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    public Long id;


    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "id_mesa",
        nullable = false
    )
    public Mesa mesa;


    @JsonIgnore
    @NotBlank
    @Column(
        name = "codigo_atendimento",
        nullable = false,
        length = 36
    )
    public String codigoAtendimento;


    @NotNull
    @Column(
        name = "data_hora",
        nullable = false
    )
    public LocalDateTime dataHora;


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 30
    )
    public StatusPedido status =
            StatusPedido.RECEBIDO;


    @Size(max = 500)
    @Column(length = 500)
    public String observacoes;

    /* =========================================
   INFORMACOES DO CANCELAMENTO
   ========================================= */

@Size(max = 255)
@Column(
    name = "motivo_cancelamento",
    length = 255
)
public String motivoCancelamento;


@Column(name = "data_cancelamento")
public LocalDateTime dataCancelamento;


@JsonIgnore
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "id_usuario_cancelamento")
public Usuario canceladoPor;


    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    @Column(
        name = "valor_total",
        nullable = false,
        precision = 14,
        scale = 2
    )
    public BigDecimal valorTotal =
            BigDecimal.ZERO;


    @OneToMany(
        mappedBy = "pedido",
        cascade = CascadeType.ALL
    )
    @OrderBy("id ASC")
    public List<ItemPedido> itens =
            new ArrayList<>();


    @Version
    @Column(nullable = false)
    public Long versao;


    public Pedido() {
    }


    /* =========================================
       ADICAO DE ITENS
       ========================================= */

    public void adicionarItem(ItemPedido item) {

        item.pedido = this;

        itens.add(item);

        recalcularTotal();
    }


    /* =========================================
       CALCULO DO VALOR TOTAL
       ========================================= */

    public void recalcularTotal() {

        valorTotal = itens.stream()

            .map(item -> item.subtotal)

            .reduce(
                BigDecimal.ZERO,
                BigDecimal::add
            );
    }
}
