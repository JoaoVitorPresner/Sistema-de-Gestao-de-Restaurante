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
    name = "conta",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_conta_atendimento",
            columnNames = {
                "id_mesa",
                "codigo_atendimento"
            }
        )
    },
    indexes = {
        @Index(
            name = "idx_conta_status",
            columnList = "status"
        ),
        @Index(
            name = "idx_conta_data_finalizacao",
            columnList = "data_finalizacao"
        )
    }
)
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta")
    public Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "id_mesa",
        nullable = false
    )
    public Mesa mesa;

    @NotNull
    @Min(1)
    @Column(
        name = "numero_mesa",
        nullable = false
    )
    public Integer numeroMesa;

    @JsonIgnore
    @NotBlank
    @Column(
        name = "codigo_atendimento",
        nullable = false,
        length = 36
    )
    public String codigoAtendimento;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    @Column(
        nullable = false,
        precision = 14,
        scale = 2
    )
    public BigDecimal subtotal = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Digits(integer = 3, fraction = 2)
    @Column(
        name = "percentual_desconto",
        nullable = false,
        precision = 5,
        scale = 2
    )
    public BigDecimal percentualDesconto =
        BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    @Column(
        name = "valor_desconto",
        nullable = false,
        precision = 14,
        scale = 2
    )
    public BigDecimal valorDesconto =
        BigDecimal.ZERO;

    @Size(max = 255)
    @Column(
        name = "motivo_desconto",
        length = 255
    )
    public String motivoDesconto;

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

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 12, fraction = 2)
    @Column(
        name = "valor_pago",
        nullable = false,
        precision = 14,
        scale = 2
    )
    public BigDecimal valorPago =
        BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        nullable = false,
        length = 20
    )
    public StatusConta status =
        StatusConta.FINALIZADA;

    @NotNull
    @Column(
        name = "data_finalizacao",
        nullable = false
    )
    public LocalDateTime dataFinalizacao;

    @JsonIgnore
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "id_usuario_finalizacao",
        nullable = false
    )
    public Usuario finalizadoPor;

    @NotBlank
    @Size(max = 100)
    @Column(
        name = "nome_responsavel",
        nullable = false,
        length = 100
    )
    public String nomeResponsavel;

    @Size(max = 255)
    @Column(
        name = "motivo_estorno",
        length = 255
    )
    public String motivoEstorno;

    @Column(name = "data_estorno")
    public LocalDateTime dataEstorno;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_usuario_estorno")
    public Usuario estornadoPor;

    @Size(max = 100)
    @Column(
        name = "nome_responsavel_estorno",
        length = 100
    )
    public String nomeResponsavelEstorno;

    @OneToMany(
        mappedBy = "conta",
        cascade = CascadeType.ALL
    )
    @OrderBy("id ASC")
    public List<Pagamento> pagamentos =
        new ArrayList<>();

    @Version
    @Column(nullable = false)
    public Long versao;

    public Conta() {
    }

    public void adicionarPagamento(
            Pagamento pagamento) {

        pagamento.conta = this;

        pagamentos.add(pagamento);
    }

}
