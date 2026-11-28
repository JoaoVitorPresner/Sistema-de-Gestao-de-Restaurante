package br.com.restaurante.service;

import br.com.restaurante.Produto;
import br.com.restaurante.ProdutoRepository;

import br.com.restaurante.dto.ComposicaoProdutoRequest;
import br.com.restaurante.dto.ComposicaoProdutoResponse;

import br.com.restaurante.model.ComposicaoProduto;
import br.com.restaurante.model.Ingrediente;

import br.com.restaurante.repository.ComposicaoProdutoRepository;
import br.com.restaurante.repository.IngredienteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ComposicaoProdutoService {

    private final ProdutoRepository produtos;

    private final IngredienteRepository ingredientes;

    private final ComposicaoProdutoRepository composicoes;


    public ComposicaoProdutoService(

        ProdutoRepository produtos,

        IngredienteRepository ingredientes,

        ComposicaoProdutoRepository composicoes

    ) {

        this.produtos = produtos;

        this.ingredientes = ingredientes;

        this.composicoes = composicoes;
    }


    /* =========================================
       CONSULTAR COMPOSICAO
       ========================================= */

    @Transactional(readOnly = true)
    public List<ComposicaoProdutoResponse>
        listarComposicao(Long produtoId) {

        buscarProduto(produtoId);

        return composicoes
            .findByProduto_IdOrderByIngrediente_IdAsc(
                produtoId
            )
            .stream()
            .map(ComposicaoProdutoResponse::de)
            .toList();
    }


    /* =========================================
       CONFIGURAR CONTROLE DE ESTOQUE
       ========================================= */

    @Transactional
    public Produto configurarControleEstoque(

        Long produtoId,

        boolean controlaEstoque

    ) {

        Produto produto =
            buscarProdutoParaAtualizacao(produtoId);


        if (

            !controlaEstoque &&

            composicoes.existsByProduto_Id(produtoId)

        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Remova a composicao antes de desabilitar o controle de estoque"
            );
        }


        produto.controlaEstoque = controlaEstoque;


        return produtos.saveAndFlush(produto);
    }


    /* =========================================
       ADICIONAR INGREDIENTE - RN13
       ========================================= */

    @Transactional
    public ComposicaoProdutoResponse adicionarIngrediente(

        Long produtoId,

        ComposicaoProdutoRequest dados

    ) {

        validarDados(dados);


        Produto produto =
            buscarProdutoParaAtualizacao(produtoId);


        Ingrediente ingrediente =
            buscarIngredienteParaAtualizacao(
                dados.ingredienteId()
            );


        validarIngredienteAtivo(ingrediente);


        if (

            composicoes.existsByProduto_IdAndIngrediente_Id(

                produtoId,

                ingrediente.id
            )

        ) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "O ingrediente ja faz parte da composicao"
            );
        }


        ComposicaoProduto composicao =
            new ComposicaoProduto();


        composicao.produto = produto;

        composicao.ingrediente = ingrediente;

        composicao.quantidade = dados.quantidade();


        produto.controlaEstoque = true;


        produtos.saveAndFlush(produto);


        return ComposicaoProdutoResponse.de(
            composicoes.saveAndFlush(composicao)
        );
    }


    /* =========================================
       EDITAR QUANTIDADE DO INGREDIENTE
       ========================================= */

    @Transactional
    public ComposicaoProdutoResponse editarIngrediente(

        Long produtoId,

        Long ingredienteId,

        ComposicaoProdutoRequest dados

    ) {

        validarDados(dados);


        if (!ingredienteId.equals(dados.ingredienteId())) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Nao e permitido substituir o ingrediente durante a edicao"
            );
        }


        buscarProdutoParaAtualizacao(produtoId);


        Ingrediente ingrediente =
            buscarIngredienteParaAtualizacao(
                ingredienteId
            );


        validarIngredienteAtivo(ingrediente);


        ComposicaoProduto composicao =
            buscarComposicao(
                produtoId,
                ingredienteId
            );


        composicao.quantidade =
            dados.quantidade();


        return ComposicaoProdutoResponse.de(
            composicoes.saveAndFlush(composicao)
        );
    }


    /* =========================================
       REMOVER INGREDIENTE DA COMPOSICAO
       ========================================= */

    @Transactional
    public void removerIngrediente(

        Long produtoId,

        Long ingredienteId

    ) {

        buscarProdutoParaAtualizacao(produtoId);

        buscarIngredienteParaAtualizacao(
            ingredienteId
        );


        ComposicaoProduto composicao =
            buscarComposicao(
                produtoId,
                ingredienteId
            );


        composicoes.delete(composicao);

        composicoes.flush();
    }


    /* =========================================
       DISPONIBILIDADE DO PRODUTO - RN09 E RN13
       ========================================= */

    @Transactional(readOnly = true)
    public boolean produtoDisponivel(
        Long produtoId
    ) {

        Produto produto =
            buscarProduto(produtoId);


        if (

            !produto.ativo ||

            produto.categoria == null ||

            !produto.categoria.ativo

        ) {

            return false;
        }


        List<ComposicaoProduto> receita =
            composicoes
                .findByProduto_IdOrderByIngrediente_IdAsc(
                    produtoId
                );


        if (receita.isEmpty()) {

            return !produto.controlaEstoque;
        }


        for (ComposicaoProduto item : receita) {

            if (!item.ingrediente.ativo) {

                return false;
            }


            if (

                item.ingrediente
                    .getEstoqueDisponivel()
                    .compareTo(item.quantidade) < 0

            ) {

                return false;
            }
        }


        return true;
    }


    /* =========================================
       METODOS AUXILIARES
       ========================================= */

    private Produto buscarProduto(
        Long id
    ) {

        return produtos.findById(id)

            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Produto nao encontrado"
                )
            );
    }


    private Produto buscarProdutoParaAtualizacao(
        Long id
    ) {

        return produtos.buscarParaAtualizacao(id)

            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Produto nao encontrado"
                )
            );
    }


    private Ingrediente buscarIngredienteParaAtualizacao(
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


    private ComposicaoProduto buscarComposicao(

        Long produtoId,

        Long ingredienteId

    ) {

        return composicoes

            .findByProduto_IdAndIngrediente_Id(

                produtoId,

                ingredienteId
            )

            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ingrediente nao encontrado na composicao"
                )
            );
    }


    private void validarIngredienteAtivo(
        Ingrediente ingrediente
    ) {

        if (!ingrediente.ativo) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "O ingrediente esta inativo"
            );
        }
    }


    private void validarDados(
        ComposicaoProdutoRequest dados
    ) {

        if (

            dados == null ||

            dados.ingredienteId() == null ||

            dados.ingredienteId() <= 0 ||

            dados.quantidade() == null ||

            dados.quantidade().signum() <= 0 ||

            dados.quantidade().scale() > 3 ||

            dados.quantidade().compareTo(
                new BigDecimal("999999999.999")
            ) > 0

        ) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Dados da composicao invalidos"
            );
        }
    }
}