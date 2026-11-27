
package br.com.restaurante.controller;

import br.com.restaurante.dto.CategoriaCardapioDTO;

import br.com.restaurante.service.CardapioService;
import br.com.restaurante.service.AtendimentoClienteService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cliente")
public class CardapioController {

    private final CardapioService cardapioService;

    private final AtendimentoClienteService
            atendimentoClienteService;

    public CardapioController(

            CardapioService cardapioService,

            AtendimentoClienteService
                atendimentoClienteService) {

        this.cardapioService = cardapioService;

        this.atendimentoClienteService =
                atendimentoClienteService;
    }


    @GetMapping("/cardapio")
    public List<CategoriaCardapioDTO>
            consultarCardapio(
                HttpServletRequest request) {

        atendimentoClienteService
            .validarAtendimento(request);

        return cardapioService.listarCardapio();
    }
}
