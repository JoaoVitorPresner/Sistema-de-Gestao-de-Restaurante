
const CHAVE_CARRINHO = "restaurante.carrinho";

let mesaCarrinho = null;


/* =========================================
   FORMATACAO MONETARIA
   ========================================= */

function formatarMoeda(valor) {

    return new Intl.NumberFormat("pt-BR", {

        style: "currency",
        currency: "BRL"

    }).format(valor);
}


/* =========================================
   ARMAZENAMENTO
   ========================================= */

function lerCarrinho() {

    try {

        const dados = JSON.parse(

            sessionStorage.getItem(CHAVE_CARRINHO)

            || "null"
        );

        if (
            !dados ||
            dados.mesaId !== mesaCarrinho ||
            !Array.isArray(dados.itens)
        ) {

            return [];
        }

        return dados.itens.filter(item =>

            Number.isInteger(item.id) &&

            Number.isInteger(item.quantidade) &&

            item.quantidade > 0 &&

            item.quantidade <= 99 &&

            Number.isFinite(item.preco) &&

            item.preco > 0
        );

    } catch {

        return [];
    }
}


function salvarCarrinho(itens) {

    sessionStorage.setItem(

        CHAVE_CARRINHO,

        JSON.stringify({

            mesaId: mesaCarrinho,

            itens: itens
        })
    );
}


function iniciarCarrinho(mesaId) {

    mesaCarrinho = mesaId;

    salvarCarrinho(lerCarrinho());
}


function limparCarrinho() {

    sessionStorage.removeItem(CHAVE_CARRINHO);
}


/* =========================================
   GERENCIAMENTO DOS ITENS
   ========================================= */

function adicionarProdutoCarrinho(produto) {

    if (!produto || produto.disponivel !== true) {

        throw new Error(
            "Este produto está temporariamente indisponível."
        );
    }

    const itens = lerCarrinho();

    const existente = itens.find(

        item => item.id === produto.id
    );

    if (existente) {

        if (existente.quantidade >= 99) {

            throw new Error(
                "Quantidade máxima atingida."
            );
        }

        existente.quantidade++;

        existente.preco = Number(produto.preco);

        existente.nome = produto.nome;

    } else {

        itens.push({

            id: produto.id,

            nome: produto.nome,

            preco: Number(produto.preco),

            quantidade: 1
        });
    }

    salvarCarrinho(itens);
}


function alterarQuantidadeCarrinho(id, diferenca) {

    const itens = lerCarrinho();

    const item = itens.find(

        registro => registro.id === id
    );

    if (!item) {
        return;
    }

    item.quantidade += diferenca;

    if (item.quantidade > 99) {
        item.quantidade = 99;
    }

    salvarCarrinho(

        itens.filter(
            registro => registro.quantidade > 0
        )
    );
}


function removerProdutoCarrinho(id) {

    salvarCarrinho(

        lerCarrinho().filter(

            item => item.id !== id
        )
    );
}


/* =========================================
   SINCRONIZACAO COM O CARDAPIO
   ========================================= */

function sincronizarCarrinho(categorias) {

    const disponiveis = new Map();

    categorias.forEach(categoria => {

        categoria.produtos.forEach(produto => {

            if (produto.disponivel === true) {

                disponiveis.set(
                    produto.id,
                    produto
                );
            }
        });
    });

    const itens = lerCarrinho();

    let alterado = false;

    const atualizados = [];

    itens.forEach(item => {

        const produto = disponiveis.get(item.id);

        if (!produto) {

            alterado = true;

            return;
        }

        if (
            item.preco !== Number(produto.preco) ||
            item.nome !== produto.nome
        ) {

            alterado = true;
        }

        atualizados.push({

            id: produto.id,

            nome: produto.nome,

            preco: Number(produto.preco),

            quantidade: item.quantidade
        });
    });

    salvarCarrinho(atualizados);

    return alterado;
}

/* =========================================
   CALCULOS
   ========================================= */

function quantidadeTotalCarrinho() {

    return lerCarrinho().reduce(

        (total, item) => total + item.quantidade,

        0
    );
}


function valorTotalCarrinho() {

    const centavos = lerCarrinho().reduce(

        (total, item) =>

            total +

            Math.round(item.preco * 100) *

            item.quantidade,

        0
    );

    return centavos / 100;
}
