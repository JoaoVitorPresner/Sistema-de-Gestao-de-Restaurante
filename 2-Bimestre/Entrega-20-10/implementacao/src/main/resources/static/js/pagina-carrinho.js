
const $ = id => document.getElementById(id);

let atendimento = null;

let carregandoCarrinho = false;

let enviandoPedido = false;

let pedidoConfirmado = false;

let envioIncerto = false;

let carrinhoValidado = false;


/* =========================================
   ELEMENTOS AUXILIARES
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


function exibirMensagem(texto, tipo = "erro") {

    mostrarMensagem(
        $("mensagem-cliente"),
        texto,
        tipo
    );
}


function operacaoEmAndamento() {

    return carregandoCarrinho ||
           enviandoPedido ||
           pedidoConfirmado ||
           envioIncerto;
}


function podeConfirmarPedido() {

    return atendimento !== null &&

        atendimento.status === "OCUPADA" &&

        carrinhoValidado &&

        !operacaoEmAndamento() &&

        lerCarrinho().length > 0;
}


/* =========================================
   SESSAO E SINCRONIZACAO
   ========================================= */

async function consultarAtendimento() {

    const dados = await requisicaoApi(
        "/api/cliente/me"
    );

    atendimento = dados;

    iniciarCarrinho(atendimento.mesaId);

    $("identificacao-mesa").textContent =

        "Mesa " +

        String(atendimento.numeroMesa)
            .padStart(2, "0");

    $("aviso-pagamento").hidden =

        atendimento.status !== "AGUARDANDO_PAGAMENTO";
}


async function sincronizarProdutos() {

    const categorias = await requisicaoApi(
        "/api/cliente/cardapio"
    );

    return sincronizarCarrinho(categorias);
}


function tratarErro(erro) {

    if (erro.status === 401 || erro.status === 403) {

        limparCarrinho();

        window.location.href = "/index.html?expirou";

        return;
    }

    exibirMensagem(erro.message);
}


async function carregarCarrinho() {

    if (operacaoEmAndamento()) {
        return;
    }

    carregandoCarrinho = true;

    carrinhoValidado = false;

    $("mensagem-cliente").hidden = true;

    atualizarControles();

    try {

        await consultarAtendimento();

        const alterado = await sincronizarProdutos();

        carrinhoValidado = true;

        if (alterado) {

            exibirMensagem(

                "O carrinho foi atualizado. Alguns produtos " +
                "podem ter mudado de preço ou ficado indisponíveis.",

                "info"
            );
        }

    } catch (erro) {

        tratarErro(erro);

    } finally {

        carregandoCarrinho = false;

        renderizarCarrinho();
    }
}


/* =========================================
   RENDERIZACAO DOS PRODUTOS
   ========================================= */

function renderizarCarrinho() {

    const lista = $("itens-carrinho");

    lista.replaceChildren();

    const itens = lerCarrinho();

    if (itens.length === 0) {

        lista.appendChild(

            elemento(
                "p",
                "estado-vazio",
                "Seu carrinho está vazio."
            )
        );

    } else {

        itens.forEach(item => {

            lista.appendChild(
                criarItemCarrinho(item)
            );
        });
    }

    atualizarResumo();

    atualizarControles();
}


function criarItemCarrinho(item) {

    const artigo = elemento(
        "article",
        "item-carrinho"
    );

    const cabecalho = elemento(
        "div",
        "cabecalho-item-carrinho"
    );

    const dados = elemento(
        "div",
        "dados-item-carrinho"
    );

    dados.appendChild(

        elemento("h3", "", item.nome)
    );

    dados.appendChild(

        elemento(

            "p",

            "",

            formatarMoeda(item.preco) +
            " por unidade"
        )
    );

    const subtotal = elemento(

        "strong",

        "subtotal-item-carrinho",

        formatarMoeda(

            Math.round(item.preco * 100) *
            item.quantidade / 100
        )
    );

    cabecalho.append(dados, subtotal);


    /* Controles de quantidade */

    const acoes = elemento(
        "div",
        "acoes-item"
    );

    const diminuir = criarBotao(

        "−",

        "",

        () => modificarQuantidade(item.id, -1)
    );

    diminuir.setAttribute(
        "aria-label",
        "Diminuir quantidade de " + item.nome
    );

    diminuir.disabled = operacaoEmAndamento();


    const quantidade = elemento(

        "span",

        "quantidade-item-carrinho",

        String(item.quantidade)
    );


    const aumentar = criarBotao(

        "+",

        "",

        () => modificarQuantidade(item.id, 1)
    );

    aumentar.setAttribute(
        "aria-label",
        "Aumentar quantidade de " + item.nome
    );

    aumentar.disabled =

        operacaoEmAndamento() ||

        item.quantidade >= 99 ||

        !atendimento ||

        atendimento.status !== "OCUPADA";


    const remover = criarBotao(

        "Remover",

        "remover-item",

        () => removerItem(item.id)
    );

    remover.setAttribute(
        "aria-label",
        "Remover " + item.nome
    );

    remover.disabled = operacaoEmAndamento();

    acoes.append(
        diminuir,
        quantidade,
        aumentar,
        remover
    );

    artigo.append(cabecalho, acoes);

    return artigo;
}


/* =========================================
   ALTERACAO DOS ITENS
   ========================================= */

function modificarQuantidade(id, diferenca) {

    if (operacaoEmAndamento()) {
        return;
    }

    if (

        diferenca > 0 &&

        (
            !atendimento ||
            atendimento.status !== "OCUPADA"
        )

    ) {
        return;
    }

    alterarQuantidadeCarrinho(id, diferenca);

    renderizarCarrinho();
}


function removerItem(id) {

    if (operacaoEmAndamento()) {
        return;
    }

    removerProdutoCarrinho(id);

    renderizarCarrinho();
}


function esvaziarCarrinho() {

    if (operacaoEmAndamento()) {
        return;
    }

    const confirmado = window.confirm(

        "Deseja remover todos os produtos do carrinho?"
    );

    if (!confirmado) {
        return;
    }

    limparCarrinho();

    $("observacoes").value = "";

    atualizarContadorObservacoes();

    renderizarCarrinho();
}


/* =========================================
   RESUMO E CONTROLES
   ========================================= */

function atualizarResumo() {

    const quantidade = quantidadeTotalCarrinho();

    $("quantidade-navegacao").textContent =

        quantidade;

    $("quantidade-total").textContent =

        quantidade +

        (quantidade === 1 ? " unidade" : " unidades");

    $("valor-total").textContent =

        formatarMoeda(valorTotalCarrinho());
}


function atualizarControles() {

    const ocupado = operacaoEmAndamento();

    $("confirmar-pedido").disabled =

        !podeConfirmarPedido();

    $("atualizar-carrinho").disabled = ocupado;

    $("esvaziar-carrinho").disabled =

        ocupado || lerCarrinho().length === 0;

    $("observacoes").disabled = ocupado;

    $("botao-sair").disabled = enviandoPedido;

    $("itens-carrinho")
        .querySelectorAll("button")
        .forEach(botao => {

            if (ocupado) {
                botao.disabled = true;
            }
        });

    $("confirmar-pedido").textContent =

        pedidoConfirmado

            ? "Pedido confirmado"

            : envioIncerto

                ? "Verifique o envio"

                : enviandoPedido

                    ? "Enviando pedido..."

                    : "Confirmar pedido";
}


function atualizarContadorObservacoes() {

    const quantidade = $("observacoes").value.length;

    $("contador-observacoes").textContent =

        quantidade + "/500 caracteres";
}


/* =========================================
   CONFIRMACAO DO PEDIDO
   ========================================= */

async function confirmarPedido(evento) {

    evento.preventDefault();

    if (!podeConfirmarPedido()) {
        return;
    }

    enviandoPedido = true;

    $("mensagem-cliente").hidden = true;

    renderizarCarrinho();

    let requisicaoIniciada = false;

    try {

        const mesaAnterior = atendimento.mesaId;

        await consultarAtendimento();

        if (atendimento.mesaId !== mesaAnterior) {

            throw new Error(
                "A mesa do atendimento foi alterada. " +
                "Revise seu carrinho."
            );
        }

        if (atendimento.status !== "OCUPADA") {

            throw new Error(

                "A mesa não está disponível " +
                "para novos pedidos."
            );
        }


        /* Conferir disponibilidade e precos */

        const alterado = await sincronizarProdutos();

        if (alterado) {

            exibirMensagem(

                "Os produtos ou preços foram atualizados. " +
                "Revise o carrinho antes de confirmar novamente.",

                "info"
            );

            return;
        }


        const itens = lerCarrinho();

        if (itens.length === 0) {

            throw new Error(
                "Seu carrinho está vazio."
            );
        }


        if (itens.length > 100) {

            throw new Error(

                "O pedido possui produtos demais. " +
                "Divida os itens em mais de um pedido."
            );
        }


        /* Montagem do JSON */

        const observacoes =

            $("observacoes").value.trim();

        const dados = {

            observacoes: observacoes || null,

            itens: itens.map(item => ({

                produtoId: item.id,

                quantidade: item.quantidade
            }))
        };


        /* Envio ao backend */

        requisicaoIniciada = true;

        const pedido = await requisicaoApi(

            "/api/cliente/pedidos",

            "POST",

            dados
        );

        if (!pedido || !Number.isInteger(pedido.id)) {

            throw new Error(
                "Não foi possível confirmar a resposta do servidor."
            );
        }


        /* Pedido registrado com sucesso */

        pedidoConfirmado = true;

        limparCarrinho();

        $("observacoes").value = "";

        atualizarContadorObservacoes();

        exibirMensagem(

            "Pedido #" + pedido.id +
            " registrado com sucesso!",

            "sucesso"
        );


        /* Redirecionamento provisório.
           A tela Meus pedidos será integrada
           na próxima etapa. */

        window.location.href =

    "/meus-pedidos.html?pedidoConfirmado=" +

    encodeURIComponent(pedido.id);

    } catch (erro) {

        if (

            requisicaoIniciada &&

            (!erro.status || erro.status >= 500)

        ) {

            envioIncerto = true;

            exibirMensagem(

                "Não foi possível confirmar se o pedido " +
                "foi registrado. Não envie novamente " +
                "antes de verificar com um atendente."
            );

        } else {

            tratarErro(erro);
        }

    } finally {

        enviandoPedido = false;

        renderizarCarrinho();
    }
}


/* =========================================
   EVENTOS
   ========================================= */

$("form-pedido").addEventListener(

    "submit",

    confirmarPedido
);


$("observacoes").addEventListener(

    "input",

    atualizarContadorObservacoes
);


$("atualizar-carrinho").addEventListener(

    "click",

    carregarCarrinho
);


$("esvaziar-carrinho").addEventListener(

    "click",

    esvaziarCarrinho
);


$("botao-sair").addEventListener(

    "click",

    async () => {

        if (enviandoPedido) {
            return;
        }

        try {

            limparCarrinho();

            await sairDoSistema();

        } catch (erro) {

            exibirMensagem(erro.message);
        }
    }
);


window.addEventListener(

    "pageshow",

    evento => {

        if (evento.persisted) {

            carregarCarrinho();
        }
    }
);


/* =========================================
   INICIALIZACAO
   ========================================= */

atualizarContadorObservacoes();

carregarCarrinho();
