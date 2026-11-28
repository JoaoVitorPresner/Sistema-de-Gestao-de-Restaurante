package br.com.restaurante.controller;

import br.com.restaurante.dto.MesaRequest;
import br.com.restaurante.model.Mesa;
import br.com.restaurante.service.MesaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    public List<Mesa> listar() {

        return mesaService.listar();
    }

    @GetMapping("/{id}")
    public Mesa buscar(@PathVariable Long id) {

        return mesaService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Mesa> cadastrar(
            @Valid @RequestBody MesaRequest dados) {

        Mesa mesa = mesaService.cadastrar(dados);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mesa);
    }

    @PutMapping("/{id}")
    public Mesa editar(
            @PathVariable Long id,
            @Valid @RequestBody MesaRequest dados) {

        return mesaService.editar(id, dados);
    }

    @PatchMapping("/{id}/ocupar")
    public Mesa ocupar(@PathVariable Long id) {

        return mesaService.ocupar(id);
    }

    @PatchMapping("/{id}/solicitar-pagamento")
    public Mesa solicitarPagamento(
            @PathVariable Long id) {

        return mesaService.solicitarPagamento(id);
    }

    @PatchMapping("/{id}/desativar")
    public Mesa desativar(@PathVariable Long id) {

        return mesaService.desativar(id);
    }

    @PatchMapping("/{id}/ativar")
    public Mesa ativar(@PathVariable Long id) {

        return mesaService.ativar(id);
    }
}