
const $ = id => document.getElementById(id);

let atendimento = null;

let categoriasCardapio = [];

let categoriaSelecionada = null;

let carregandoCardapio = false;

let cardapioValidado = false;

const mensagemCliente = $("mensagem-cliente");


/* =========================================
   FUNCOES AUXILIARES
   ========================================= */

function elemento(tag, classe, texto = "") {

    const novo = document.createElement(tag);

    if (classe) {
        novo.className = classe;
    }

    novo.textContent = texto;

    return novo;
}


function criarBotao(texto, classe, acao) {

    const botao = elemento("button", classe, texto);

    botao.type = "button";

    botao.addEventListener("click", acao);

    return botao;
}


function normalizar(texto) {

    return String(texto || "")

        .normalize("NFD")

        .replace(/[\u0300-\u036f]/g, "")

        .toLowerCase()

        .trim();
}


function podeAdicionarProdutos() {

    return atendimento !== null &&

        atendimento.status === "OCUPADA" &&

        cardapioValidado &&

        !carregandoCardapio;
}


/* =========================================
   CARREGAMENTO DO CARDAPIO
   ========================================= */

async function carregarCardapio() {

    if (carregandoCardapio) {
        return;
    }

    carregandoCardapio = true;

    cardapioValidado = false;

    $("atualizar-cardapio").disabled = true;

    mensagemCliente.hidden = true;

    renderizarCardapio();

    try {

        atendimento = await requisicaoApi(
            "/api/cliente/me"
        );

        iniciarCarrinho(atendimento.mesaId);

        $("identificacao-mesa").textContent =

            "Mesa " +

            String(atendimento.numeroMesa)
                .padStart(2, "0");

        $("aviso-pagamento").hidden =

            atendimento.status !== "AGUARDANDO_PAGAMENTO";


        categoriasCardapio = await requisicaoApi(

            "/api/cliente/cardapio"
        );


        /* Verificar se a categoria selecionada
           continua disponivel. */

        if (

            categoriaSelecionada !== null &&

            !categoriasCardapio.some(

                categoria =>
                    categoria.id === categoriaSelecionada
            )

        ) {

            categoriaSelecionada = null;
        }


        /* Atualizar os produtos do carrinho. */

        const alterado = sincronizarCarrinho(
            categoriasCardapio
        );

        cardapioValidado = true;

        if (alterado) {

            mostrarMensagem(

                mensagemCliente,

                "Seu carrinho foi atualizado de acordo " +
                "com os preços e produtos disponíveis.",

                "info"
            );
        }


        /* Exibir confirmacao de pedido. */

        const parametros = new URLSearchParams(
            window.location.search
        );

        const pedidoConfirmado = parametros.get(
            "pedidoConfirmado"
        );

        if (

            pedidoConfirmado &&

            /^\d+$/.test(pedidoConfirmado)

        ) {

            mostrarMensagem(

                mensagemCliente,

                "Pedido #" + pedidoConfirmado +
                " confirmado. Consulte o atendente " +
                "para acompanhar seu preparo.",

                "sucesso"
            );

            parametros.delete("pedidoConfirmado");

            const consulta = parametros.toString();

            window.history.replaceState(

                null,

                "",

                window.location.pathname +

                (consulta ? "?" + consulta : "")
            );
        }

    } catch (erro) {

        if (erro.status === 401 || erro.status === 403) {

            limparCarrinho();

            window.location.href =
                "/index.html?expirou";

            return;
        }

        mostrarMensagem(
            mensagemCliente,
            erro.message
        );

    } finally {

        carregandoCardapio = false;

        $("atualizar-cardapio").disabled = false;

        renderizarFiltros();

        renderizarCardapio();

        atualizarResumoCarrinho();
    }
}


/* =========================================
   FILTROS DE CATEGORIAS
   ========================================= */

function renderizarFiltros() {

    const filtros = $("filtros-categorias");

    filtros.replaceChildren();

    filtros.appendChild(

        criarFiltro("Todas", null)
    );

    categoriasCardapio.forEach(categoria => {

        filtros.appendChild(

            criarFiltro(
                categoria.nome,
                categoria.id
            )
        );
    });
}


function criarFiltro(nome, id) {

    const botao = criarBotao(

        nome,

        "filtro-categoria",

        () => {

            categoriaSelecionada = id;

            renderizarFiltros();

            renderizarCardapio();
        }
    );

    const selecionado =
        categoriaSelecionada === id;

    botao.classList.toggle(
        "ativo",
        selecionado
    );

    botao.setAttribute(
        "aria-pressed",
        String(selecionado)
    );

    return botao;
}


/* =========================================
   RENDERIZACAO DO CARDAPIO
   ========================================= */

function renderizarCardapio() {

    const lista = $("lista-cardapio");

    lista.replaceChildren();

    if (

        carregandoCardapio &&

        categoriasCardapio.length === 0

    ) {

        lista.appendChild(

            elemento(

                "p",

                "estado-vazio",

                "Carregando cardápio..."
            )
        );

        return;
    }


    const pesquisa = normalizar(
        $("pesquisa-produto").value
    );

    let quantidadeExibida = 0;


    categoriasCardapio.forEach(categoria => {

        if (

            categoriaSelecionada !== null &&

            categoria.id !== categoriaSelecionada

        ) {

            return;
        }


        const produtos = categoria.produtos.filter(

            produto => {

                const conteudo = normalizar(

                    produto.nome + " " +

                    (produto.descricao || "") + " " +

                    categoria.nome
                );

                return conteudo.includes(pesquisa);
            }
        );


        if (produtos.length === 0) {
            return;
        }


        const secao = elemento(
            "section",
            "secao-categoria"
        );

        const titulo = elemento(

            "h2",

            "titulo-categoria",

            categoria.nome
        );

        secao.appendChild(titulo);


        if (categoria.descricao) {

            secao.appendChild(

                elemento(

                    "p",

                    "descricao-categoria",

                    categoria.descricao
                )
            );
        }


        const grade = elemento(
            "div",
            "grade-produtos"
        );


        produtos.forEach(produto => {

            grade.appendChild(

                criarCartaoProduto(produto)
            );

            quantidadeExibida++;
        });


        secao.appendChild(grade);

        lista.appendChild(secao);
    });


    if (quantidadeExibida === 0) {

        lista.appendChild(

            elemento(

                "p",

                "estado-vazio",

                pesquisa

                    ? "Nenhum produto encontrado."

                    : "Nenhum produto disponível no momento."
            )
        );
    }
}


/* =========================================
   CARTOES DOS PRODUTOS
   ========================================= */

function criarCartaoProduto(produto) {

    const disponivel = produto.disponivel === true;

    const cartao = elemento(
        "article",
        "cartao-produto"
    );

    cartao.classList.toggle(
        "produto-indisponivel",
        !disponivel
    );

    const dados = elemento(
        "div",
        "dados-produto"
    );

    dados.appendChild(
        elemento(
            "h3",
            "",
            produto.nome
        )
    );

    dados.appendChild(
        elemento(
            "p",
            "descricao-produto",
            produto.descricao || "Sem descrição."
        )
    );

    const disponibilidade = elemento(
        "span",
        disponivel
            ? "status-produto disponivel"
            : "status-produto indisponivel",
        disponivel
            ? "Disponível"
            : "Temporariamente indisponível"
    );

    dados.appendChild(disponibilidade);

    const rodape = elemento(
        "div",
        "rodape-produto"
    );

    const preco = elemento(
        "strong",
        "preco-produto",
        formatarMoeda(Number(produto.preco))
    );

    const botao = criarBotao(
        disponivel
            ? "+ Adicionar"
            : "Indisponível",

        "botao-adicionar",

        () => adicionarProduto(produto)
    );

    botao.disabled =
        !disponivel ||
        !podeAdicionarProdutos();

    botao.setAttribute(
        "aria-label",
        disponivel
            ? "Adicionar " + produto.nome + " ao carrinho"
            : produto.nome + " temporariamente indisponível"
    );

    rodape.append(preco, botao);

    cartao.append(dados, rodape);

    return cartao;
}

/* =========================================
   ADICIONAR PRODUTOS
   ========================================= */

function adicionarProduto(produto) {

    if (!podeAdicionarProdutos()) {
        return;
    }

    if (produto.disponivel !== true) {

        mostrarMensagem(
            mensagemCliente,
            "Este produto está temporariamente indisponível."
        );

        return;
    }

    try {

        adicionarProdutoCarrinho(produto);

        atualizarResumoCarrinho();

        mostrarMensagem(
            mensagemCliente,
            produto.nome + " adicionado ao carrinho.",
            "sucesso"
        );

    } catch (erro) {

        mostrarMensagem(
            mensagemCliente,
            erro.message
        );
    }
}

/* =========================================
   RESUMO DO CARRINHO
   ========================================= */

function atualizarResumoCarrinho() {

    const quantidade = quantidadeTotalCarrinho();

    $("quantidade-navegacao").textContent =

        quantidade;

    $("quantidade-carrinho").textContent =

        quantidade +

        (
            quantidade === 1

                ? " item selecionado"

                : " itens selecionados"
        );

    $("total-carrinho").textContent =

        formatarMoeda(valorTotalCarrinho());

    $("barra-carrinho").hidden =

        quantidade === 0;
}


/* =========================================
   EVENTOS
   ========================================= */

$("pesquisa-produto").addEventListener(

    "input",

    renderizarCardapio
);


$("atualizar-cardapio").addEventListener(

    "click",

    carregarCardapio
);


$("botao-sair").addEventListener(

    "click",

    async () => {

        try {

            limparCarrinho();

            await sairDoSistema();

        } catch (erro) {

            mostrarMensagem(
                mensagemCliente,
                erro.message
            );
        }
    }
);


window.addEventListener(

    "pageshow",

    evento => {

        if (evento.persisted) {

            carregarCardapio();
        }
    }
);


/* =========================================
   INICIALIZACAO
   ========================================= */

carregarCardapio();
