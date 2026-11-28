package br.com.restaurante.service;

import br.com.restaurante.dto.*;
import br.com.restaurante.model.*;
import br.com.restaurante.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EstoqueService {

    private final IngredienteRepository ingredientes;

    private final ComposicaoProdutoRepository composicoes;

    private final ReservaEstoqueRepository reservas;

    private final MovimentacaoEstoqueRepository movimentacoes;

    private final UsuarioRepository usuarios;


    public EstoqueService(

        IngredienteRepository ingredientes,

        ComposicaoProdutoRepository composicoes,

        ReservaEstoqueRepository reservas,

        MovimentacaoEstoqueRepository movimentacoes,

        UsuarioRepository usuarios

    ) {

        this.ingredientes = ingredientes;

        this.composicoes = composicoes;

        this.reservas = reservas;

        this.movimentacoes = movimentacoes;

        this.usuarios = usuarios;
    }


    /* =========================================
       LISTAGEM DE INGREDIENTES
       ========================================= */

    @Transactional(readOnly = true)
    public List<IngredienteResponse> listarIngredientes() {

        return ingredientes
            .findAllByOrderByNomeAsc()
            .stream()
            .map(IngredienteResponse::de)
            .toList();
    }


    /* =========================================
       CONSULTA DE INGREDIENTE
       ========================================= */

    @Transactional(readOnly = true)
    public IngredienteResponse buscarIngrediente(
        Long id
    ) {

        return IngredienteResponse.de(
            buscarEntidade(id)
        );
    }


    /* =========================================
       CADASTRO DE INGREDIENTE
       ========================================= */

    @Transactional
    public IngredienteResponse cadastrarIngrediente(
        IngredienteRequest dados
    ) {

        validarDadosIngrediente(dados);

        String nome = dados.nome().strip();


        if (ingredientes.existsByNomeIgnoreCase(nome)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ja existe um ingrediente com esse nome"
            );
        }


        Ingrediente ingrediente = new Ingrediente();

        ingrediente.nome = nome;

        ingrediente.descricao =
            normalizarDescricao(dados.descricao());

        ingrediente.unidadeMedida =
            dados.unidadeMedida();

        ingrediente.estoqueMinimo =
            dados.estoqueMinimo();

        ingrediente.estoqueFisico =
            BigDecimal.ZERO;

        ingrediente.estoqueReservado =
            BigDecimal.ZERO;

        ingrediente.ativo = true;


        return IngredienteResponse.de(
            ingredientes.saveAndFlush(ingrediente)
        );
    }


    /* =========================================
       EDICAO DE INGREDIENTE
       ========================================= */

    @Transactional
    public IngredienteResponse editarIngrediente(

        Long id,

        IngredienteRequest dados

    ) {

        validarDadosIngrediente(dados);

        Ingrediente ingrediente =
            buscarParaAtualizacao(id);

        String nome = dados.nome().strip();


        if (
            ingredientes.existsByNomeIgnoreCaseAndIdNot(
                nome,
                id
            )
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ja existe outro ingrediente com esse nome"
            );
        }


        if (
            ingrediente.unidadeMedida !=
            dados.unidadeMedida()
        ) {

            validarAlteracaoUnidade(ingrediente);
        }


        ingrediente.nome = nome;

        ingrediente.descricao =
            normalizarDescricao(dados.descricao());

        ingrediente.unidadeMedida =
            dados.unidadeMedida();

        ingrediente.estoqueMinimo =
            dados.estoqueMinimo();


        return IngredienteResponse.de(
            ingredientes.saveAndFlush(ingrediente)
        );
    }


    /* =========================================
       DESATIVACAO DE INGREDIENTE
       ========================================= */

    @Transactional
    public IngredienteResponse desativarIngrediente(
        Long id
    ) {

        Ingrediente ingrediente =
            buscarParaAtualizacao(id);


        if (!ingrediente.ativo) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "O ingrediente ja esta inativo"
            );
        }


        if (

            ingrediente.estoqueReservado.signum() > 0 ||

            reservas.existsByIngrediente_IdAndStatus(
                id,
                StatusReserva.RESERVADA
            )

        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "O ingrediente possui reservas ativas"
            );
        }


        if (
            composicoes
                .existsByIngrediente_IdAndProduto_AtivoTrue(
                    id
                )
        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Existem produtos ativos que utilizam este ingrediente"
            );
        }


        ingrediente.ativo = false;


        return IngredienteResponse.de(
            ingredientes.saveAndFlush(ingrediente)
        );
    }


    /* =========================================
       REATIVACAO DE INGREDIENTE
       ========================================= */

    @Transactional
    public IngredienteResponse ativarIngrediente(
        Long id
    ) {

        Ingrediente ingrediente =
            buscarParaAtualizacao(id);


        if (ingrediente.ativo) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "O ingrediente ja esta ativo"
            );
        }


        ingrediente.ativo = true;


        return IngredienteResponse.de(
            ingredientes.saveAndFlush(ingrediente)
        );
    }


    /* =========================================
       MOVIMENTACOES MANUAIS - RN08
       ========================================= */

    @Transactional
    public MovimentacaoEstoqueResponse registrarMovimentacao(

        MovimentacaoEstoqueRequest dados,

        String loginFuncionario

    ) {

        validarMovimentacao(dados);


        Usuario responsavel =
            buscarFuncionario(loginFuncionario);


        Ingrediente ingrediente =
            buscarParaAtualizacao(
                dados.ingredienteId()
            );


        if (!ingrediente.ativo) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Nao e permitido movimentar um ingrediente inativo"
            );
        }


        BigDecimal quantidade =
            dados.quantidade();


        BigDecimal fisicoAnterior =
            ingrediente.estoqueFisico;


        BigDecimal reservadoAnterior =
            ingrediente.estoqueReservado;


        if (dados.tipo() == TipoMovimentacao.ENTRADA) {

            BigDecimal novoSaldo =
                ingrediente.estoqueFisico.add(
                    quantidade
                );


            validarLimiteSaldo(novoSaldo);


            ingrediente.estoqueFisico =
                novoSaldo;

        } else {

            if (

                ingrediente.getEstoqueDisponivel()
                    .compareTo(quantidade) < 0

            ) {

                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Estoque disponivel insuficiente para esta saida"
                );
            }


            ingrediente.estoqueFisico =
                ingrediente.estoqueFisico.subtract(
                    quantidade
                );
        }


        validarIntegridade(ingrediente);


        ingredientes.saveAndFlush(ingrediente);


        MovimentacaoEstoque movimentacao =
            new MovimentacaoEstoque();


        movimentacao.ingrediente =
            ingrediente;

        movimentacao.responsavel =
            responsavel;

        movimentacao.tipo =
            dados.tipo();

        movimentacao.quantidade =
            quantidade;

        movimentacao.fisicoAnterior =
            fisicoAnterior;

        movimentacao.fisicoPosterior =
            ingrediente.estoqueFisico;

        movimentacao.reservadoAnterior =
            reservadoAnterior;

        movimentacao.reservadoPosterior =
            ingrediente.estoqueReservado;

        movimentacao.motivo =
            dados.motivo().strip();

        movimentacao.dataHora =
            LocalDateTime.now();


        return MovimentacaoEstoqueResponse.de(
            movimentacoes.saveAndFlush(movimentacao)
        );
    }


    /* =========================================
       HISTORICO DE MOVIMENTACOES
       ========================================= */

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueResponse>
        listarMovimentacoes() {

        return movimentacoes
            .findTop100ByOrderByDataHoraDescIdDesc()
            .stream()
            .map(MovimentacaoEstoqueResponse::de)
            .toList();
    }


    /* =========================================
       HISTORICO POR INGREDIENTE
       ========================================= */

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueResponse>
        listarMovimentacoesIngrediente(
            Long ingredienteId
        ) {

        buscarEntidade(ingredienteId);


        return movimentacoes

            .findTop100ByIngrediente_IdOrderByDataHoraDescIdDesc(
                ingredienteId
            )

            .stream()

            .map(MovimentacaoEstoqueResponse::de)

            .toList();
    }


    /* =========================================
       HISTORICO POR TIPO
       ========================================= */

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueResponse>
        listarMovimentacoesTipo(
            TipoMovimentacao tipo
        ) {

        if (tipo == null) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O tipo de movimentacao e obrigatorio"
            );
        }


        return movimentacoes

            .findTop100ByTipoOrderByDataHoraDescIdDesc(
                tipo
            )

            .stream()

            .map(MovimentacaoEstoqueResponse::de)

            .toList();
    }


    /* =========================================
       INDICADORES DO ESTOQUE
       ========================================= */

    @Transactional(readOnly = true)
    public ResumoEstoqueResponse obterResumo() {

        List<Ingrediente> lista =
            ingredientes.findAll();


        long total = lista.size();


        long ativos = lista.stream()

            .filter(i -> i.ativo)

            .count();


        long inativos = total - ativos;


        long estoqueBaixo = lista.stream()

            .filter(i -> i.ativo)

            .filter(Ingrediente::isEstoqueBaixo)

            .count();


        long indisponiveis = lista.stream()

            .filter(i -> i.ativo)

            .filter(
                i -> i.getEstoqueDisponivel().signum() <= 0
            )

            .count();


        long comReserva = lista.stream()

            .filter(
                i -> i.estoqueReservado.signum() > 0
            )

            .count();


        return new ResumoEstoqueResponse(

            total,

            ativos,

            inativos,

            estoqueBaixo,

            indisponiveis,

            comReserva
        );
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Ingrediente buscarEntidade(
        Long id
    ) {

        return ingredientes.findById(id)

            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ingrediente nao encontrado"
                )
            );
    }


    private Ingrediente buscarParaAtualizacao(
        Long id
    ) {

        return ingredientes.buscarParaAtualizacao(id)

            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ingrediente nao encontrado"
                )
            );
    }


    private void validarDadosIngrediente(
        IngredienteRequest dados
    ) {

        if (

            dados == null ||

            dados.nome() == null ||

            dados.nome().isBlank() ||

            dados.nome().length() > 100 ||

            dados.unidadeMedida() == null ||

            dados.estoqueMinimo() == null ||

            dados.estoqueMinimo().signum() < 0

        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Dados do ingrediente invalidos"
            );
        }


        if (

            dados.descricao() != null &&

            dados.descricao().length() > 255

        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A descricao deve possuir no maximo 255 caracteres"
            );
        }


        validarPrecisao(dados.estoqueMinimo());
    }


    private void validarAlteracaoUnidade(
        Ingrediente ingrediente
    ) {

        if (

            ingrediente.estoqueFisico.signum() > 0 ||

            ingrediente.estoqueReservado.signum() > 0 ||

            composicoes.existsByIngrediente_Id(
                ingrediente.id
            ) ||

            !movimentacoes
                .findTop100ByIngrediente_IdOrderByDataHoraDescIdDesc(
                    ingrediente.id
                )
                .isEmpty()

        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Nao e possivel alterar a unidade de um ingrediente com saldo, composicao ou historico"
            );
        }
    }


    private void validarMovimentacao(
        MovimentacaoEstoqueRequest dados
    ) {

        if (

            dados == null ||

            dados.ingredienteId() == null ||

            dados.ingredienteId() <= 0 ||

            dados.quantidade() == null ||

            dados.quantidade().signum() <= 0 ||

            dados.motivo() == null ||

            dados.motivo().isBlank() ||

            dados.motivo().length() > 255

        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Dados da movimentacao invalidos"
            );
        }


        if (!dados.isTipoManualValido()) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Somente entradas e saidas manuais sao permitidas"
            );
        }


        validarPrecisao(dados.quantidade());
    }


    private Usuario buscarFuncionario(
        String login
    ) {

        if (login == null || login.isBlank()) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Funcionario nao autenticado"
            );
        }


        Usuario usuario = usuarios.findByLogin(login)

            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Funcionario nao encontrado"
                )
            );


        if (

            !usuario.ativo ||

            usuario.bloqueado ||

            (
                usuario.perfil != Perfil.ADMINISTRADOR &&

                usuario.perfil != Perfil.GERENTE &&

                usuario.perfil != Perfil.ESTOQUISTA
            )

        ) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Funcionario sem permissao para movimentar estoque"
            );
        }


        return usuario;
    }


    private void validarPrecisao(
        BigDecimal quantidade
    ) {

        if (

            quantidade.scale() > 3 ||

            quantidade.abs().compareTo(
                new BigDecimal("999999999999.999")
            ) > 0

        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Quantidade fora dos limites permitidos"
            );
        }
    }


    private void validarLimiteSaldo(
        BigDecimal saldo
    ) {

        if (

            saldo.compareTo(
                new BigDecimal("999999999999.999")
            ) > 0

        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "O saldo ultrapassaria o limite permitido"
            );
        }
    }


    private void validarIntegridade(
        Ingrediente ingrediente
    ) {

        if (!ingrediente.isSaldoValido()) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A operacao comprometeria a integridade do estoque"
            );
        }
    }


    private String normalizarDescricao(
        String descricao
    ) {

        if (

            descricao == null ||

            descricao.isBlank()

        ) {

            return null;
        }


        return descricao.strip();
    }
}