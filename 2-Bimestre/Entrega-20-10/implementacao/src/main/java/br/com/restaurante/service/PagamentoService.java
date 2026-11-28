package br.com.restaurante.service;

import br.com.restaurante.dto.*;

import br.com.restaurante.model.*;

import br.com.restaurante.repository.ContaRepository;
import br.com.restaurante.repository.MesaRepository;
import br.com.restaurante.repository.PedidoRepository;
import br.com.restaurante.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;

@Service
public class PagamentoService {

    private static final BigDecimal ZERO =
        new BigDecimal("0.00");

    private static final BigDecimal DEZ =
        new BigDecimal("10.00");

    private static final BigDecimal CEM =
        new BigDecimal("100.00");

    private static final BigDecimal VALOR_MAXIMO =
        new BigDecimal("999999999999.99");

    private final ContaRepository contas;

    private final MesaRepository mesas;

    private final PedidoRepository pedidos;

    private final UsuarioRepository usuarios;

    private final EntityManager entityManager;

    public PagamentoService(

        ContaRepository contas,

        MesaRepository mesas,

        PedidoRepository pedidos,

        UsuarioRepository usuarios,

        EntityManager entityManager

    ) {

        this.contas = contas;

        this.mesas = mesas;

        this.pedidos = pedidos;

        this.usuarios = usuarios;

        this.entityManager = entityManager;
    }


    /* =========================================
       CONSULTA DE CONTAS ABERTAS
       ========================================= */

    @Transactional(readOnly = true)
    public List<ContaAbertaResponse> listarContasAbertas() {

        return mesas.findAllByOrderByNumeroAsc()

            .stream()

            .filter(mesa ->
                mesa.status == StatusMesa.OCUPADA ||
                mesa.status ==
                    StatusMesa.AGUARDANDO_PAGAMENTO
            )

            .map(mesa -> {

                List<Pedido> pedidosAtendimento =
                    buscarPedidosAtendimento(mesa);

                return montarContaAberta(
                    mesa,
                    pedidosAtendimento
                );
            })

            .toList();
    }


    @Transactional(readOnly = true)
    public ContaAbertaResponse consultarContaAberta(
            Long mesaId) {

        Mesa mesa = buscarMesa(mesaId);

        validarMesaEmAtendimento(mesa);

        return montarContaAberta(

            mesa,

            buscarPedidosAtendimento(mesa)

        );
    }


    /* =========================================
       FINALIZACAO DA CONTA
       RN02, RN05, RN06 E RN07
       ========================================= */

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ContaResponse finalizarConta(

        Long mesaId,

        FinalizacaoContaRequest dados,

        String loginFuncionario

    ) {

        Usuario funcionario =
            buscarFuncionarioFinanceiro(
                loginFuncionario
            );

        validarDadosFinalizacao(dados);

        Mesa mesa =
            buscarMesaParaAtualizacao(mesaId);

        if (
            mesa.status !=
                StatusMesa.AGUARDANDO_PAGAMENTO
        ) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "A mesa deve estar aguardando pagamento"
            );
        }

        if (
            mesa.codigoAtendimento == null ||

            !mesa.codigoAtendimento.equals(
                dados.codigoAtendimento()
            )
        ) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "O atendimento informado nao corresponde ao atendimento atual"
            );
        }

        if (
            contas.existsByMesa_IdAndCodigoAtendimento(

                mesa.id,

                mesa.codigoAtendimento

            )
        ) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Este atendimento ja possui uma conta registrada"
            );
        }


        List<Pedido> pedidosAtendimento =
            buscarPedidosParaFechamento(mesa);


        boolean possuiPedidosPendentes =
            pedidosAtendimento.stream()

                .anyMatch(pedido ->
                    pedido.status != StatusPedido.ENTREGUE &&
                    pedido.status != StatusPedido.CANCELADO
                );


        if (possuiPedidosPendentes) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Todos os pedidos devem estar entregues ou cancelados"
            );
        }


        BigDecimal subtotal =
            calcularSubtotal(pedidosAtendimento);

        validarLimiteMonetario(subtotal);


        BigDecimal percentualDesconto =
            validarDesconto(
                dados,
                funcionario
            );


        BigDecimal valorDesconto =
            subtotal

                .multiply(percentualDesconto)

                .divide(
                    CEM,
                    2,
                    RoundingMode.HALF_UP
                );


        BigDecimal valorTotal =
            subtotal.subtract(valorDesconto);


        BigDecimal valorPago =
            calcularTotalPagamentos(
                dados.pagamentos()
            );


        if (valorPago.compareTo(valorTotal) != 0) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "A soma dos pagamentos deve corresponder ao valor total da conta"
            );
        }


        LocalDateTime agora =
            LocalDateTime.now();


        Conta conta = new Conta();

        conta.mesa = mesa;

        conta.numeroMesa = mesa.numero;

        conta.codigoAtendimento =
            mesa.codigoAtendimento;

        conta.subtotal = subtotal;

        conta.percentualDesconto =
            percentualDesconto;

        conta.valorDesconto = valorDesconto;

        conta.motivoDesconto =
            percentualDesconto.signum() > 0
                ? dados.motivoDesconto().strip()
                : null;

        conta.valorTotal = valorTotal;

        conta.valorPago = valorPago;

        conta.status = StatusConta.FINALIZADA;

        conta.dataFinalizacao = agora;

        conta.finalizadoPor = funcionario;

        conta.nomeResponsavel = funcionario.nome;


        for (
            PagamentoRequest registro :
                dados.pagamentos()
        ) {

            Pagamento pagamento =
                new Pagamento();

            pagamento.formaPagamento =
                registro.formaPagamento();

            pagamento.valor =
                registro.valor().setScale(2);

            pagamento.dataHora = agora;

            conta.adicionarPagamento(
                pagamento
            );
        }


        Conta salva =
            contas.saveAndFlush(conta);


        /* =====================================
           ENCERRAMENTO DO ATENDIMENTO
           ===================================== */

        mesa.status = StatusMesa.LIVRE;

        mesa.codigoAtendimento = null;

        mesas.saveAndFlush(mesa);


        return ContaResponse.de(salva);
    }


    /* =========================================
       CONSULTAS DO HISTORICO FINANCEIRO
       ========================================= */

    @Transactional(readOnly = true)
    public List<ContaResponse> listarContas() {

        return contas

            .findAllByOrderByDataFinalizacaoDescIdDesc()

            .stream()

            .map(ContaResponse::de)

            .toList();
    }


    @Transactional(readOnly = true)
    public List<ContaResponse> listarContasPorStatus(
            StatusConta status) {

        if (status == null) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Status da conta obrigatorio"
            );
        }

        return contas

            .findByStatusOrderByDataFinalizacaoDescIdDesc(
                status
            )

            .stream()

            .map(ContaResponse::de)

            .toList();
    }


    @Transactional(readOnly = true)
    public List<ContaResponse> listarContasPorMesa(
            Long mesaId) {

        buscarMesa(mesaId);

        return contas

            .findByMesa_IdOrderByDataFinalizacaoDescIdDesc(
                mesaId
            )

            .stream()

            .map(ContaResponse::de)

            .toList();
    }


    @Transactional(readOnly = true)
    public List<ContaResponse> listarContasPorPeriodo(

        LocalDateTime inicio,

        LocalDateTime fim

    ) {

        if (
            inicio == null ||

            fim == null ||

            !inicio.isBefore(fim)
        ) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Periodo de consulta invalido"
            );
        }

        return contas

            .findByDataFinalizacaoGreaterThanEqualAndDataFinalizacaoLessThanOrderByDataFinalizacaoDescIdDesc(
                inicio,
                fim
            )

            .stream()

            .map(ContaResponse::de)

            .toList();
    }


    @Transactional(readOnly = true)
    public ContaResponse buscarConta(Long id) {

        Conta conta = contas.findById(id)

            .orElseThrow(() ->

                new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Conta nao encontrada"
                )
            );

        return ContaResponse.de(conta);
    }


    /* =========================================
       ESTORNO DE CONTA - RN03
       ========================================= */

    @Transactional
    public ContaResponse estornarConta(

        Long id,

        EstornoContaRequest dados,

        String loginFuncionario

    ) {

        Usuario funcionario =
            buscarFuncionarioFinanceiro(
                loginFuncionario
            );


        if (
            funcionario.perfil !=
                Perfil.ADMINISTRADOR &&

            funcionario.perfil !=
                Perfil.GERENTE
        ) {

            throw new ResponseStatusException(

                HttpStatus.FORBIDDEN,

                "Somente administradores e gerentes podem estornar contas"
            );
        }


        if (
            dados == null ||

            dados.motivo() == null ||

            dados.motivo().isBlank() ||

            dados.motivo().length() > 255
        ) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Informe um motivo de estorno valido"
            );
        }


        Conta conta =
            contas.buscarParaAtualizacao(id)

                .orElseThrow(() ->

                    new ResponseStatusException(

                        HttpStatus.NOT_FOUND,

                        "Conta nao encontrada"
                    )
                );


        entityManager.refresh(

            conta,

            LockModeType.PESSIMISTIC_WRITE
        );


        if (conta.status != StatusConta.FINALIZADA) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Somente contas finalizadas podem ser estornadas"
            );
        }


        conta.status = StatusConta.ESTORNADA;

        conta.motivoEstorno =
            dados.motivo().strip();

        conta.dataEstorno =
            LocalDateTime.now();

        conta.estornadoPor = funcionario;

        conta.nomeResponsavelEstorno =
            funcionario.nome;


        return ContaResponse.de(

            contas.saveAndFlush(conta)

        );
    }


    /* =========================================
       INDICADORES FINANCEIROS
       ========================================= */

    @Transactional(readOnly = true)
    public ResumoPagamentosResponse obterResumo() {

        List<Mesa> todasMesas =
            mesas.findAll();

        List<Conta> todasContas =
            contas.findAll();


        long mesasOcupadas =
            todasMesas.stream()

                .filter(mesa ->
                    mesa.status == StatusMesa.OCUPADA
                )

                .count();


        long mesasAguardandoPagamento =
            todasMesas.stream()

                .filter(mesa ->
                    mesa.status ==
                        StatusMesa.AGUARDANDO_PAGAMENTO
                )

                .count();


        long contasFinalizadas =
            todasContas.stream()

                .filter(conta ->
                    conta.status ==
                        StatusConta.FINALIZADA
                )

                .count();


        long contasEstornadas =
            todasContas.stream()

                .filter(conta ->
                    conta.status ==
                        StatusConta.ESTORNADA
                )

                .count();


        BigDecimal valorBruto =
            todasContas.stream()

                .map(conta -> conta.subtotal)

                .reduce(ZERO, BigDecimal::add);


        BigDecimal valorDescontos =
            todasContas.stream()

                .map(conta -> conta.valorDesconto)

                .reduce(ZERO, BigDecimal::add);


        BigDecimal valorRecebido =
            todasContas.stream()

                .map(conta -> conta.valorPago)

                .reduce(ZERO, BigDecimal::add);


        BigDecimal valorEstornado =
            todasContas.stream()

                .filter(conta ->
                    conta.status ==
                        StatusConta.ESTORNADA
                )

                .map(conta -> conta.valorPago)

                .reduce(ZERO, BigDecimal::add);


        BigDecimal valorLiquido =
            valorRecebido.subtract(valorEstornado);


        return new ResumoPagamentosResponse(

            mesasOcupadas,

            mesasAguardandoPagamento,

            contasFinalizadas,

            contasEstornadas,

            valorBruto,

            valorDescontos,

            valorRecebido,

            valorEstornado,

            valorLiquido

        );
    }


    /* =========================================
       MONTAGEM DA CONTA ABERTA
       ========================================= */

    private ContaAbertaResponse montarContaAberta(

        Mesa mesa,

        List<Pedido> pedidosAtendimento

    ) {

        long totalPedidos =
            pedidosAtendimento.size();


        long pedidosEntregues =
            pedidosAtendimento.stream()

                .filter(pedido ->
                    pedido.status ==
                        StatusPedido.ENTREGUE
                )

                .count();


        long pedidosCancelados =
            pedidosAtendimento.stream()

                .filter(pedido ->
                    pedido.status ==
                        StatusPedido.CANCELADO
                )

                .count();


        long pedidosPendentes =

            totalPedidos -

            pedidosEntregues -

            pedidosCancelados;


        BigDecimal valorOriginalPedidos =
            pedidosAtendimento.stream()

                .map(pedido -> pedido.valorTotal)

                .reduce(ZERO, BigDecimal::add);


        BigDecimal valorCancelado =
            pedidosAtendimento.stream()

                .filter(pedido ->
                    pedido.status ==
                        StatusPedido.CANCELADO
                )

                .map(pedido -> pedido.valorTotal)

                .reduce(ZERO, BigDecimal::add);


        BigDecimal subtotal =
            calcularSubtotal(pedidosAtendimento);


        String motivoBloqueio = null;


        if (
            mesa.codigoAtendimento == null ||

            mesa.codigoAtendimento.isBlank()
        ) {

            motivoBloqueio =
                "Mesa sem codigo de atendimento valido";

        } else if (
            mesa.status !=
                StatusMesa.AGUARDANDO_PAGAMENTO
        ) {

            motivoBloqueio =
                "Solicite o pagamento da mesa";

        } else if (pedidosPendentes > 0) {

            motivoBloqueio =
                "Existem pedidos pendentes";

        } else if (
            contas.existsByMesa_IdAndCodigoAtendimento(
                mesa.id,
                mesa.codigoAtendimento
            )
        ) {

            motivoBloqueio =
                "Atendimento ja possui conta registrada";
        }


        return new ContaAbertaResponse(

            mesa.id,

            mesa.numero,

            mesa.codigoAtendimento,

            mesa.status,

            totalPedidos,

            pedidosPendentes,

            pedidosEntregues,

            pedidosCancelados,

            valorOriginalPedidos,

            valorCancelado,

            subtotal,

            motivoBloqueio == null,

            motivoBloqueio,

            pedidosAtendimento.stream()

                .map(PedidoContaResponse::de)

                .toList()

        );
    }


    /* =========================================
       CONSULTAS AUXILIARES
       ========================================= */

    private Mesa buscarMesa(Long id) {

        return mesas.findById(id)

            .orElseThrow(() ->

                new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Mesa nao encontrada"
                )
            );
    }


    private Mesa buscarMesaParaAtualizacao(Long id) {

        Mesa mesa =
            mesas.buscarParaAtualizacao(id)

                .orElseThrow(() ->

                    new ResponseStatusException(

                        HttpStatus.NOT_FOUND,

                        "Mesa nao encontrada"
                    )
                );

        entityManager.refresh(

            mesa,

            LockModeType.PESSIMISTIC_WRITE
        );

        return mesa;
    }


    private List<Pedido> buscarPedidosAtendimento(
            Mesa mesa) {

        if (
            mesa.codigoAtendimento == null ||

            mesa.codigoAtendimento.isBlank()
        ) {

            return List.of();
        }

        return pedidos

            .findByMesa_IdAndCodigoAtendimentoOrderByDataHoraDesc(

                mesa.id,

                mesa.codigoAtendimento

            );
    }


    private List<Pedido> buscarPedidosParaFechamento(
            Mesa mesa) {

        return buscarPedidosAtendimento(mesa)

            .stream()

            .sorted(
                Comparator.comparing(pedido -> pedido.id)
            )

            .map(pedido -> {

                Pedido atualizado =
                    pedidos.buscarParaAtualizacao(
                        pedido.id
                    )

                    .orElseThrow(() ->

                        new ResponseStatusException(

                            HttpStatus.CONFLICT,

                            "Pedido do atendimento nao encontrado"
                        )
                    );

                entityManager.refresh(

                    atualizado,

                    LockModeType.PESSIMISTIC_WRITE
                );

                return atualizado;
            })

            .toList();
    }


    private void validarMesaEmAtendimento(
            Mesa mesa) {

        if (
            mesa.status != StatusMesa.OCUPADA &&

            mesa.status !=
                StatusMesa.AGUARDANDO_PAGAMENTO
        ) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "A mesa nao possui atendimento ativo"
            );
        }
    }


    /* =========================================
       CALCULOS FINANCEIROS
       ========================================= */

    private BigDecimal calcularSubtotal(
            List<Pedido> pedidosAtendimento) {

        return pedidosAtendimento.stream()

            .filter(pedido ->
                pedido.status !=
                    StatusPedido.CANCELADO
            )

            .map(pedido -> pedido.valorTotal)

            .reduce(ZERO, BigDecimal::add);
    }


    private BigDecimal validarDesconto(

        FinalizacaoContaRequest dados,

        Usuario funcionario

    ) {

        BigDecimal percentual =
            dados.percentualDesconto();

        if (
            percentual == null ||

            percentual.signum() < 0 ||

            percentual.compareTo(CEM) > 0 ||

            percentual.scale() > 2
        ) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Percentual de desconto invalido"
            );
        }


        if (
            percentual.compareTo(DEZ) > 0 &&

            funcionario.perfil !=
                Perfil.ADMINISTRADOR &&

            funcionario.perfil !=
                Perfil.GERENTE
        ) {

            throw new ResponseStatusException(

                HttpStatus.FORBIDDEN,

                "Descontos superiores a 10% exigem gerente ou administrador"
            );
        }


        if (
            percentual.signum() > 0 &&

            (
                dados.motivoDesconto() == null ||

                dados.motivoDesconto().isBlank()
            )
        ) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Informe o motivo do desconto"
            );
        }

        return percentual.setScale(2);
    }


    private BigDecimal calcularTotalPagamentos(
            List<PagamentoRequest> pagamentos) {

        BigDecimal total = ZERO;

        for (PagamentoRequest pagamento : pagamentos) {

            if (
                pagamento == null ||

                pagamento.formaPagamento() == null ||

                pagamento.valor() == null ||

                pagamento.valor().signum() <= 0 ||

                pagamento.valor().scale() > 2 ||

                pagamento.valor().compareTo(
                    VALOR_MAXIMO
                ) > 0
            ) {

                throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Forma ou valor de pagamento invalido"
                );
            }

            total = total.add(
                pagamento.valor()
            );
        }

        validarLimiteMonetario(total);

        return total.setScale(2);
    }


    private void validarLimiteMonetario(
            BigDecimal valor) {

        if (
            valor == null ||

            valor.signum() < 0 ||

            valor.scale() > 2 ||

            valor.compareTo(VALOR_MAXIMO) > 0
        ) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Valor monetario fora dos limites permitidos"
            );
        }
    }


    /* =========================================
       VALIDACAO DOS DADOS RECEBIDOS
       ========================================= */

    private void validarDadosFinalizacao(
            FinalizacaoContaRequest dados) {

        if (
            dados == null ||

            dados.codigoAtendimento() == null ||

            dados.codigoAtendimento().isBlank() ||

            dados.codigoAtendimento().length() > 36 ||

            dados.percentualDesconto() == null ||

            dados.pagamentos() == null ||

            dados.pagamentos().size() > 20 ||

            (
                dados.motivoDesconto() != null &&

                dados.motivoDesconto().length() > 255
            )
        ) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Dados de finalizacao invalidos"
            );
        }
    }


    /* =========================================
       AUTORIZACAO DO FUNCIONARIO
       ========================================= */

    private Usuario buscarFuncionarioFinanceiro(
            String loginFuncionario) {

        if (
            loginFuncionario == null ||

            loginFuncionario.isBlank()
        ) {

            throw new ResponseStatusException(

                HttpStatus.UNAUTHORIZED,

                "Funcionario nao autenticado"
            );
        }


        Usuario funcionario =
            usuarios.findByLogin(loginFuncionario)

                .orElseThrow(() ->

                    new ResponseStatusException(

                        HttpStatus.UNAUTHORIZED,

                        "Funcionario nao encontrado"
                    )
                );


        if (
            !funcionario.ativo ||

            funcionario.bloqueado
        ) {

            throw new ResponseStatusException(

                HttpStatus.FORBIDDEN,

                "Funcionario inativo ou bloqueado"
            );
        }


        if (
            funcionario.perfil !=
                Perfil.ADMINISTRADOR &&

            funcionario.perfil !=
                Perfil.GERENTE &&

            funcionario.perfil !=
                Perfil.ATENDENTE
        ) {

            throw new ResponseStatusException(

                HttpStatus.FORBIDDEN,

                "Funcionario sem permissao para operacoes financeiras"
            );
        }

        return funcionario;
    }

}