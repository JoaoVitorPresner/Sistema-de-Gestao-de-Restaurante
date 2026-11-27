
package br.com.restaurante.service;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.StatusMesa;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AtendimentoClienteService {

    private final MesaService mesaService;

    public AtendimentoClienteService(
            MesaService mesaService) {

        this.mesaService = mesaService;
    }


    /* =========================================
       VALIDACAO DO ATENDIMENTO
       ========================================= */

    public Mesa validarAtendimento(
            HttpServletRequest request) {

        HttpSession sessao =
                request.getSession(false);

        if (sessao == null) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Sessão inexistente"
            );
        }

        Object mesaId =
                sessao.getAttribute("mesaId");

        Object codigoAtendimento =
                sessao.getAttribute(
                    "codigoAtendimento"
                );

        if (!(mesaId instanceof Long) ||
            !(codigoAtendimento instanceof String)) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Sessão de cliente inválida"
            );
        }

        return mesaService.validarAtendimento(

            (Long) mesaId,

            (String) codigoAtendimento
        );
    }


    /* =========================================
       VALIDACAO PARA NOVOS PEDIDOS
       ========================================= */

    public Mesa validarNovoPedido(
            HttpServletRequest request) {

        Mesa mesa = validarAtendimento(request);

        if (mesa.status != StatusMesa.OCUPADA) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A mesa não está disponível " +
                "para novos pedidos"
            );
        }

        return mesa;
    }
}
