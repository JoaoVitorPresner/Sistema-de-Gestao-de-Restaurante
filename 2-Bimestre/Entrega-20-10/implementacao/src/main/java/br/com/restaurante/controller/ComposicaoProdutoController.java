package br.com.restaurante.controller;

import br.com.restaurante.Produto;

import br.com.restaurante.dto.ComposicaoProdutoRequest;
import br.com.restaurante.dto.ComposicaoProdutoResponse;
import br.com.restaurante.dto.ControleEstoqueRequest;

import br.com.restaurante.service.ComposicaoProdutoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estoque/composicoes")
public class ComposicaoProdutoController {

    private final ComposicaoProdutoService composicaoService;

    public ComposicaoProdutoController(

        ComposicaoProdutoService composicaoService

    ) {

        this.composicaoService = composicaoService;
    }


    /* =========================================
       CONSULTAR COMPOSICAO
       ========================================= */

    @GetMapping("/{produtoId}")
    public List<ComposicaoProdutoResponse> listarComposicao(

        @PathVariable Long produtoId

    ) {

        return composicaoService.listarComposicao(
            produtoId
        );
    }


    /* =========================================
       ADICIONAR INGREDIENTE
       ========================================= */

    @PostMapping("/{produtoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ComposicaoProdutoResponse adicionarIngrediente(

        @PathVariable Long produtoId,

        @Valid @RequestBody ComposicaoProdutoRequest dados

    ) {

        return composicaoService.adicionarIngrediente(

            produtoId,

            dados
        );
    }


    /* =========================================
       EDITAR QUANTIDADE
       ========================================= */

    @PutMapping("/{produtoId}/{ingredienteId}")
    public ComposicaoProdutoResponse editarIngrediente(

        @PathVariable Long produtoId,

        @PathVariable Long ingredienteId,

        @Valid @RequestBody ComposicaoProdutoRequest dados

    ) {

        return composicaoService.editarIngrediente(

            produtoId,

            ingredienteId,

            dados
        );
    }


    /* =========================================
       REMOVER INGREDIENTE
       ========================================= */

    @DeleteMapping("/{produtoId}/{ingredienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerIngrediente(

        @PathVariable Long produtoId,

        @PathVariable Long ingredienteId

    ) {

        composicaoService.removerIngrediente(

            produtoId,

            ingredienteId
        );
    }


    /* =========================================
       CONFIGURAR CONTROLE DE ESTOQUE
       ========================================= */

    @PatchMapping("/{produtoId}/controle")
    public Map<String, Object> configurarControleEstoque(

        @PathVariable Long produtoId,

        @Valid @RequestBody ControleEstoqueRequest dados

    ) {

        Produto produto =
            composicaoService.configurarControleEstoque(

                produtoId,

                dados.controlaEstoque()
            );

        return Map.of(

            "produtoId", produto.id,

            "controlaEstoque", produto.controlaEstoque
        );
    }
}