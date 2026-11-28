
package br.com.restaurante.service;

import br.com.restaurante.Produto;
import br.com.restaurante.ProdutoRepository;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import br.com.restaurante.dto.ItemPedidoRequest;
import br.com.restaurante.dto.PedidoRequest;
import br.com.restaurante.dto.PedidoResponse;

import br.com.restaurante.model.ItemPedido;
import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.Pedido;
import br.com.restaurante.model.StatusMesa;
import br.com.restaurante.model.StatusPedido;

import br.com.restaurante.repository.MesaRepository;
import br.com.restaurante.repository.PedidoRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.restaurante.dto.CancelamentoPedidoRequest;

import br.com.restaurante.model.Perfil;
import br.com.restaurante.model.Usuario;

import br.com.restaurante.repository.UsuarioRepository;

import java.util.TreeMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

@Service
public class PedidoService {

    private final PedidoRepository pedidos;

    private final ProdutoRepository produtos;

    private final MesaRepository mesas;

    private final MesaService mesaService;

    private final UsuarioRepository usuarios;

    private final ReservaEstoqueService reservaEstoqueService;

    private final EntityManager entityManager;


   public PedidoService(

    PedidoRepository pedidos,

    ProdutoRepository produtos,

    MesaRepository mesas,

    MesaService mesaService,

    UsuarioRepository usuarios,

    ReservaEstoqueService reservaEstoqueService,

    EntityManager entityManager

) {

    this.pedidos = pedidos;

    this.produtos = produtos;

    this.mesas = mesas;

    this.mesaService = mesaService;

    this.usuarios = usuarios;

    this.reservaEstoqueService = reservaEstoqueService;

    this.entityManager = entityManager;
}


    /* =========================================
       REALIZACAO DE PEDIDOS
       ========================================= */

    @Transactional
    public PedidoResponse realizarPedido(

            Long mesaId,

            String codigoAtendimento,

            PedidoRequest dados) {


        Mesa mesa = mesas.buscarParaAtualizacao(mesaId)

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


        validarCodigoAtendimento(
            mesa,
            codigoAtendimento
        );


        if (mesa.status != StatusMesa.OCUPADA) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "A mesa nao esta recebendo novos pedidos"
            );
        }


        Map<Long, Integer> quantidades =
    new TreeMap<>(
        agruparItens(dados.itens())
    );


        Pedido pedido = new Pedido();

        pedido.mesa = mesa;

        pedido.codigoAtendimento =
                codigoAtendimento;

        pedido.dataHora = LocalDateTime.now();

        pedido.status = StatusPedido.RECEBIDO;

        pedido.observacoes =

            normalizarObservacoes(
                dados.observacoes()
            );


        for (Map.Entry<Long, Integer> registro
                : quantidades.entrySet()) {


            Produto produto =

                buscarProdutoDisponivel(
                    registro.getKey()
                );


            ItemPedido item = new ItemPedido();

            item.produto = produto;

            item.nomeProduto = produto.nome;

            item.quantidade = registro.getValue();

            item.precoUnitario = produto.preco;

            item.calcularSubtotal();


            pedido.adicionarItem(item);
        }


        Pedido salvo = pedidos.saveAndFlush(pedido);


/* =========================================
   RESERVA AUTOMATICA DE ESTOQUE
   RN09, RN10 E RN13
   ========================================= */

reservaEstoqueService.reservarIngredientes(
    salvo
);


return PedidoResponse.de(salvo);
    }


    /* =========================================
       CONSULTAS DO CLIENTE
       ========================================= */

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosCliente(

            Long mesaId,

            String codigoAtendimento) {


        mesaService.validarAtendimento(

            mesaId,

            codigoAtendimento
        );


        return pedidos

            .findByMesa_IdAndCodigoAtendimentoOrderByDataHoraDesc(

                mesaId,

                codigoAtendimento
            )

            .stream()

            .map(PedidoResponse::de)

            .toList();
    }


    @Transactional(readOnly = true)
    public PedidoResponse buscarPedidoCliente(

            Long pedidoId,

            Long mesaId,

            String codigoAtendimento) {


        mesaService.validarAtendimento(

            mesaId,

            codigoAtendimento
        );


        Pedido pedido = pedidos

            .findByIdAndMesa_IdAndCodigoAtendimento(

                pedidoId,

                mesaId,

                codigoAtendimento
            )

            .orElseThrow(() ->

                new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Pedido nao encontrado neste atendimento"
                )
            );


        return PedidoResponse.de(pedido);
    }


    /* =========================================
       CONSULTAS ADMINISTRATIVAS
       ========================================= */

    @Transactional(readOnly = true)
public List<PedidoResponse> listarTodos() {

    return pedidos

        .findAllByOrderByDataHoraDesc()

        .stream()

        .map(PedidoResponse::deAdministrativo)

        .toList();
}


    @Transactional(readOnly = true)
    public List<PedidoResponse> listarFilaCozinha() {

        return pedidos

            .findByStatusInOrderByDataHoraAsc(

                List.of(

                    StatusPedido.RECEBIDO,

                    StatusPedido.EM_PREPARO,

                    StatusPedido.PRONTO
                )
            )

            .stream()

            .map(PedidoResponse::de)

            .toList();
    }


    /* =========================================
       ATUALIZACAO DOS ESTADOS
       ========================================= */

    @Transactional
    public PedidoResponse iniciarPreparo(Long id) {

        return alterarStatus(

            id,

            StatusPedido.RECEBIDO,

            StatusPedido.EM_PREPARO
        );
    }


    @Transactional
public PedidoResponse marcarPronto(Long id) {

    Pedido pedido =
        buscarPedidoParaAtualizacao(id);


    if (pedido.status != StatusPedido.EM_PREPARO) {

        throw new ResponseStatusException(

            HttpStatus.CONFLICT,

            "Somente pedidos em preparo podem ser marcados como prontos"
        );
    }


    Usuario responsavel =
        buscarResponsavelOperacao();


    /* =========================================
       BAIXA AUTOMATICA DE ESTOQUE - RN11
       ========================================= */

    reservaEstoqueService.consumirReservas(

        pedido,

        responsavel
    );


    pedido.status = StatusPedido.PRONTO;


    return PedidoResponse.de(

        pedidos.saveAndFlush(pedido)
    );
}


    @Transactional
    public PedidoResponse registrarEntrega(Long id) {

        return alterarStatus(

            id,

            StatusPedido.PRONTO,

            StatusPedido.ENTREGUE
        );
    }

    /* =========================================
   CANCELAMENTO DE PEDIDOS - RN04
   ========================================= */

@Transactional
public PedidoResponse cancelarPedido(

        Long id,

        CancelamentoPedidoRequest dados,

        String loginFuncionario) {


    String motivo = validarMotivoCancelamento(dados);


    Usuario funcionario =

        buscarFuncionarioCancelamento(
            loginFuncionario
        );


    Pedido pedido =
    buscarPedidoParaAtualizacao(id);


    if (
        pedido.status != StatusPedido.RECEBIDO &&

        pedido.status != StatusPedido.EM_PREPARO &&

        pedido.status != StatusPedido.PRONTO
    ) {

        throw new ResponseStatusException(

            HttpStatus.CONFLICT,

            "Somente pedidos ativos podem ser cancelados"
        );
    }

    /* =========================================
   TRATAMENTO DAS RESERVAS NO CANCELAMENTO
   ========================================= */

switch (pedido.status) {

    case RECEBIDO ->

        reservaEstoqueService.liberarReservas(

            pedido,

            funcionario
        );


    case EM_PREPARO ->

        reservaEstoqueService.registrarPerdas(

            pedido,

            funcionario
        );


    case PRONTO -> {

        /*
         * Os ingredientes ja foram consumidos.
         * Nao realizar uma segunda baixa.
         */
    }


    default ->

        throw new ResponseStatusException(

            HttpStatus.CONFLICT,

            "Somente pedidos ativos podem ser cancelados"
        );
}

    pedido.status = StatusPedido.CANCELADO;

    pedido.motivoCancelamento = motivo;

    pedido.dataCancelamento = LocalDateTime.now();

    pedido.canceladoPor = funcionario;


    Pedido salvo = pedidos.saveAndFlush(pedido);


    return PedidoResponse.deAdministrativo(salvo);
}


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Pedido buscarPedido(Long id) {

        return pedidos.findById(id)

            .orElseThrow(() ->

                new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Pedido nao encontrado"
                )
            );
    }

    private Pedido buscarPedidoParaAtualizacao(Long id) {

        Long mesaId =
            pedidos.buscarMesaIdPorPedido(id)

                .orElseThrow(() ->

                    new ResponseStatusException(

                        HttpStatus.NOT_FOUND,

                        "Pedido nao encontrado"
                    )
                );


        Mesa mesa =
            mesas.buscarParaAtualizacao(mesaId)

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


        Pedido pedido =
            pedidos.buscarParaAtualizacao(id)

                .orElseThrow(() ->

                    new ResponseStatusException(

                        HttpStatus.NOT_FOUND,

                        "Pedido nao encontrado"
                    )
                );


        entityManager.refresh(

            pedido,

            LockModeType.PESSIMISTIC_WRITE
        );


        if (
            pedido.mesa == null ||

            !mesaId.equals(pedido.mesa.id) ||

            pedido.codigoAtendimento == null ||

            !pedido.codigoAtendimento.equals(
                mesa.codigoAtendimento
            ) ||

            (
                mesa.status != StatusMesa.OCUPADA &&

                mesa.status !=
                    StatusMesa.AGUARDANDO_PAGAMENTO
            )
        ) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "O pedido pertence a um atendimento encerrado ou invalido"
            );
        }


        return pedido;
    }

    private String validarMotivoCancelamento(

        CancelamentoPedidoRequest dados) {


    if (
        dados == null ||

        dados.motivo() == null ||

        dados.motivo().isBlank()
    ) {

        throw new ResponseStatusException(

            HttpStatus.BAD_REQUEST,

            "O motivo do cancelamento e obrigatorio"
        );
    }


    if (dados.motivo().length() > 255) {

        throw new ResponseStatusException(

            HttpStatus.BAD_REQUEST,

            "O motivo deve possuir no maximo 255 caracteres"
        );
    }


    return dados.motivo().strip();
}

    private Usuario buscarResponsavelOperacao() {

    Authentication autenticacao =
        SecurityContextHolder
            .getContext()
            .getAuthentication();


    if (

        autenticacao == null ||

        !autenticacao.isAuthenticated()

    ) {

        return null;
    }


    return usuarios
        .findByLogin(
            autenticacao.getName()
        )
        .orElse(null);
}

    private Usuario buscarFuncionarioCancelamento(

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


    Usuario funcionario = usuarios

        .findByLogin(loginFuncionario)

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
        funcionario.perfil != Perfil.ADMINISTRADOR &&

        funcionario.perfil != Perfil.GERENTE &&

        funcionario.perfil != Perfil.ATENDENTE
    ) {

        throw new ResponseStatusException(

            HttpStatus.FORBIDDEN,

            "Funcionario sem permissao para cancelar pedidos"
        );
    }


    return funcionario;
}


    private PedidoResponse alterarStatus(

            Long id,

            StatusPedido estadoEsperado,

            StatusPedido novoEstado) {


        Pedido pedido =
    buscarPedidoParaAtualizacao(id);


        if (pedido.status != estadoEsperado) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Transicao de estado nao permitida"
            );
        }


        pedido.status = novoEstado;


        return PedidoResponse.de(

            pedidos.saveAndFlush(pedido)
        );
    }


    private Produto buscarProdutoDisponivel(Long id) {

    Produto produto =
        produtos.buscarParaAtualizacao(id)

            .orElseThrow(() ->

                new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Produto nao encontrado: " + id
                )
            );


    entityManager.refresh(

        produto,

        LockModeType.PESSIMISTIC_WRITE
    );


    if (

        !produto.ativo ||

        produto.categoria == null ||

        !produto.categoria.ativo

    ) {

        throw new ResponseStatusException(

            HttpStatus.CONFLICT,

            "Produto indisponivel: " + produto.nome
        );
    }


    return produto;
}


    private void validarCodigoAtendimento(

            Mesa mesa,

            String codigoAtendimento) {


        if (
            codigoAtendimento == null ||

            !codigoAtendimento.equals(
                mesa.codigoAtendimento
            ) ||

            (
                mesa.status != StatusMesa.OCUPADA &&

                mesa.status !=
                    StatusMesa.AGUARDANDO_PAGAMENTO
            )
        ) {

            throw new ResponseStatusException(

                HttpStatus.UNAUTHORIZED,

                "Atendimento encerrado ou invalido"
            );
        }
    }


    private Map<Long, Integer> agruparItens(

            List<ItemPedidoRequest> itens) {


        if (
            itens == null ||

            itens.isEmpty() ||

            itens.size() > 100
        ) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Quantidade de itens invalida"
            );
        }


        Map<Long, Integer> quantidades =

            new LinkedHashMap<>();


        for (ItemPedidoRequest item : itens) {


            if (
                item == null ||

                item.produtoId() == null ||

                item.produtoId() <= 0 ||

                item.quantidade() == null ||

                item.quantidade() < 1 ||

                item.quantidade() > 99
            ) {

                throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Item do pedido invalido"
                );
            }


            int quantidadeTotal =

                quantidades.getOrDefault(

                    item.produtoId(),

                    0

                ) + item.quantidade();


            if (quantidadeTotal > 99) {

                throw new ResponseStatusException(

                    HttpStatus.BAD_REQUEST,

                    "Limite de 99 unidades por produto"
                );
            }


            quantidades.put(

                item.produtoId(),

                quantidadeTotal
            );
        }


        return quantidades;
    }


    private String normalizarObservacoes(

            String observacoes) {


        if (observacoes == null) {

            return null;
        }


        String texto = observacoes.trim();


        if (texto.length() > 500) {

            throw new ResponseStatusException(

                HttpStatus.BAD_REQUEST,

                "Observacoes excedem 500 caracteres"
            );
        }


        return texto.isEmpty() ? null : texto;
    }
}
