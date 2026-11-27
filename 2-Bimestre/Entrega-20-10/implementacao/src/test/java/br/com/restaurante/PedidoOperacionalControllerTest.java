
package br.com.restaurante;

import br.com.restaurante.dto.ItemPedidoRequest;
import br.com.restaurante.dto.PedidoRequest;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.StatusMesa;
import br.com.restaurante.model.StatusPedido;

import br.com.restaurante.repository.MesaRepository;
import br.com.restaurante.repository.PedidoRepository;

import br.com.restaurante.service.PedidoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet
        .request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoOperacionalControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    MesaRepository mesas;

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    ProdutoRepository produtos;

    @Autowired
    PedidoRepository pedidos;

    @Autowired
    PedidoService pedidoService;

    private Long pedidoId;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararPedido() {

        Mesa mesa = mesas.findByNumero(500)
            .orElseGet(Mesa::new);

        mesa.numero = 500;
        mesa.capacidade = 4;
        mesa.status = StatusMesa.OCUPADA;

        mesa.codigoAtendimento =
            UUID.randomUUID().toString();

        mesas.saveAndFlush(mesa);


        Categoria categoria = new Categoria();

        categoria.nome = "Categoria Operacional Teste";

        categoria.ativo = true;

        categorias.save(categoria);


        Produto produto = new Produto();

        produto.nome = "Produto Operacional Teste";

        produto.preco = new BigDecimal("25.00");

        produto.ativo = true;

        produto.categoria = categoria;

        produtos.saveAndFlush(produto);


        PedidoRequest dados = new PedidoRequest(

            "Observacao operacional",

            List.of(
                new ItemPedidoRequest(produto.id, 2)
            )
        );


        pedidoId = pedidoService.realizarPedido(

            mesa.id,

            mesa.codigoAtendimento,

            dados

        ).id();
    }


    /* =========================================
       CT21 - CONSULTA DA FILA
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void cozinheiroConsultaFila()
            throws Exception {

        mvc.perform(

            get("/api/cozinha/pedidos")
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath(
                "$[?(@.id == " + pedidoId + ")].status",
                contains("RECEBIDO")
            )
        );
    }


    /* =========================================
       CT22 - INICIAR PREPARO
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void cozinheiroIniciaPreparo()
            throws Exception {

        mvc.perform(

            patch(
                "/api/cozinha/pedidos/{id}/iniciar",
                pedidoId
            )

            .with(csrf())
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.status").value("EM_PREPARO")
        );

        assertEquals(

            StatusPedido.EM_PREPARO,

            pedidos.findById(pedidoId)
                .orElseThrow().status
        );
    }


    /* =========================================
       CT23 - FINALIZAR PREPARO
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void cozinheiroFinalizaPreparo()
            throws Exception {

        pedidoService.iniciarPreparo(pedidoId);

        mvc.perform(

            patch(
                "/api/cozinha/pedidos/{id}/pronto",
                pedidoId
            )

            .with(csrf())
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.status").value("PRONTO")
        );
    }


    /* =========================================
       CT24 - REGISTRAR ENTREGA
       ========================================= */

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void atendenteRegistraEntrega()
            throws Exception {

        pedidoService.iniciarPreparo(pedidoId);

        pedidoService.marcarPronto(pedidoId);

        mvc.perform(

            patch(
                "/api/pedidos/{id}/entregar",
                pedidoId
            )

            .with(csrf())
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.status").value("ENTREGUE")
        );

        assertEquals(

            StatusPedido.ENTREGUE,

            pedidos.findById(pedidoId)
                .orElseThrow().status
        );
    }


    /* =========================================
       CT25 - FINALIZACAO INDEVIDA
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void impedeFinalizacaoAntesDoPreparo()
            throws Exception {

        mvc.perform(

            patch(
                "/api/cozinha/pedidos/{id}/pronto",
                pedidoId
            )

            .with(csrf())
        )

        .andExpect(status().isConflict());
    }


    /* =========================================
       CT26 - ENTREGA INDEVIDA
       ========================================= */

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void impedeEntregaAntesDeEstarPronto()
            throws Exception {

        mvc.perform(

            patch(
                "/api/pedidos/{id}/entregar",
                pedidoId
            )

            .with(csrf())
        )

        .andExpect(status().isConflict());
    }


    /* =========================================
       CT27 - REINICIO INDEVIDO
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void impedeReinicioDePedidoEntregue()
            throws Exception {

        pedidoService.iniciarPreparo(pedidoId);

        pedidoService.marcarPronto(pedidoId);

        pedidoService.registrarEntrega(pedidoId);

        mvc.perform(

            patch(
                "/api/cozinha/pedidos/{id}/iniciar",
                pedidoId
            )

            .with(csrf())
        )

        .andExpect(status().isConflict());
    }


    /* =========================================
       CT28 - FILA DE PEDIDOS ATIVOS
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void naoExibePedidoEntregueNaFila()
            throws Exception {

        pedidoService.iniciarPreparo(pedidoId);

        pedidoService.marcarPronto(pedidoId);

        pedidoService.registrarEntrega(pedidoId);

        mvc.perform(

            get("/api/cozinha/pedidos")
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath(
                "$[?(@.id == " + pedidoId + ")]"
            ).isEmpty()
        );
    }


    /* =========================================
       CT29 - PERMISSAO DO ATENDENTE
       ========================================= */

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void impedeAtendenteDeIniciarPreparo()
            throws Exception {

        mvc.perform(

            patch(
                "/api/cozinha/pedidos/{id}/iniciar",
                pedidoId
            )

            .with(csrf())
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT30 - PERMISSAO DO COZINHEIRO
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void impedeCozinheiroDeRegistrarEntrega()
            throws Exception {

        pedidoService.iniciarPreparo(pedidoId);

        pedidoService.marcarPronto(pedidoId);

        mvc.perform(

            patch(
                "/api/pedidos/{id}/entregar",
                pedidoId
            )

            .with(csrf())
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT31 - PERMISSAO DO ESTOQUISTA
       ========================================= */

    @Test
    @WithMockUser(roles = "ESTOQUISTA")
    void impedeEstoquistaDeConsultarPedidosAdministrativos()
            throws Exception {

        mvc.perform(

            get("/api/pedidos")
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT32 - PROTECAO CSRF
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void impedeAlteracaoSemTokenCsrf()
            throws Exception {

        mvc.perform(

            patch(
                "/api/cozinha/pedidos/{id}/iniciar",
                pedidoId
            )
        )

        .andExpect(status().isForbidden());
    }
}
