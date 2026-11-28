package br.com.restaurante.controller;

import br.com.restaurante.dto.ContaAbertaResponse;
import br.com.restaurante.dto.ContaResponse;
import br.com.restaurante.dto.FinalizacaoContaRequest;
import br.com.restaurante.dto.EstornoContaRequest;
import br.com.restaurante.dto.ResumoPagamentosResponse;

import br.com.restaurante.model.StatusConta;

import br.com.restaurante.service.PagamentoService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;


    public PagamentoController(
            PagamentoService pagamentoService) {

        this.pagamentoService = pagamentoService;
    }


    /* =========================================
       INDICADORES FINANCEIROS
       ========================================= */

    @GetMapping("/resumo")
    public ResumoPagamentosResponse obterResumo() {

        return pagamentoService.obterResumo();
    }


    /* =========================================
       CONTAS EM ABERTO
       ========================================= */

    @GetMapping("/abertas")
    public List<ContaAbertaResponse> listarContasAbertas() {

        return pagamentoService.listarContasAbertas();
    }


    @GetMapping("/abertas/{mesaId}")
    public ContaAbertaResponse consultarContaAberta(

        @PathVariable Long mesaId

    ) {

        return pagamentoService.consultarContaAberta(
            mesaId
        );
    }


    /* =========================================
       FINALIZACAO DA CONTA - RN02, RN06 E RN07
       ========================================= */

    @PostMapping("/abertas/{mesaId}/finalizar")
    @ResponseStatus(HttpStatus.CREATED)
    public ContaResponse finalizarConta(

        @PathVariable Long mesaId,

        @Valid @RequestBody FinalizacaoContaRequest dados,

        Authentication autenticacao

    ) {

        return pagamentoService.finalizarConta(

            mesaId,

            dados,

            autenticacao.getName()
        );
    }


    /* =========================================
       HISTORICO FINANCEIRO
       ========================================= */

    @GetMapping("/contas")
    public List<ContaResponse> listarContas() {

        return pagamentoService.listarContas();
    }


    @GetMapping("/contas/{id}")
    public ContaResponse buscarConta(

        @PathVariable Long id

    ) {

        return pagamentoService.buscarConta(id);
    }


    /* =========================================
       FILTRO POR STATUS
       ========================================= */

    @GetMapping("/contas/status/{status}")
    public List<ContaResponse> listarContasPorStatus(

        @PathVariable StatusConta status

    ) {

        return pagamentoService.listarContasPorStatus(
            status
        );
    }


    /* =========================================
       HISTORICO POR MESA
       ========================================= */

    @GetMapping("/contas/mesa/{mesaId}")
    public List<ContaResponse> listarContasPorMesa(

        @PathVariable Long mesaId

    ) {

        return pagamentoService.listarContasPorMesa(
            mesaId
        );
    }


    /* =========================================
       HISTORICO POR PERIODO
       ========================================= */

    @GetMapping("/contas/periodo")
    public List<ContaResponse> listarContasPorPeriodo(

        @RequestParam
        @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        )
        LocalDateTime inicio,

        @RequestParam
        @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE_TIME
        )
        LocalDateTime fim

    ) {

        return pagamentoService.listarContasPorPeriodo(

            inicio,

            fim
        );
    }


    /* =========================================
       ESTORNO DE CONTA - RN03
       ========================================= */

    @PatchMapping("/contas/{id}/estornar")
    public ContaResponse estornarConta(

        @PathVariable Long id,

        @Valid @RequestBody EstornoContaRequest dados,

        Authentication autenticacao

    ) {

        return pagamentoService.estornarConta(

            id,

            dados,

            autenticacao.getName()
        );
    }

}