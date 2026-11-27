package br.com.restaurante.service;

import br.com.restaurante.Categoria;
import br.com.restaurante.CategoriaRepository;
import br.com.restaurante.Produto;
import br.com.restaurante.ProdutoRepository;

import br.com.restaurante.dto.CategoriaCardapioDTO;
import br.com.restaurante.dto.ProdutoCardapioDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CardapioService {

    private final CategoriaRepository categorias;
    private final ProdutoRepository produtos;
    private final ComposicaoProdutoService composicaoService;

    public CardapioService(
            CategoriaRepository categorias,
            ProdutoRepository produtos,
            ComposicaoProdutoService composicaoService) {

        this.categorias = categorias;
        this.produtos = produtos;
        this.composicaoService = composicaoService;
    }

    /* =========================================
       CONSULTA DO CARDAPIO
       ========================================= */

    @Transactional(readOnly = true)
    public List<CategoriaCardapioDTO> listarCardapio() {

        List<Categoria> categoriasAtivas =
            categorias.findByAtivoTrueOrderByNomeAsc();

        List<Produto> produtosAtivos =
            produtos
                .findByAtivoTrueAndCategoria_AtivoTrueOrderByNomeAsc();

        Map<Long, List<ProdutoCardapioDTO>>
            produtosPorCategoria = new HashMap<>();

        /* Agrupa os produtos por categoria. */

        for (Produto produto : produtosAtivos) {

            boolean disponivel =
                composicaoService.produtoDisponivel(
                    produto.id
                );

            ProdutoCardapioDTO dto =
                new ProdutoCardapioDTO(
                    produto.id,
                    produto.nome,
                    produto.descricao,
                    produto.preco,
                    disponivel
                );

            produtosPorCategoria
                .computeIfAbsent(
                    produto.categoria.id,
                    id -> new ArrayList<>()
                )
                .add(dto);
        }

        /* Monta a resposta final do cardapio. */

        List<CategoriaCardapioDTO> cardapio =
            new ArrayList<>();

        for (Categoria categoria : categoriasAtivas) {

            List<ProdutoCardapioDTO> produtosDaCategoria =
                produtosPorCategoria.getOrDefault(
                    categoria.id,
                    List.of()
                );

            // Categorias sem produtos ativos
            // nao precisam aparecer no cardapio.

            if (produtosDaCategoria.isEmpty()) {
                continue;
            }

            CategoriaCardapioDTO dto =
                new CategoriaCardapioDTO(
                    categoria.id,
                    categoria.nome,
                    categoria.descricao,
                    produtosDaCategoria
                );

            cardapio.add(dto);
        }

        return cardapio;
    }
}