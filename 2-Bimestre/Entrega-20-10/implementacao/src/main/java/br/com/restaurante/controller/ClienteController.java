
package br.com.restaurante.controller;

import br.com.restaurante.dto.ClienteLoginRequest;

import br.com.restaurante.model.Mesa;

import br.com.restaurante.service.MesaService;
import br.com.restaurante.service.AtendimentoClienteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority
        .SimpleGrantedAuthority;

import org.springframework.security.core.context
        .SecurityContext;

import org.springframework.security.core.context
        .SecurityContextHolder;

import org.springframework.security.web.context
        .HttpSessionSecurityContextRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    private final MesaService mesaService;

    private final AtendimentoClienteService
            atendimentoClienteService;

    private final HttpSessionSecurityContextRepository
            securityContextRepository =

                new HttpSessionSecurityContextRepository();


    public ClienteController(

            MesaService mesaService,

            AtendimentoClienteService
                atendimentoClienteService) {

        this.mesaService = mesaService;

        this.atendimentoClienteService =
                atendimentoClienteService;
    }


    /* =========================================
       LOGIN DO CLIENTE
       ========================================= */

    @PostMapping("/login")
    public Map<String, Object> login(

            @Valid @RequestBody ClienteLoginRequest dados,

            HttpServletRequest request,

            HttpServletResponse response) {

        Mesa mesa = mesaService.acessarComoCliente(

            dados.numeroMesa()
        );


        // Encerra qualquer sessao anterior.

        HttpSession anterior =
                request.getSession(false);

        if (anterior != null) {

            anterior.invalidate();
        }


        // Cria uma nova sessao para o cliente.

        HttpSession sessao =
                request.getSession(true);

        sessao.setMaxInactiveInterval(15 * 60);

        sessao.setAttribute(
            "mesaId",
            mesa.id
        );

        sessao.setAttribute(

            "codigoAtendimento",

            mesa.codigoAtendimento
        );


        // Define a autenticacao do cliente.

        Authentication authentication =

            new UsernamePasswordAuthenticationToken(

                "mesa_" + mesa.numero,

                null,

                List.of(

                    new SimpleGrantedAuthority(
                        "ROLE_CLIENTE"
                    )
                )
            );


        SecurityContext contexto =
            SecurityContextHolder.createEmptyContext();

        contexto.setAuthentication(authentication);

        SecurityContextHolder.setContext(contexto);

        securityContextRepository.saveContext(

            contexto,

            request,

            response
        );

        return dadosAtendimento(mesa);
    }


    /* =========================================
       CONSULTA DA SESSAO
       ========================================= */

    @GetMapping("/me")
    public Map<String, Object> consultarSessao(

            HttpServletRequest request) {

        Mesa mesa = atendimentoClienteService
            .validarAtendimento(request);

        return dadosAtendimento(mesa);
    }


    /* =========================================
       MONTAGEM DA RESPOSTA
       ========================================= */

    private Map<String, Object> dadosAtendimento(
            Mesa mesa) {

        return Map.of(

            "mesaId", mesa.id,

            "numeroMesa", mesa.numero,

            "perfil", "CLIENTE",

            "status", mesa.status.name()
        );
    }
}
