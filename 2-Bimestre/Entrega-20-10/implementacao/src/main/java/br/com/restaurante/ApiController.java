
package br.com.restaurante;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final CategoriaRepository categorias;
    private final ProdutoRepository produtos;

    public ApiController(
            CategoriaRepository categorias,
            ProdutoRepository produtos) {

        this.categorias = categorias;
        this.produtos = produtos;
    }


    /* =========================================
       GERENCIAMENTO DE CATEGORIAS
       ========================================= */


    @GetMapping("/categorias")
    public List<Categoria> categorias() {

        return categorias.findAllByOrderByNomeAsc();
    }


    @GetMapping("/categorias/{id}")
    public Categoria buscarCategoria(
            @PathVariable Long id) {

        return categorias.findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoria não encontrada"
                )
            );
    }


    @PostMapping("/categorias")
    public ResponseEntity<Categoria> cadastrarCategoria(
            @Valid @RequestBody Categoria dados) {

        Categoria categoria = new Categoria();

        categoria.nome = dados.nome.trim();
        categoria.descricao = dados.descricao;
        categoria.ativo = true;

        Categoria salva = categorias.save(categoria);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(salva);
    }


    @PutMapping("/categorias/{id}")
    @Transactional
    public Categoria editarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody Categoria dados) {

        Categoria categoria = buscarCategoria(id);

        categoria.nome = dados.nome.trim();
        categoria.descricao = dados.descricao;

        return categorias.save(categoria);
    }


    @DeleteMapping("/categorias/{id}")
    @Transactional
    public ResponseEntity<Void> desativarCategoria(
            @PathVariable Long id) {

        Categoria categoria = buscarCategoria(id);

        if (produtos.existsByCategoria_IdAndAtivoTrue(id)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A categoria possui produtos ativos. " +
                "Desative ou transfira esses produtos primeiro."
            );
        }

        categoria.ativo = false;

        categorias.save(categoria);

        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/categorias/{id}/ativar")
    @Transactional
    public Categoria ativarCategoria(
            @PathVariable Long id) {

        Categoria categoria = buscarCategoria(id);

        categoria.ativo = true;

        return categorias.save(categoria);
    }


    /* =========================================
       GERENCIAMENTO DE PRODUTOS
       ========================================= */


    @GetMapping("/produtos")
    public List<Produto> produtos() {

        return produtos.findAllByOrderByNomeAsc();
    }


    @GetMapping("/produtos/{id}")
    public Produto buscarProduto(
            @PathVariable Long id) {

        return produtos.findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Produto não encontrado"
                )
            );
    }


    @PostMapping("/produtos")
    @Transactional
    public ResponseEntity<Produto> cadastrarProduto(
            @Valid @RequestBody Produto dados) {

        Categoria categoria =
            validarCategoria(dados.categoria);

        exigirCategoriaAtiva(categoria);

        Produto produto = new Produto();

        produto.nome = dados.nome.trim();
        produto.descricao = dados.descricao;
        produto.preco = dados.preco;
        produto.categoria = categoria;
        produto.ativo = true;

        Produto salvo = produtos.save(produto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(salvo);
    }


    @PutMapping("/produtos/{id}")
    @Transactional
    public Produto editarProduto(
            @PathVariable Long id,
            @Valid @RequestBody Produto dados) {

        Produto produto = buscarProduto(id);

        Categoria categoria =
            validarCategoria(dados.categoria);

        if (produto.ativo) {
            exigirCategoriaAtiva(categoria);
        }

        produto.nome = dados.nome.trim();
        produto.descricao = dados.descricao;
        produto.preco = dados.preco;
        produto.categoria = categoria;

        return produtos.save(produto);
    }


    @DeleteMapping("/produtos/{id}")
    @Transactional
    public ResponseEntity<Void> desativarProduto(
            @PathVariable Long id) {

        Produto produto = buscarProduto(id);

        produto.ativo = false;

        produtos.save(produto);

        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/produtos/{id}/ativar")
    @Transactional
    public Produto ativarProduto(
            @PathVariable Long id) {

        Produto produto = buscarProduto(id);

        exigirCategoriaAtiva(produto.categoria);

        produto.ativo = true;

        return produtos.save(produto);
    }


    /* =========================================
       VALIDACOES INTERNAS
       ========================================= */


    private Categoria validarCategoria(
            Categoria dados) {

        if (dados == null || dados.id == null) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Categoria obrigatória"
            );
        }

        return categorias.findById(dados.id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Categoria inexistente"
                )
            );
    }


    private void exigirCategoriaAtiva(
            Categoria categoria) {

        if (!categoria.ativo) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A categoria selecionada está inativa"
            );
        }
    }


    /* =========================================
       TRATAMENTO DE EXCECOES
       ========================================= */


    @ExceptionHandler(
        MethodArgumentNotValidException.class
    )
    public ResponseEntity<Map<String, String>>
            tratarDadosInvalidos(
                MethodArgumentNotValidException ex) {

        return ResponseEntity.badRequest().body(
            Map.of(
                "erro",
                "Dados inválidos: confira os campos " +
                "obrigatórios e os valores informados."
            )
        );
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>>
            tratarErroOperacao(
                ResponseStatusException ex) {

        String mensagem = ex.getReason();

        if (mensagem == null) {
            mensagem = "Não foi possível realizar a operação.";
        }

        return ResponseEntity
            .status(ex.getStatusCode())
            .body(Map.of("erro", mensagem));
    }
}
