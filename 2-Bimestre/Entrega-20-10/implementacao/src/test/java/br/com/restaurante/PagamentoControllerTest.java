package br.com.restaurante;

import br.com.restaurante.dto.*;
import br.com.restaurante.model.*;
import br.com.restaurante.repository.*;
import br.com.restaurante.service.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

import static org.springframework.security.test.web.servlet
    .request.SecurityMockMvcRequestPostProcessors.*;

import static org.springframework.test.web.servlet
    .request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet
    .result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PagamentoControllerTest {

    private static final String API = "/api/pagamentos";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    MesaRepository mesas;

    @Autowired
    CategoriaRepository categorias;

    @Autowired
    ProdutoRepository produtos;

    @Autowired
    ContaRepository contas;

    @Autowired
    PagamentoRepository pagamentos;

    @Autowired
    PedidoService pedidoService;

    @Autowired
    MesaService mesaService;

    @Autowired
    PagamentoService pagamentoService;

    private Mesa mesa;

    private Produto produto;

    private Long pedidoId;

    private String codigoAtendimento;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        criarFuncionario(Perfil.ADMINISTRADOR);
        criarFuncionario(Perfil.GERENTE);
        criarFuncionario(Perfil.ATENDENTE);

        Categoria categoria = new Categoria();

        categoria.nome = "Categoria financeira teste";
        categoria.ativo = true;

        categorias.saveAndFlush(categoria);

        produto = new Produto();

        produto.nome = "Produto financeiro teste";
        produto.preco = new BigDecimal("25.00");
        produto.categoria = categoria;
        produto.ativo = true;
        produto.controlaEstoque = false;

        produto = produtos.saveAndFlush(produto);

        mesa = new Mesa();

        mesa.numero = 850;
        mesa.capacidade = 4;
        mesa.status = StatusMesa.OCUPADA;

        codigoAtendimento =
            UUID.randomUUID().toString();

        mesa.codigoAtendimento = codigoAtendimento;

        mesa = mesas.saveAndFlush(mesa);

        pedidoId = criarPedido(4);
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private String login(String perfil) {

        return "financeiro_" +
            perfil.toLowerCase(java.util.Locale.ROOT);
    }


    private void criarFuncionario(Perfil perfil) {

        Usuario funcionario = new Usuario();

        funcionario.nome = "Financeiro " + perfil.name();

        funcionario.login = login(perfil.name());

        funcionario.senha =
            passwordEncoder.encode("SenhaTeste123");

        funcionario.perfil = perfil;
        funcionario.ativo = true;
        funcionario.bloqueado = false;

        usuarios.saveAndFlush(funcionario);
    }


    private Long criarPedido(int quantidade) {

        return pedidoService.realizarPedido(

            mesa.id,

            codigoAtendimento,

            new PedidoRequest(

                null,

                List.of(
                    new ItemPedidoRequest(
                        produto.id,
                        quantidade
                    )
                )
            )

        ).id();
    }


    private void entregarPedido() {

        pedidoService.iniciarPreparo(pedidoId);

        pedidoService.marcarPronto(pedidoId);

        pedidoService.registrarEntrega(pedidoId);
    }


    private void prepararFechamento() {

        entregarPedido();

        mesaService.solicitarPagamento(mesa.id);
    }


    private PagamentoRequest pagamento(
            String forma,
            String valor) {

        return new PagamentoRequest(

            FormaPagamento.valueOf(forma),

            new BigDecimal(valor)
        );
    }


    private FinalizacaoContaRequest dados(
            String desconto,
            String motivo,
            List<PagamentoRequest> registros) {

        return new FinalizacaoContaRequest(

            codigoAtendimento,

            new BigDecimal(desconto),

            motivo,

            registros
        );
    }


    private FinalizacaoContaRequest dadosPadrao() {

        return dados(

            "0.00",

            null,

            List.of(
                pagamento("PIX", "100.00")
            )
        );
    }


    private ResultActions finalizar(
            String perfil,
            FinalizacaoContaRequest requisicao)
            throws Exception {

        return mvc.perform(

            post(
                API + "/abertas/{mesaId}/finalizar",
                mesa.id
            )

            .with(
                user(login(perfil)).roles(perfil)
            )

            .with(csrf())

            .contentType(MediaType.APPLICATION_JSON)

            .content(
                objectMapper.writeValueAsString(
                    requisicao
                )
            )
        );
    }


    private ContaResponse criarContaFinalizada() {

        prepararFechamento();

        return pagamentoService.finalizarConta(

            mesa.id,

            dadosPadrao(),

            login("GERENTE")
        );
    }


    private ResultActions estornar(
            Long contaId,
            String perfil,
            String motivo)
            throws Exception {

        return mvc.perform(

            patch(
                API + "/contas/{id}/estornar",
                contaId
            )

            .with(
                user(login(perfil)).roles(perfil)
            )

            .with(csrf())

            .contentType(MediaType.APPLICATION_JSON)

            .content(
                objectMapper.writeValueAsString(
                    new EstornoContaRequest(motivo)
                )
            )
        );
    }


    /* =========================================
       GRUPO A - AUTENTICACAO E AUTORIZACAO
       ========================================= */

    @ParameterizedTest
    @ValueSource(strings = {
        "ADMINISTRADOR",
        "GERENTE",
        "ATENDENTE"
    })
    void permiteConsultaAosPerfisFinanceiros(
            String perfil) throws Exception {

        mvc.perform(

            get(API + "/resumo")

                .with(
                    user(login(perfil)).roles(perfil)
                )
        )

        .andExpect(status().isOk());
    }


    @ParameterizedTest
    @ValueSource(strings = {
        "COZINHEIRO",
        "ESTOQUISTA",
        "CLIENTE"
    })
    void bloqueiaPerfisNaoAutorizados(
            String perfil) throws Exception {

        mvc.perform(

            get(API + "/resumo")

                .with(
                    user("teste").roles(perfil)
                )
        )

        .andExpect(status().isForbidden());
    }


    @Test
    void exigeAutenticacao() throws Exception {

        mvc.perform(
            get(API + "/resumo")
        )

        .andExpect(status().is3xxRedirection());
    }


    @Test
    void bloqueiaEstornoPeloAtendente()
            throws Exception {

        estornar(
            999999L,
            "ATENDENTE",
            "Estorno indevido"
        )

        .andExpect(status().isForbidden());
    }


    @Test
    void exigeTokenCsrfNaFinalizacao()
            throws Exception {

        prepararFechamento();

        mvc.perform(

            post(
                API + "/abertas/{mesaId}/finalizar",
                mesa.id
            )

            .with(
                user(login("GERENTE"))
                    .roles("GERENTE")
            )

            .contentType(MediaType.APPLICATION_JSON)

            .content(
                objectMapper.writeValueAsString(
                    dadosPadrao()
                )
            )
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       GRUPO B - CONSULTAS DE CONTAS ABERTAS
       ========================================= */

    @Test
    void listaMesaEmAtendimento() throws Exception {

        mvc.perform(

            get(API + "/abertas")

                .with(
                    user(login("ATENDENTE"))
                        .roles("ATENDENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath(
                "$[?(@.mesaId == " + mesa.id + ")]"
            ).isNotEmpty()
        );
    }


    @Test
    void excluiMesasLivresEInativas()
            throws Exception {

        Mesa livre = new Mesa();

        livre.numero = 851;
        livre.capacidade = 4;
        livre.status = StatusMesa.LIVRE;

        mesas.saveAndFlush(livre);

        Mesa inativa = new Mesa();

        inativa.numero = 852;
        inativa.capacidade = 4;
        inativa.status = StatusMesa.INATIVA;

        mesas.saveAndFlush(inativa);

        mvc.perform(

            get(API + "/abertas")

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath(
                "$[?(@.mesaId == " + livre.id + ")]"
            ).isEmpty()
        )

        .andExpect(
            jsonPath(
                "$[?(@.mesaId == " + inativa.id + ")]"
            ).isEmpty()
        );
    }


    @Test
    void consultaContaComPedidoPendente()
            throws Exception {

        mvc.perform(

            get(API + "/abertas/{mesaId}", mesa.id)

                .with(
                    user(login("ATENDENTE"))
                        .roles("ATENDENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.numeroMesa").value(850)
        )

        .andExpect(
            jsonPath("$.subtotal").value(100.00)
        )

        .andExpect(
            jsonPath("$.pedidosPendentes").value(1)
        )

        .andExpect(
            jsonPath("$.podeFinalizar").value(false)
        );
    }


    @Test
    void rejeitaConsultaDeMesaInexistente()
            throws Exception {

        mvc.perform(

            get(API + "/abertas/{mesaId}", 999999L)

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isNotFound());
    }


    @Test
    void excluiPedidoCanceladoDoSubtotal()
            throws Exception {

        Long canceladoId = criarPedido(2);

        pedidoService.cancelarPedido(

            canceladoId,

            new CancelamentoPedidoRequest(
                "Cancelamento financeiro teste"
            ),

            login("GERENTE")
        );

        prepararFechamento();

        mvc.perform(

            get(API + "/abertas/{mesaId}", mesa.id)

                .with(
                    user(login("ATENDENTE"))
                        .roles("ATENDENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.valorOriginalPedidos")
                .value(150.00)
        )

        .andExpect(
            jsonPath("$.valorCancelado")
                .value(50.00)
        )

        .andExpect(
            jsonPath("$.subtotal").value(100.00)
        )

        .andExpect(
            jsonPath("$.pedidosCancelados").value(1)
        )

        .andExpect(
            jsonPath("$.podeFinalizar").value(true)
        );
    }


    /* =========================================
       GRUPO C - FINALIZACAO DE CONTAS
       ========================================= */

    @ParameterizedTest
    @ValueSource(strings = {
        "DINHEIRO",
        "PIX",
        "CARTAO_CREDITO",
        "CARTAO_DEBITO",
        "VALE_REFEICAO"
    })
    void finalizaComFormaDePagamento(
            String forma) throws Exception {

        prepararFechamento();

        finalizar(

            "ATENDENTE",

            dados(
                "0.00",
                null,
                List.of(
                    pagamento(forma, "100.00")
                )
            )
        )

        .andExpect(status().isCreated())

        .andExpect(
            jsonPath("$.status").value("FINALIZADA")
        )

        .andExpect(
            jsonPath("$.valorTotal").value(100.00)
        )

        .andExpect(
            jsonPath("$.valorPago").value(100.00)
        )

        .andExpect(
            jsonPath("$.pagamentos[0].formaPagamento")
                .value(forma)
        );
    }


    @Test
    void aceitaMultiplasFormasDePagamento()
            throws Exception {

        prepararFechamento();

        finalizar(

            "ATENDENTE",

            dados(

                "0.00",

                null,

                List.of(
                    pagamento("PIX", "60.00"),
                    pagamento("DINHEIRO", "40.00")
                )
            )
        )

        .andExpect(status().isCreated())

        .andExpect(
            jsonPath("$.pagamentos", hasSize(2))
        )

        .andExpect(
            jsonPath("$.valorPago").value(100.00)
        );
    }


    @ParameterizedTest
    @ValueSource(strings = {
        "99.99",
        "100.01"
    })
    void rejeitaSomaDiferenteDoTotal(
            String valor) throws Exception {

        prepararFechamento();

        finalizar(

            "ATENDENTE",

            dados(
                "0.00",
                null,
                List.of(
                    pagamento("PIX", valor)
                )
            )
        )

        .andExpect(status().isConflict());
    }


    @ParameterizedTest
    @ValueSource(strings = {
        "-1.00",
        "0.00",
        "100.001"
    })
    void rejeitaValorDePagamentoInvalido(
            String valor) throws Exception {

        prepararFechamento();

        finalizar(

            "GERENTE",

            dados(
                "0.00",
                null,
                List.of(
                    pagamento("PIX", valor)
                )
            )
        )

        .andExpect(status().isBadRequest());
    }


    @Test
    void rejeitaFechamentoComPedidoPendente()
            throws Exception {

        mesaService.solicitarPagamento(mesa.id);

        finalizar(
            "ATENDENTE",
            dadosPadrao()
        )

        .andExpect(status().isConflict());
    }


    @Test
    void exigeSolicitacaoDePagamento()
            throws Exception {

        entregarPedido();

        finalizar(
            "ATENDENTE",
            dadosPadrao()
        )

        .andExpect(status().isConflict());
    }


    @Test
    void rejeitaCodigoDeAtendimentoIncorreto()
            throws Exception {

        prepararFechamento();

        finalizar(

            "GERENTE",

            new FinalizacaoContaRequest(

                UUID.randomUUID().toString(),

                BigDecimal.ZERO,

                null,

                List.of(
                    pagamento("PIX", "100.00")
                )
            )
        )

        .andExpect(status().isConflict());
    }


    @Test
    void permiteFechamentoSemValorDevido()
            throws Exception {

        pedidoService.cancelarPedido(

            pedidoId,

            new CancelamentoPedidoRequest(
                "Cancelamento integral"
            ),

            login("GERENTE")
        );

        mesaService.solicitarPagamento(mesa.id);

        finalizar(

            "ATENDENTE",

            dados(
                "0.00",
                null,
                List.of()
            )
        )

        .andExpect(status().isCreated())

        .andExpect(
            jsonPath("$.valorTotal").value(0.00)
        )

        .andExpect(
            jsonPath("$.pagamentos", hasSize(0))
        );
    }


    /* =========================================
       GRUPO D - DESCONTOS
       ========================================= */

    @ParameterizedTest
    @CsvSource({
        "5.00,95.00",
        "10.00,90.00"
    })
    void permiteDescontoAoAtendente(
            String desconto,
            String total) throws Exception {

        prepararFechamento();

        finalizar(

            "ATENDENTE",

            dados(

                desconto,

                "Desconto promocional",

                List.of(
                    pagamento("PIX", total)
                )
            )
        )

        .andExpect(status().isCreated())

        .andExpect(
            jsonPath("$.valorTotal")
                .value(Double.parseDouble(total))
        );
    }


    @Test
    void impedeAtendenteDeUltrapassarDezPorCento()
            throws Exception {

        prepararFechamento();

        finalizar(

            "ATENDENTE",

            dados(
                "10.01",
                "Desconto especial",
                List.of(
                    pagamento("PIX", "89.99")
                )
            )
        )

        .andExpect(status().isForbidden());
    }


    @ParameterizedTest
    @ValueSource(strings = {
        "ADMINISTRADOR",
        "GERENTE"
    })
    void permiteDescontoSuperiorADezPorCento(
            String perfil) throws Exception {

        prepararFechamento();

        finalizar(

            perfil,

            dados(
                "20.00",
                "Desconto autorizado",
                List.of(
                    pagamento("PIX", "80.00")
                )
            )
        )

        .andExpect(status().isCreated())

        .andExpect(
            jsonPath("$.valorDesconto").value(20.00)
        )

        .andExpect(
            jsonPath("$.valorTotal").value(80.00)
        );
    }


    @ParameterizedTest
    @ValueSource(strings = {
        "-1.00",
        "100.01"
    })
    void rejeitaPercentualDeDescontoInvalido(
            String desconto) throws Exception {

        prepararFechamento();

        finalizar(

            "GERENTE",

            dados(
                desconto,
                "Desconto teste",
                List.of(
                    pagamento("PIX", "100.00")
                )
            )
        )

        .andExpect(status().isBadRequest());
    }


    @Test
    void exigeJustificativaDoDesconto()
            throws Exception {

        prepararFechamento();

        finalizar(

            "GERENTE",

            dados(
                "10.00",
                "",
                List.of(
                    pagamento("PIX", "90.00")
                )
            )
        )

        .andExpect(status().isBadRequest());
    }


    @Test
    void permiteDescontoIntegralAutorizado()
            throws Exception {

        prepararFechamento();

        finalizar(

            "GERENTE",

            dados(
                "100.00",
                "Cortesia autorizada",
                List.of()
            )
        )

        .andExpect(status().isCreated())

        .andExpect(
            jsonPath("$.valorDesconto").value(100.00)
        )

        .andExpect(
            jsonPath("$.valorTotal").value(0.00)
        )

        .andExpect(
            jsonPath("$.valorPago").value(0.00)
        );
    }


    @Test
    void arredondaDescontoParaCentavos()
            throws Exception {

        Long adicional = criarPedido(1);

        pedidoService.iniciarPreparo(adicional);
        pedidoService.marcarPronto(adicional);
        pedidoService.registrarEntrega(adicional);

        prepararFechamento();

        finalizar(

            "GERENTE",

            dados(
                "7.25",
                "Desconto fracionado",
                List.of(
                    pagamento("PIX", "115.94")
                )
            )
        )

        .andExpect(status().isCreated())

        .andExpect(
            jsonPath("$.subtotal").value(125.00)
        )

        .andExpect(
            jsonPath("$.valorDesconto").value(9.06)
        )

        .andExpect(
            jsonPath("$.valorTotal").value(115.94)
        );
    }


    /* =========================================
       GRUPO E - ESTORNOS
       ========================================= */

    @ParameterizedTest
    @ValueSource(strings = {
        "ADMINISTRADOR",
        "GERENTE"
    })
    void permiteEstornoAutorizado(
            String perfil) throws Exception {

        ContaResponse conta = criarContaFinalizada();

        estornar(

            conta.id(),

            perfil,

            "Cobranca incorreta"
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.status").value("ESTORNADA")
        )

        .andExpect(
            jsonPath("$.valorPago").value(100.00)
        )

        .andExpect(
            jsonPath("$.valorEstornado").value(100.00)
        )

        .andExpect(
            jsonPath("$.valorLiquido").value(0.00)
        )

        .andExpect(
            jsonPath("$.motivoEstorno")
                .value("Cobranca incorreta")
        )

        .andExpect(
            jsonPath("$.nomeResponsavelEstorno")
                .value("Financeiro " + perfil)
        )

        .andExpect(
            jsonPath("$.dataEstorno").isNotEmpty()
        )

        .andExpect(
            jsonPath("$.pagamentos", hasSize(1))
        );
    }


    @Test
    void rejeitaEstornoSemJustificativa()
            throws Exception {

        ContaResponse conta = criarContaFinalizada();

        estornar(
            conta.id(),
            "GERENTE",
            ""
        )

        .andExpect(status().isBadRequest());
    }


    @Test
    void rejeitaEstornoDeContaInexistente()
            throws Exception {

        estornar(
            999999L,
            "GERENTE",
            "Estorno teste"
        )

        .andExpect(status().isNotFound());
    }


    @Test
    void impedeEstornoDuplicado()
            throws Exception {

        ContaResponse conta = criarContaFinalizada();

        estornar(
            conta.id(),
            "GERENTE",
            "Primeiro estorno"
        )

        .andExpect(status().isOk());

        estornar(
            conta.id(),
            "GERENTE",
            "Segundo estorno"
        )

        .andExpect(status().isConflict());
    }


    /* =========================================
       GRUPO F - HISTORICO FINANCEIRO
       ========================================= */

    @Test
    void consultaHistoricoFinanceiro()
            throws Exception {

        ContaResponse conta = criarContaFinalizada();

        mvc.perform(

            get(API + "/contas")

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath(
                "$[?(@.id == " + conta.id() + ")]"
            ).isNotEmpty()
        );
    }


    @Test
    void consultaContaIndividual()
            throws Exception {

        ContaResponse conta = criarContaFinalizada();

        mvc.perform(

            get(API + "/contas/{id}", conta.id())

                .with(
                    user(login("ATENDENTE"))
                        .roles("ATENDENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.id").value(conta.id())
        )

        .andExpect(
            jsonPath("$.valorPago").value(100.00)
        );
    }


    @Test
    void filtraContasPorStatus()
            throws Exception {

        ContaResponse conta = criarContaFinalizada();

        mvc.perform(

            get(API + "/contas/status/FINALIZADA")

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath(
                "$[?(@.id == " + conta.id() + ")]"
            ).isNotEmpty()
        );

        mvc.perform(

            get(API + "/contas/status/ESTORNADA")

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath(
                "$[?(@.id == " + conta.id() + ")]"
            ).isEmpty()
        );
    }


    @Test
    void filtraContasPorMesa()
            throws Exception {

        ContaResponse conta = criarContaFinalizada();

        mvc.perform(

            get(
                API + "/contas/mesa/{mesaId}",
                mesa.id
            )

            .with(
                user(login("GERENTE"))
                    .roles("GERENTE")
            )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath(
                "$[?(@.id == " + conta.id() + ")]"
            ).isNotEmpty()
        );
    }


    @Test
    void filtraContasPorPeriodo()
            throws Exception {

        ContaResponse conta = criarContaFinalizada();

        LocalDateTime agora = LocalDateTime.now();

        mvc.perform(

            get(API + "/contas/periodo")

                .param(
                    "inicio",
                    agora.minusDays(1).toString()
                )

                .param(
                    "fim",
                    agora.plusDays(1).toString()
                )

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath(
                "$[?(@.id == " + conta.id() + ")]"
            ).isNotEmpty()
        );
    }


    @Test
    void rejeitaPeriodoInvalido()
            throws Exception {

        mvc.perform(

            get(API + "/contas/periodo")

                .param(
                    "inicio",
                    "2026-12-01T00:00:00"
                )

                .param(
                    "fim",
                    "2026-11-01T00:00:00"
                )

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isBadRequest());
    }


    @Test
    void calculaIndicadoresFinanceiros()
            throws Exception {

        ResumoPagamentosResponse antes =
            pagamentoService.obterResumo();

        prepararFechamento();

        ContaResponse conta =
            pagamentoService.finalizarConta(

                mesa.id,

                dados(
                    "10.00",
                    "Desconto teste",
                    List.of(
                        pagamento("PIX", "90.00")
                    )
                ),

                login("GERENTE")
            );

        mvc.perform(

            get(API + "/resumo")

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.valorBruto").value(

                antes.valorBruto()
                    .add(new BigDecimal("100.00"))
                    .doubleValue()
            )
        )

        .andExpect(
            jsonPath("$.valorDescontos").value(

                antes.valorDescontos()
                    .add(new BigDecimal("10.00"))
                    .doubleValue()
            )
        )

        .andExpect(
            jsonPath("$.valorRecebido").value(

                antes.valorRecebido()
                    .add(new BigDecimal("90.00"))
                    .doubleValue()
            )
        );

        estornar(

            conta.id(),

            "GERENTE",

            "Estorno dos indicadores"
        )

        .andExpect(status().isOk());

        mvc.perform(

            get(API + "/resumo")

                .with(
                    user(login("GERENTE"))
                        .roles("GERENTE")
                )
        )

        .andExpect(status().isOk())

        .andExpect(
            jsonPath("$.valorEstornado").value(

                antes.valorEstornado()
                    .add(new BigDecimal("90.00"))
                    .doubleValue()
            )
        )

        .andExpect(
            jsonPath("$.valorLiquido").value(
                antes.valorLiquido().doubleValue()
            )
        );
    }
}