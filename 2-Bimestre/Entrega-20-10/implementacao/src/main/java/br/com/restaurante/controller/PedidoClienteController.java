
package br.com.restaurante.controller;

import br.com.restaurante.dto.PedidoRequest;
import br.com.restaurante.dto.PedidoResponse;

import br.com.restaurante.service.PedidoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cliente/pedidos")
public class PedidoClienteController {

    private final PedidoService pedidoService;


    public PedidoClienteController(
            PedidoService pedidoService) {

        this.pedidoService = pedidoService;
    }


    /* =========================================
       REALIZAR PEDIDO
       ========================================= */

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse realizarPedido(

            @Valid @RequestBody PedidoRequest dados,

            HttpServletRequest request) {


        Atendimento atendimento =
                obterAtendimento(request);


        return pedidoService.realizarPedido(

            atendimento.mesaId(),

            atendimento.codigoAtendimento(),

            dados
        );
    }


    /* =========================================
       LISTAR PEDIDOS DO CLIENTE
       ========================================= */

    @GetMapping
    public List<PedidoResponse> listarPedidos(

            HttpServletRequest request) {


        Atendimento atendimento =
                obterAtendimento(request);


        return pedidoService.listarPedidosCliente(

            atendimento.mesaId(),

            atendimento.codigoAtendimento()
        );
    }


    /* =========================================
       CONSULTAR PEDIDO ESPECIFICO
       ========================================= */

    @GetMapping("/{id}")
    public PedidoResponse buscarPedido(

            @PathVariable Long id,

            HttpServletRequest request) {


        Atendimento atendimento =
                obterAtendimento(request);


        return pedidoService.buscarPedidoCliente(

            id,

            atendimento.mesaId(),

            atendimento.codigoAtendimento()
        );
    }


    /* =========================================
       IDENTIFICACAO DO ATENDIMENTO
       ========================================= */

    private Atendimento obterAtendimento(

            HttpServletRequest request) {


        HttpSession sessao =
                request.getSession(false);


        if (sessao == null) {

            throw new ResponseStatusException(

                HttpStatus.UNAUTHORIZED,

                "Sessao inexistente"
            );
        }


        Object mesaId =
                sessao.getAttribute("mesaId");

        Object codigo =
                sessao.getAttribute(
                    "codigoAtendimento"
                );


        if (
            !(mesaId instanceof Long) ||

            !(codigo instanceof String) ||

            ((String) codigo).isBlank()
        ) {

            throw new ResponseStatusException(

                HttpStatus.UNAUTHORIZED,

                "Sessao de cliente invalida"
            );
        }


        return new Atendimento(

            (Long) mesaId,

            (String) codigo
        );
    }


    private record Atendimento(

        Long mesaId,

        String codigoAtendimento

    ) {
    }
}
