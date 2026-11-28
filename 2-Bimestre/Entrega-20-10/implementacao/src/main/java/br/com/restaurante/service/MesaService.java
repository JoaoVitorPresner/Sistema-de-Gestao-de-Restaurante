package br.com.restaurante.service;

import br.com.restaurante.dto.MesaRequest;

import br.com.restaurante.model.Mesa;
import br.com.restaurante.model.StatusMesa;

import br.com.restaurante.repository.MesaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class MesaService {

    private final MesaRepository mesas;

    private final EntityManager entityManager;


    public MesaService(

        MesaRepository mesas,

        EntityManager entityManager

    ) {

        this.mesas = mesas;

        this.entityManager = entityManager;
    }


    /* =========================================
       CONSULTAS
       ========================================= */

    @Transactional(readOnly = true)
    public List<Mesa> listar() {

        return mesas.findAllByOrderByNumeroAsc();
    }


    @Transactional(readOnly = true)
    public Mesa buscar(Long id) {

        return mesas.findById(id)

            .orElseThrow(() ->

                new ResponseStatusException(

                    HttpStatus.NOT_FOUND,

                    "Mesa nao encontrada"
                )
            );
    }


    /* =========================================
       CADASTRO DE MESAS
       ========================================= */

    @Transactional
    public Mesa cadastrar(MesaRequest dados) {

        if (mesas.existsByNumero(dados.numero())) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Numero de mesa ja cadastrado"
            );
        }


        Mesa mesa = new Mesa();

        mesa.numero = dados.numero();

        mesa.capacidade = dados.capacidade();

        mesa.status = StatusMesa.LIVRE;

        mesa.codigoAtendimento = null;


        return mesas.saveAndFlush(mesa);
    }


    /* =========================================
       EDICAO DE MESAS
       ========================================= */

    @Transactional
    public Mesa editar(

        Long id,

        MesaRequest dados

    ) {

        Mesa mesa =
            buscarParaAtualizacao(id);


        if (
            mesa.status != StatusMesa.LIVRE &&

            mesa.status != StatusMesa.INATIVA
        ) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Nao e possivel editar uma mesa em atendimento"
            );
        }


        if (
            !mesa.numero.equals(dados.numero()) &&

            mesas.existsByNumero(dados.numero())
        ) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Numero de mesa ja cadastrado"
            );
        }


        mesa.numero = dados.numero();

        mesa.capacidade = dados.capacidade();


        return mesas.saveAndFlush(mesa);
    }


    /* =========================================
       INICIO DO ATENDIMENTO - RN05
       ========================================= */

    @Transactional
    public Mesa ocupar(Long id) {

        Mesa mesa =
            buscarParaAtualizacao(id);


        exigirStatus(

            mesa,

            StatusMesa.LIVRE
        );


        mesa.status =
            StatusMesa.OCUPADA;

        mesa.codigoAtendimento =
            UUID.randomUUID().toString();


        return mesas.saveAndFlush(mesa);
    }


    /* =========================================
       SOLICITACAO DE PAGAMENTO
       ========================================= */

    @Transactional
    public Mesa solicitarPagamento(Long id) {

        Mesa mesa =
            buscarParaAtualizacao(id);


        exigirStatus(

            mesa,

            StatusMesa.OCUPADA
        );


        mesa.status =
            StatusMesa.AGUARDANDO_PAGAMENTO;


        return mesas.saveAndFlush(mesa);
    }


    /* =========================================
       DESATIVACAO DE MESAS
       ========================================= */

    @Transactional
    public Mesa desativar(Long id) {

        Mesa mesa =
            buscarParaAtualizacao(id);


        exigirStatus(

            mesa,

            StatusMesa.LIVRE
        );


        mesa.status =
            StatusMesa.INATIVA;

        mesa.codigoAtendimento = null;


        return mesas.saveAndFlush(mesa);
    }


    /* =========================================
       REATIVACAO DE MESAS
       ========================================= */

    @Transactional
    public Mesa ativar(Long id) {

        Mesa mesa =
            buscarParaAtualizacao(id);


        exigirStatus(

            mesa,

            StatusMesa.INATIVA
        );


        mesa.status =
            StatusMesa.LIVRE;

        mesa.codigoAtendimento = null;


        return mesas.saveAndFlush(mesa);
    }


    /* =========================================
       ACESSO DO CLIENTE
       ========================================= */

    @Transactional
    public Mesa acessarComoCliente(
            Integer numero) {

        Mesa mesa =
            buscarPorNumeroParaAtualizacao(
                numero
            );


        if (mesa.status == StatusMesa.LIVRE) {

            mesa.status =
                StatusMesa.OCUPADA;

            mesa.codigoAtendimento =
                UUID.randomUUID().toString();


        } else if (
            mesa.status == StatusMesa.OCUPADA
        ) {

            if (mesa.codigoAtendimento == null) {

                mesa.codigoAtendimento =
                    UUID.randomUUID().toString();
            }


        } else {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Mesa indisponivel para atendimento"
            );
        }


        return mesas.saveAndFlush(mesa);
    }


    /* =========================================
       VALIDACAO DO ATENDIMENTO - RN14
       ========================================= */

    @Transactional(readOnly = true)
    public Mesa validarAtendimento(

        Long mesaId,

        String codigoAtendimento

    ) {

        Mesa mesa = buscar(mesaId);


        boolean atendimentoValido =

            codigoAtendimento != null &&

            codigoAtendimento.equals(
                mesa.codigoAtendimento
            );


        boolean statusValido =

            mesa.status == StatusMesa.OCUPADA ||

            mesa.status ==
                StatusMesa.AGUARDANDO_PAGAMENTO;


        if (!atendimentoValido || !statusValido) {

            throw new ResponseStatusException(

                HttpStatus.UNAUTHORIZED,

                "Atendimento encerrado ou invalido"
            );
        }


        return mesa;
    }


    /* =========================================
       BLOQUEIOS TRANSACIONAIS
       ========================================= */

    private Mesa buscarParaAtualizacao(
            Long id) {

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


    private Mesa buscarPorNumeroParaAtualizacao(
            Integer numero) {

        Mesa mesa =
            mesas.buscarPorNumeroParaAtualizacao(
                numero
            )

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


    /* =========================================
       VALIDACAO DE ESTADO
       ========================================= */

    private void exigirStatus(

        Mesa mesa,

        StatusMesa statusEsperado

    ) {

        if (mesa.status != statusEsperado) {

            throw new ResponseStatusException(

                HttpStatus.CONFLICT,

                "Operacao nao permitida para uma mesa com status "
                    + mesa.status
            );
        }
    }

}