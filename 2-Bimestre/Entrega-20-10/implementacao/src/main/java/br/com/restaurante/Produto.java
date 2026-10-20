package br.com.restaurante;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
@Entity @Table(name="produto") public class Produto {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_produto") public Long id;
 @NotBlank @Size(max=100) @Column(nullable=false,length=100) public String nome;
 @Size(max=255) @Column(length=255) public String descricao;
 @NotNull @DecimalMin(value="0.01") @Digits(integer=8,fraction=2) @Column(nullable=false,precision=10,scale=2) public BigDecimal preco;
 @Column(nullable=false) public boolean ativo=true;
 @NotNull @ManyToOne(optional=false) @JoinColumn(name="id_categoria",nullable=false) public Categoria categoria;
 public Produto() {}
}
