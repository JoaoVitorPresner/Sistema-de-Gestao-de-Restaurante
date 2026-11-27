package br.com.restaurante.controller;

import br.com.restaurante.dto.IngredienteRequest;
import br.com.restaurante.dto.IngredienteResponse;

import br.com.restaurante.dto.MovimentacaoEstoqueRequest;
import br.com.restaurante.dto.MovimentacaoEstoqueResponse;

import br.com.restaurante.dto.ResumoEstoqueResponse;

import br.com.restaurante.model.TipoMovimentacao;

import br.com.restaurante.service.EstoqueService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(
        EstoqueService estoqueService
    ) {

        this.estoqueService = estoqueService;
    }


    /* =========================================
       INDICADORES DE ESTOQUE
       ========================================= */

    @GetMapping("/resumo")
    public ResumoEstoqueResponse obterResumo() {

        return estoqueService.obterResumo();
    }


    /* =========================================
       CONSULTA DE INGREDIENTES
       ========================================= */

    @GetMapping("/ingredientes")
    public List<IngredienteResponse> listarIngredientes() {

        return estoqueService.listarIngredientes();
    }


    @GetMapping("/ingredientes/{id}")
    public IngredienteResponse buscarIngrediente(
        @PathVariable Long id
    ) {

        return estoqueService.buscarIngrediente(id);
    }


    /* =========================================
       CADASTRO DE INGREDIENTES
       ========================================= */

    @PostMapping("/ingredientes")
    @ResponseStatus(HttpStatus.CREATED)
    public IngredienteResponse cadastrarIngrediente(

        @Valid @RequestBody IngredienteRequest dados

    ) {

        return estoqueService.cadastrarIngrediente(
            dados
        );
    }


    /* =========================================
       EDICAO DE INGREDIENTES
       ========================================= */

    @PutMapping("/ingredientes/{id}")
    public IngredienteResponse editarIngrediente(

        @PathVariable Long id,

        @Valid @RequestBody IngredienteRequest dados

    ) {

        return estoqueService.editarIngrediente(
            id,
            dados
        );
    }


    /* =========================================
       ATIVACAO E DESATIVACAO
       ========================================= */

    @PatchMapping("/ingredientes/{id}/ativar")
    public IngredienteResponse ativarIngrediente(
        @PathVariable Long id
    ) {

        return estoqueService.ativarIngrediente(id);
    }


    @PatchMapping("/ingredientes/{id}/desativar")
    public IngredienteResponse desativarIngrediente(
        @PathVariable Long id
    ) {

        return estoqueService.desativarIngrediente(id);
    }


    /* =========================================
       CONSULTA DE MOVIMENTACOES
       ========================================= */

    @GetMapping("/movimentacoes")
    public List<MovimentacaoEstoqueResponse>
        listarMovimentacoes() {

        return estoqueService.listarMovimentacoes();
    }


    @GetMapping("/movimentacoes/ingrediente/{id}")
    public List<MovimentacaoEstoqueResponse>
        listarMovimentacoesIngrediente(

            @PathVariable Long id

        ) {

        return estoqueService
            .listarMovimentacoesIngrediente(id);
    }


    @GetMapping("/movimentacoes/tipo/{tipo}")
    public List<MovimentacaoEstoqueResponse>
        listarMovimentacoesTipo(

            @PathVariable TipoMovimentacao tipo

        ) {

        return estoqueService
            .listarMovimentacoesTipo(tipo);
    }


    /* =========================================
       ENTRADAS E SAIDAS MANUAIS
       ========================================= */

    @PostMapping("/movimentacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoEstoqueResponse registrarMovimentacao(

        @Valid @RequestBody MovimentacaoEstoqueRequest dados,

        Authentication authentication

    ) {

        if (

            authentication == null ||

            !authentication.isAuthenticated()

        ) {

            throw new ResponseStatusException(

                HttpStatus.UNAUTHORIZED,

                "Funcionario nao autenticado"
            );
        }

        return estoqueService.registrarMovimentacao(

            dados,

            authentication.getName()
        );
    }
}