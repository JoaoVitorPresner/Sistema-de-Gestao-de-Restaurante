package br.com.restaurante;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="categoria") public class Categoria {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_categoria") public Long id;
 @NotBlank @Size(max=100) @Column(nullable=false,length=100) public String nome;
 @Size(max=255) @Column(length=255) public String descricao;
 public Categoria() {}
}
