package br.com.restaurante.controller;

import br.com.restaurante.dto.CancelamentoPedidoRequest;
import br.com.restaurante.dto.PedidoResponse;

import br.com.restaurante.service.PedidoService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;


    public PedidoController(

            PedidoService pedidoService) {

        this.pedidoService = pedidoService;
    }


    /* =========================================
       CONSULTAR TODOS OS PEDIDOS
       ========================================= */

    @GetMapping
    public List<PedidoResponse> listarPedidos() {

        return pedidoService.listarTodos();
    }


    /* =========================================
       REGISTRAR ENTREGA
       ========================================= */

    @PatchMapping("/{id}/entregar")
    public PedidoResponse registrarEntrega(

            @PathVariable Long id) {

        return pedidoService.registrarEntrega(id);
    }


    /* =========================================
       CANCELAR PEDIDO - RN04
       ========================================= */

    @PatchMapping("/{id}/cancelar")
    public PedidoResponse cancelarPedido(

            @PathVariable Long id,

            @Valid
            @RequestBody CancelamentoPedidoRequest dados,

            Authentication authentication) {


        if (
            authentication == null ||

            !authentication.isAuthenticated()
        ) {

            throw new ResponseStatusException(

                HttpStatus.UNAUTHORIZED,

                "Funcionario nao autenticado"
            );
        }


        return pedidoService.cancelarPedido(

            id,

            dados,

            authentication.getName()
        );
    }
}