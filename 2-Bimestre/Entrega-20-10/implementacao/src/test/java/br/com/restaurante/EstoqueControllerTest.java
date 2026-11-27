package br.com.restaurante;

import br.com.restaurante.model.Ingrediente;
import br.com.restaurante.model.UnidadeMedida;
import br.com.restaurante.model.Usuario;
import br.com.restaurante.model.Perfil;

import br.com.restaurante.repository.IngredienteRepository;
import br.com.restaurante.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(
    username = "estoquista_teste",
    roles = "ESTOQUISTA"
)
class EstoqueControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    IngredienteRepository ingredientes;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder passwordEncoder;

    private Ingrediente ingrediente;


    /* =========================================
       PREPARACAO DOS DADOS
       ========================================= */

    @BeforeEach
    void prepararDados() {

        Usuario funcionario = new Usuario();

        funcionario.nome = "Estoquista de teste";
        funcionario.login = "estoquista_teste";
        funcionario.senha =
            passwordEncoder.encode("SenhaTeste123");

        funcionario.perfil = Perfil.ESTOQUISTA;
        funcionario.ativo = true;
        funcionario.bloqueado = false;

        usuarios.saveAndFlush(funcionario);

        ingrediente = new Ingrediente();

        ingrediente.nome = "Carne bovina teste";
        ingrediente.descricao = "Ingrediente de teste";

        ingrediente.unidadeMedida =
            UnidadeMedida.GRAMA;

        ingrediente.estoqueFisico =
            new BigDecimal("1000.000");

        ingrediente.estoqueReservado =
            BigDecimal.ZERO;

        ingrediente.estoqueMinimo =
            new BigDecimal("200.000");

        ingrediente.ativo = true;

        ingrediente =
            ingredientes.saveAndFlush(ingrediente);
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private ResultActions movimentar(
        String tipo,
        String quantidade,
        String motivo
    ) throws Exception {

        String json = """
            {
                "ingredienteId": %d,
                "tipo": "%s",
                "quantidade": %s,
                "motivo": "%s"
            }
            """.formatted(
                ingrediente.id,
                tipo,
                quantidade,
                motivo
            );

        return mvc.perform(

            post("/api/estoque/movimentacoes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        );
    }


    private void verificarSaldo(
        String fisico,
        String reservado
    ) {

        Ingrediente atual = ingredientes
            .findById(ingrediente.id)
            .orElseThrow();

        assertEquals(
            0,
            new BigDecimal(fisico)
                .compareTo(atual.estoqueFisico)
        );

        assertEquals(
            0,
            new BigDecimal(reservado)
                .compareTo(atual.estoqueReservado)
        );
    }


    /* =========================================
       CT01 - CADASTRO
       ========================================= */

    @Test
    void cadastraIngredienteComSaldoZero()
            throws Exception {

        mvc.perform(

            post("/api/estoque/ingredientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Queijo teste",
                        "descricao": "Queijo",
                        "unidadeMedida": "GRAMA",
                        "estoqueMinimo": 100
                    }
                    """)
        )

        .andExpect(status().isCreated())

        .andExpect(jsonPath("$.nome")
            .value("Queijo teste"))

        .andExpect(jsonPath("$.estoqueFisico")
            .value(0))

        .andExpect(jsonPath("$.estoqueReservado")
            .value(0))

        .andExpect(jsonPath("$.ativo")
            .value(true));
    }


    /* =========================================
       CT02 - CONSULTA
       ========================================= */

    @Test
    void consultaIngredientePorId()
            throws Exception {

        mvc.perform(

            get(
                "/api/estoque/ingredientes/{id}",
                ingrediente.id
            )
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.nome")
            .value("Carne bovina teste"))

        .andExpect(jsonPath("$.estoqueDisponivel")
            .value(1000));
    }


    /* =========================================
       CT03 - LISTAGEM
       ========================================= */

    @Test
    void listaIngredientes() throws Exception {

        mvc.perform(
            get("/api/estoque/ingredientes")
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$").isArray())

        .andExpect(jsonPath("$[0].id").exists());
    }


    /* =========================================
       CT04 - NOME DUPLICADO
       ========================================= */

    @Test
    void rejeitaIngredienteDuplicado()
            throws Exception {

        mvc.perform(

            post("/api/estoque/ingredientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Carne bovina teste",
                        "unidadeMedida": "GRAMA",
                        "estoqueMinimo": 100
                    }
                    """)
        )

        .andExpect(status().isConflict());
    }


    /* =========================================
       CT05 - DADOS INVALIDOS
       ========================================= */

    @Test
    void rejeitaIngredienteInvalido()
            throws Exception {

        mvc.perform(

            post("/api/estoque/ingredientes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "",
                        "unidadeMedida": "GRAMA",
                        "estoqueMinimo": -100
                    }
                    """)
        )

        .andExpect(status().isBadRequest());
    }


    /* =========================================
       CT06 - EDICAO
       ========================================= */

    @Test
    void editaIngredienteSemAlterarSaldo()
            throws Exception {

        mvc.perform(

            put(
                "/api/estoque/ingredientes/{id}",
                ingrediente.id
            )
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "nome": "Carne atualizada",
                    "descricao": "Nova descricao",
                    "unidadeMedida": "GRAMA",
                    "estoqueMinimo": 300
                }
                """)
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.nome")
            .value("Carne atualizada"))

        .andExpect(jsonPath("$.estoqueMinimo")
            .value(300));

        verificarSaldo("1000", "0");
    }


    /* =========================================
       CT07 - UNIDADE DE MEDIDA
       ========================================= */

    @Test
    void impedeAlteracaoDeUnidadeComSaldo()
            throws Exception {

        mvc.perform(

            put(
                "/api/estoque/ingredientes/{id}",
                ingrediente.id
            )
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "nome": "Carne bovina teste",
                    "unidadeMedida": "QUILOGRAMA",
                    "estoqueMinimo": 1
                }
                """)
        )

        .andExpect(status().isConflict());

        verificarSaldo("1000", "0");
    }


    /* =========================================
       CT08 - ENTRADA DE ESTOQUE
       ========================================= */

    @Test
    void registraEntradaDeEstoque()
            throws Exception {

        movimentar(
            "ENTRADA",
            "500",
            "Recebimento de fornecedor"
        )

        .andExpect(status().isCreated())

        .andExpect(jsonPath("$.tipo")
            .value("ENTRADA"))

        .andExpect(jsonPath("$.fisicoAnterior")
            .value(1000))

        .andExpect(jsonPath("$.fisicoPosterior")
            .value(1500))

        .andExpect(jsonPath("$.responsavel")
            .value("Estoquista de teste"));

        verificarSaldo("1500", "0");
    }


    /* =========================================
       CT09 - SAIDA MANUAL
       ========================================= */

    @Test
    void registraSaidaManual()
            throws Exception {

        movimentar(
            "SAIDA_MANUAL",
            "300",
            "Ajuste de estoque"
        )

        .andExpect(status().isCreated())

        .andExpect(jsonPath("$.fisicoPosterior")
            .value(700));

        verificarSaldo("700", "0");
    }


    /* =========================================
       CT10 - RN08
       ESTOQUE NAO PODE FICAR NEGATIVO
       ========================================= */

    @Test
    void impedeSaidaSuperiorAoEstoque()
            throws Exception {

        movimentar(
            "SAIDA_MANUAL",
            "1001",
            "Saida invalida"
        )

        .andExpect(status().isConflict());

        verificarSaldo("1000", "0");
    }


    /* =========================================
       CT11 - PROTECAO DAS RESERVAS
       ========================================= */

    @Test
    void impedeSaidaDeQuantidadeReservada()
            throws Exception {

        ingrediente.estoqueReservado =
            new BigDecimal("800");

        ingredientes.saveAndFlush(ingrediente);

        movimentar(
            "SAIDA_MANUAL",
            "201",
            "Saida superior ao disponivel"
        )

        .andExpect(status().isConflict());

        verificarSaldo("1000", "800");
    }


    /* =========================================
       CT12 - SAIDA DO SALDO DISPONIVEL
       ========================================= */

    @Test
    void permiteSaidaDoSaldoNaoReservado()
            throws Exception {

        ingrediente.estoqueReservado =
            new BigDecimal("800");

        ingredientes.saveAndFlush(ingrediente);

        movimentar(
            "SAIDA_MANUAL",
            "200",
            "Saida do saldo disponivel"
        )

        .andExpect(status().isCreated());

        verificarSaldo("800", "800");
    }


    /* =========================================
       CT13 - QUANTIDADE ZERO
       ========================================= */

    @Test
    void rejeitaMovimentacaoComQuantidadeZero()
            throws Exception {

        movimentar(
            "ENTRADA",
            "0",
            "Entrada invalida"
        )

        .andExpect(status().isBadRequest());

        verificarSaldo("1000", "0");
    }


    /* =========================================
       CT14 - QUANTIDADE NEGATIVA
       ========================================= */

    @Test
    void rejeitaQuantidadeNegativa()
            throws Exception {

        movimentar(
            "ENTRADA",
            "-50",
            "Entrada invalida"
        )

        .andExpect(status().isBadRequest());

        verificarSaldo("1000", "0");
    }


    /* =========================================
       CT15 - JUSTIFICATIVA OBRIGATORIA
       ========================================= */

    @Test
    void rejeitaMovimentacaoSemJustificativa()
            throws Exception {

        movimentar(
            "ENTRADA",
            "100",
            ""
        )

        .andExpect(status().isBadRequest());
    }


    /* =========================================
       CT16 - MOVIMENTACAO AUTOMATICA
       ========================================= */

    @Test
    void impedeReservaManual()
            throws Exception {

        movimentar(
            "RESERVA",
            "100",
            "Reserva indevida"
        )

        .andExpect(status().isBadRequest());

        verificarSaldo("1000", "0");
    }


    /* =========================================
       CT17 - INGREDIENTE INATIVO
       ========================================= */

    @Test
    void impedeMovimentacaoDeIngredienteInativo()
            throws Exception {

        ingrediente.ativo = false;

        ingredientes.saveAndFlush(ingrediente);

        movimentar(
            "ENTRADA",
            "100",
            "Entrada indevida"
        )

        .andExpect(status().isConflict());

        verificarSaldo("1000", "0");
    }


    /* =========================================
       CT18 - DESATIVACAO
       ========================================= */

    @Test
    void desativaIngredienteSemExcluirRegistro()
            throws Exception {

        ingrediente.estoqueFisico =
            BigDecimal.ZERO;

        ingredientes.saveAndFlush(ingrediente);

        mvc.perform(

            patch(
                "/api/estoque/ingredientes/{id}/desativar",
                ingrediente.id
            )
            .with(csrf())
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.ativo")
            .value(false));

        assertTrue(
            ingredientes.existsById(ingrediente.id)
        );
    }


    /* =========================================
       CT19 - REATIVACAO
       ========================================= */

    @Test
    void reativaIngrediente()
            throws Exception {

        ingrediente.ativo = false;

        ingredientes.saveAndFlush(ingrediente);

        mvc.perform(

            patch(
                "/api/estoque/ingredientes/{id}/ativar",
                ingrediente.id
            )
            .with(csrf())
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.ativo")
            .value(true));
    }


    /* =========================================
       CT20 - HISTORICO
       ========================================= */

    @Test
    void consultaHistoricoDeMovimentacoes()
            throws Exception {

        movimentar(
            "ENTRADA",
            "500",
            "Compra de ingredientes"
        )

        .andExpect(status().isCreated());

        mvc.perform(

            get(
                "/api/estoque/movimentacoes/ingrediente/{id}",
                ingrediente.id
            )
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.length()")
            .value(1))

        .andExpect(jsonPath("$[0].tipo")
            .value("ENTRADA"))

        .andExpect(jsonPath("$[0].quantidade")
            .value(500));
    }


    /* =========================================
       CT21 - ESTOQUE BAIXO
       ========================================= */

    @Test
    void identificaEstoqueBaixo()
            throws Exception {

        ingrediente.estoqueFisico =
            new BigDecimal("100");

        ingredientes.saveAndFlush(ingrediente);

        mvc.perform(

            get(
                "/api/estoque/ingredientes/{id}",
                ingrediente.id
            )
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.estoqueBaixo")
            .value(true));
    }


    /* =========================================
       CT22 - INDICADORES
       ========================================= */

    @Test
    void consultaResumoDoEstoque()
            throws Exception {

        mvc.perform(
            get("/api/estoque/resumo")
        )

        .andExpect(status().isOk())

        .andExpect(jsonPath("$.totalIngredientes")
            .isNumber())

        .andExpect(jsonPath("$.ingredientesAtivos")
            .isNumber())

        .andExpect(jsonPath("$.ingredientesEstoqueBaixo")
            .isNumber());
    }


    /* =========================================
       CT23 - ADMINISTRADOR
       ========================================= */

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void permiteAdministradorConsultarEstoque()
            throws Exception {

        mvc.perform(
            get("/api/estoque/ingredientes")
        )

        .andExpect(status().isOk());
    }


    /* =========================================
       CT24 - GERENTE
       ========================================= */

    @Test
    @WithMockUser(roles = "GERENTE")
    void permiteGerenteConsultarEstoque()
            throws Exception {

        mvc.perform(
            get("/api/estoque/ingredientes")
        )

        .andExpect(status().isOk());
    }


    /* =========================================
       CT25 - ATENDENTE
       ========================================= */

    @Test
    @WithMockUser(roles = "ATENDENTE")
    void impedeAtendenteDeConsultarEstoque()
            throws Exception {

        mvc.perform(
            get("/api/estoque/ingredientes")
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT26 - COZINHEIRO
       ========================================= */

    @Test
    @WithMockUser(roles = "COZINHEIRO")
    void impedeCozinheiroDeConsultarEstoque()
            throws Exception {

        mvc.perform(
            get("/api/estoque/ingredientes")
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT27 - CLIENTE
       ========================================= */

    @Test
    @WithMockUser(roles = "CLIENTE")
    void impedeClienteDeConsultarEstoque()
            throws Exception {

        mvc.perform(
            get("/api/estoque/ingredientes")
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT28 - COMPOSICAO RESTRITA
       ========================================= */

    @Test
    void impedeEstoquistaDeConsultarComposicao()
            throws Exception {

        mvc.perform(
            get("/api/estoque/composicoes/1")
        )

        .andExpect(status().isForbidden());
    }


    /* =========================================
       CT29 - PROTECAO CSRF
       ========================================= */

    @Test
    void impedeCadastroSemCsrf()
            throws Exception {

        mvc.perform(

            post("/api/estoque/ingredientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "nome": "Ingrediente indevido",
                        "unidadeMedida": "GRAMA",
                        "estoqueMinimo": 100
                    }
                    """)
        )

        .andExpect(status().isForbidden());
    }
}