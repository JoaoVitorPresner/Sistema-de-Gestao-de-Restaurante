package br.com.restaurante;

import br.com.restaurante.dto.ItemPedidoRequest;
import br.com.restaurante.dto.PedidoRequest;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.Pedido;
import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.StatusMesa;
import br.com.restaurante.model.StatusPedido;
import br.com.restaurante.model.Usuario;

import br.com.restaurante.repository.MesaRepository;
import br.com.restaurante.repository.PedidoRepository;
import br.com.restaurante.repository.UsuarioRepository;

import br.com.restaurante.service.PedidoService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;

import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet
        .request.SecurityMockMvcRequestPostProcessors.*;

import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CancelamentoPedidoControllerTest {

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
    UsuarioRepository usuarios;

    @Autowired
    PedidoService pedidoService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EntityManager entityManager;


    private Long pedidoId;

    private Long mesaId;

    private String codigoAtendimento;

    private Long atendenteId;


    private static final String MOTIVO_VALIDO =
        "{\"motivo\":\"Solicitacao do cliente\"}";


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        Mesa mesa = mesas.findByNumero(600)

            .orElseGet(Mesa::new);

        mesa.numero = 600;

        mesa.capacidade = 4;

        mesa.status = StatusMesa.OCUPADA;

        mesa.codigoAtendimento =
            UUID.randomUUID().toString();

        mesas.saveAndFlush(mesa);

        mesaId = mesa.id;

        codigoAtendimento = mesa.codigoAtendimento;


        Categoria categoria = new Categoria();

        categoria.nome = "Categoria Cancelamento Teste";

        categoria.ativo = true;

        categorias.save(categoria);


        Produto produto = new Produto();

        produto.nome = "Produto Cancelamento Teste";

        produto.preco = new BigDecimal("25.00");

        produto.ativo = true;

        produto.categoria = categoria;

        produtos.saveAndFlush(produto);


        PedidoRequest dados = new PedidoRequest(

            "Observacao de teste",

            List.of(
                new ItemPedidoRequest(produto.id, 2)
            )
        );


        pedidoId = pedidoService.realizarPedido(

            mesaId,

            codigoAtendimento,

            dados

        ).id();


        Usuario atendente = cadastrarUsuario(

            "atendente_cancelamento",

            Perfil.ATENDENTE
        );

        atendenteId = atendente.id;


        cadastrarUsuario(

            "gerente_cancelamento",

            Perfil.GERENTE
        );


        cadastrarUsuario(

            "admin_cancelamento",

            Perfil.ADMINISTRADOR
        );
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Usuario cadastrarUsuario(

            String login,

            Perfil perfil) {


        Usuario usuario = usuarios.findByLogin(login)

            .orElseGet(Usuario::new);


        usuario.nome = "Funcionario " + login;

        usuario.login = login;

        usuario.senha =
            passwordEncoder.encode("SenhaTeste123");

        usuario.perfil = perfil;

        usuario.ativo = true;

        usuario.bloqueado = false;

        usuario.tentativasFalhas = 0;


        return usuarios.saveAndFlush(usuario);
    }


    private ResultActions cancelar(

            Long id,

            String login,

            String perfil,

            String corpo) throws Exception {


        return mvc.perform(

            patch("/api/pedidos/{id}/cancelar", id)

                .with(user(login).roles(perfil))

                .with(csrf())

                .contentType(MediaType.APPLICATION_JSON)

                .content(corpo)
        );
    }


    private ResultActions cancelarComoAtendente()
            throws Exception {

        return cancelar(

            pedidoId,

            "atendente_cancelamento",

            "ATENDENTE",

            MOTIVO_VALIDO
        );
    }


    private Pedido consultarPedido() {

        entityManager.flush();

        entityManager.clear();

        return pedidos.findById(pedidoId)

            .orElseThrow();
    }


    /* =========================================
       GRUPO A - ESTADOS DO PEDIDO
       ========================================= */

    // CT33

    @Test
    void permiteCancelarPedidoRecebido()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk())

            .andExpect(
                jsonPath("$.status").value("CANCELADO")
            );

        assertEquals(

            StatusPedido.CANCELADO,

            consultarPedido().status
        );
    }


    // CT34

    @Test
    void permiteCancelarPedidoEmPreparo()
            throws Exception {

        pedidoService.iniciarPreparo(pedidoId);

        cancelarComoAtendente()

            .andExpect(status().isOk())

            .andExpect(
                jsonPath("$.status").value("CANCELADO")
            );
    }


    // CT35

    @Test
    void permiteCancelarPedidoPronto()
            throws Exception {

        pedidoService.iniciarPreparo(pedidoId);

        pedidoService.marcarPronto(pedidoId);

        cancelarComoAtendente()

            .andExpect(status().isOk())

            .andExpect(
                jsonPath("$.status").value("CANCELADO")
            );
    }


    // CT36

    @Test
    void impedeCancelarPedidoEntregue()
            throws Exception {

        pedidoService.iniciarPreparo(pedidoId);

        pedidoService.marcarPronto(pedidoId);

        pedidoService.registrarEntrega(pedidoId);


        cancelarComoAtendente()

            .andExpect(status().isConflict());


        assertEquals(

            StatusPedido.ENTREGUE,

            consultarPedido().status
        );
    }


    // CT37

    @Test
    void impedeCancelarPedidoJaCancelado()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk());


        cancelar(

            pedidoId,

            "gerente_cancelamento",

            "GERENTE",

            "{\"motivo\":\"Outro motivo\"}"
        )

        .andExpect(status().isConflict());


        Pedido pedido = consultarPedido();

        assertEquals(

            "Solicitacao do cliente",

            pedido.motivoCancelamento
        );

        assertEquals(

            atendenteId,

            pedido.canceladoPor.id
        );
    }


    /* =========================================
       GRUPO B - PERMISSOES E AUTENTICACAO
       ========================================= */

    // CT38

    @Test
    void gerentePodeCancelarPedido()
            throws Exception {

        cancelar(

            pedidoId,

            "gerente_cancelamento",

            "GERENTE",

            MOTIVO_VALIDO
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.status").value("CANCELADO")
        );
    }


    // CT39

    @Test
    void administradorPodeCancelarPedido()
            throws Exception {

        cancelar(

            pedidoId,

            "admin_cancelamento",

            "ADMINISTRADOR",

            MOTIVO_VALIDO
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.status").value("CANCELADO")
        );
    }


    // CT40

    @Test
    void cozinheiroNaoPodeCancelarPedido()
            throws Exception {

        cancelar(

            pedidoId,

            "cozinheiro_teste",

            "COZINHEIRO",

            MOTIVO_VALIDO
        )

        .andExpect(status().isForbidden());
    }


    // CT41

    @Test
    void estoquistaNaoPodeCancelarPedido()
            throws Exception {

        cancelar(

            pedidoId,

            "estoquista_teste",

            "ESTOQUISTA",

            MOTIVO_VALIDO
        )

        .andExpect(status().isForbidden());
    }


    // CT42

    @Test
    void clienteNaoPodeCancelarPedido()
            throws Exception {

        cancelar(

            pedidoId,

            "mesa_600",

            "CLIENTE",

            MOTIVO_VALIDO
        )

        .andExpect(status().isForbidden());
    }


    // CT43

    @Test
    void impedeCancelamentoSemAutenticacao()
            throws Exception {

        mvc.perform(

            patch(
                "/api/pedidos/{id}/cancelar",
                pedidoId
            )

            .with(csrf())

            .contentType(MediaType.APPLICATION_JSON)

            .content(MOTIVO_VALIDO)
        )

        .andExpect(status().is3xxRedirection());
    }


    // CT44

    @Test
    void impedeCancelamentoSemCsrf()
            throws Exception {

        mvc.perform(

            patch(
                "/api/pedidos/{id}/cancelar",
                pedidoId
            )

            .with(
                user("atendente_cancelamento")
                    .roles("ATENDENTE")
            )

            .contentType(MediaType.APPLICATION_JSON)

            .content(MOTIVO_VALIDO)
        )

        .andExpect(status().isForbidden());
    }


    // CT45

    @Test
    void impedeFuncionarioInativoDeCancelar()
            throws Exception {

        Usuario funcionario = usuarios

            .findByLogin("atendente_cancelamento")

            .orElseThrow();

        funcionario.ativo = false;

        usuarios.saveAndFlush(funcionario);


        cancelarComoAtendente()

            .andExpect(status().isForbidden());
    }


    // CT46

    @Test
    void impedeFuncionarioBloqueadoDeCancelar()
            throws Exception {

        Usuario funcionario = usuarios

            .findByLogin("atendente_cancelamento")

            .orElseThrow();

        funcionario.bloqueado = true;

        usuarios.saveAndFlush(funcionario);


        cancelarComoAtendente()

            .andExpect(status().isForbidden());
    }


    /* =========================================
       GRUPO C - VALIDACAO DOS DADOS
       ========================================= */

    // CT47

    @Test
    void impedeCancelamentoSemMotivo()
            throws Exception {

        cancelar(

            pedidoId,

            "atendente_cancelamento",

            "ATENDENTE",

            "{}"
        )

        .andExpect(status().isBadRequest());
    }


    // CT48

    @Test
    void impedeMotivoVazio()
            throws Exception {

        cancelar(

            pedidoId,

            "atendente_cancelamento",

            "ATENDENTE",

            "{\"motivo\":\"\"}"
        )

        .andExpect(status().isBadRequest());
    }


    // CT49

    @Test
    void impedeMotivoComSomenteEspacos()
            throws Exception {

        cancelar(

            pedidoId,

            "atendente_cancelamento",

            "ATENDENTE",

            "{\"motivo\":\"     \"}"
        )

        .andExpect(status().isBadRequest());
    }


    // CT50

    @Test
    void impedeMotivoAcimaDoLimite()
            throws Exception {

        String motivo = "A".repeat(256);

        cancelar(

            pedidoId,

            "atendente_cancelamento",

            "ATENDENTE",

            "{\"motivo\":\"" + motivo + "\"}"
        )

        .andExpect(status().isBadRequest());
    }


    // CT51

    @Test
    void retornaErroParaPedidoInexistente()
            throws Exception {

        cancelar(

            Long.MAX_VALUE,

            "atendente_cancelamento",

            "ATENDENTE",

            MOTIVO_VALIDO
        )

        .andExpect(status().isNotFound());
    }


    /* =========================================
       GRUPO D - PERSISTENCIA E INTEGRACAO
       ========================================= */

    // CT52

    @Test
    void registraMotivoDataEResponsavel()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk())

            .andExpect(
                jsonPath("$.motivoCancelamento")
                    .value("Solicitacao do cliente")
            )

            .andExpect(
                jsonPath("$.dataCancelamento")
                    .isNotEmpty()
            )

            .andExpect(
                jsonPath("$.canceladoPor")
                    .value(
                        "Funcionario atendente_cancelamento"
                    )
            );


        Pedido pedido = consultarPedido();


        assertEquals(

            "Solicitacao do cliente",

            pedido.motivoCancelamento
        );

        assertNotNull(pedido.dataCancelamento);

        assertNotNull(pedido.canceladoPor);

        assertEquals(

            atendenteId,

            pedido.canceladoPor.id
        );
    }


    // CT53

    @Test
    void preservaItensEValorOriginal()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk())

            .andExpect(
                jsonPath("$.valorTotal").value(50.00)
            )

            .andExpect(
                jsonPath("$.itens", hasSize(1))
            )

            .andExpect(
                jsonPath("$.itens[0].quantidade")
                    .value(2)
            );


        Pedido pedido = consultarPedido();


        assertEquals(

            0,

            new BigDecimal("50.00")

                .compareTo(pedido.valorTotal)
        );

        assertEquals(1, pedido.itens.size());

        assertEquals(

            2,

            pedido.itens.get(0).quantidade
        );
    }


    // CT54

    @Test
    void preservaEstadoDaMesa()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk());


        entityManager.flush();

        entityManager.clear();


        Mesa mesa = mesas.findById(mesaId)

            .orElseThrow();


        assertEquals(

            StatusMesa.OCUPADA,

            mesa.status
        );

        assertEquals(

            codigoAtendimento,

            mesa.codigoAtendimento
        );
    }


    // CT55

    @Test
    void removePedidoCanceladoDaFilaDaCozinha()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk());


        mvc.perform(

            get("/api/cozinha/pedidos")

                .with(
                    user("cozinheiro_teste")
                        .roles("COZINHEIRO")
                )
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath(
                "$[?(@.id == " + pedidoId + ")]"
            ).isEmpty()
        );
    }


    // CT56

    @Test
    void mantemPedidoNoHistoricoAdministrativo()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk());


        entityManager.flush();

        entityManager.clear();


        mvc.perform(

            get("/api/pedidos")

                .with(
                    user("atendente_cancelamento")
                        .roles("ATENDENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(

            jsonPath(
                "$[?(@.id == " + pedidoId + ")].status",
                contains("CANCELADO")
            )
        )

        .andExpect(

            jsonPath(
                "$[?(@.id == " + pedidoId +
                ")].canceladoPor",

                contains(
                    "Funcionario atendente_cancelamento"
                )
            )
        );
    }


    // CT57

    @Test
    void clienteConsultaCancelamentoSemDadosDoFuncionario()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk());


        MockHttpSession sessao = new MockHttpSession();

        sessao.setAttribute("mesaId", mesaId);

        sessao.setAttribute(

            "codigoAtendimento",

            codigoAtendimento
        );


        mvc.perform(

            get(
                "/api/cliente/pedidos/{id}",
                pedidoId
            )

            .session(sessao)

            .with(
                user("mesa_600").roles("CLIENTE")
            )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.status").value("CANCELADO")
        )

        .andExpect(
            jsonPath("$.motivoCancelamento")
                .value("Solicitacao do cliente")
        )

        .andExpect(
            jsonPath("$.dataCancelamento").isNotEmpty()
        )

        .andExpect(
            jsonPath("$.canceladoPor").value(nullValue())
        )

        .andExpect(
            jsonPath("$.senha").doesNotExist()
        );
    }


    // CT58

    @Test
    void impedeRetomarOuEntregarPedidoCancelado()
            throws Exception {

        cancelarComoAtendente()

            .andExpect(status().isOk());


        mvc.perform(

            patch(
                "/api/cozinha/pedidos/{id}/iniciar",
                pedidoId
            )

            .with(
                user("cozinheiro_teste")
                    .roles("COZINHEIRO")
            )

            .with(csrf())
        )

        .andExpect(status().isConflict());


        mvc.perform(

            patch(
                "/api/cozinha/pedidos/{id}/pronto",
                pedidoId
            )

            .with(
                user("cozinheiro_teste")
                    .roles("COZINHEIRO")
            )

            .with(csrf())
        )

        .andExpect(status().isConflict());


        mvc.perform(

            patch(
                "/api/pedidos/{id}/entregar",
                pedidoId
            )

            .with(
                user("atendente_cancelamento")
                    .roles("ATENDENTE")
            )

            .with(csrf())
        )

        .andExpect(status().isConflict());


        assertEquals(

            StatusPedido.CANCELADO,

            consultarPedido().status
        );
    }
}