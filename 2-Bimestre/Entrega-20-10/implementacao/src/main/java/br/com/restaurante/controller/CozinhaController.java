
package br.com.restaurante.controller;

import br.com.restaurante.dto.PedidoResponse;

import br.com.restaurante.service.PedidoService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cozinha/pedidos")
public class CozinhaController {

    private final PedidoService pedidoService;


    public CozinhaController(

            PedidoService pedidoService) {

        this.pedidoService = pedidoService;
    }


    /* =========================================
       FILA DA COZINHA
       ========================================= */

    @GetMapping
    public List<PedidoResponse> listarFila() {

        return pedidoService.listarFilaCozinha();
    }


    /* =========================================
       INICIAR PREPARO
       ========================================= */

    @PatchMapping("/{id}/iniciar")
    public PedidoResponse iniciarPreparo(

            @PathVariable Long id) {

        return pedidoService.iniciarPreparo(id);
    }


    /* =========================================
       FINALIZAR PREPARO
       ========================================= */

    @PatchMapping("/{id}/pronto")
    public PedidoResponse marcarPronto(

            @PathVariable Long id) {

        return pedidoService.marcarPronto(id);
    }
}
