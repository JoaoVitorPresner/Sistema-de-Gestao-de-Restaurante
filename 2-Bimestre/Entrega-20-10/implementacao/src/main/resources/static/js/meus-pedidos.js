
const $ = id => document.getElementById(id);

let atendimento = null;

let pedidos = [];

let filtroSelecionado = "TODOS";

let carregandoPedidos = false;

let dadosCarregados = false;

let encerrandoSessao = false;


/* =========================================
   CONFIGURACAO DOS STATUS
   ========================================= */

const STATUS_PEDIDOS = {

    RECEBIDO: {
        nome: "Recebido",
        classe: "status-recebido"
    },

    EM_PREPARO: {
        nome: "Em preparo",
        classe: "status-em-preparo"
    },

    PRONTO: {
        nome: "Pronto",
        classe: "status-pronto"
    },

    ENTREGUE: {
        nome: "Entregue",
        classe: "status-entregue"
    },

    CANCELADO: {
        nome: "Cancelado",
        classe: "status-cancelado"
    }
};


const ETAPAS_PEDIDO = [

    "RECEBIDO",

    "EM_PREPARO",

    "PRONTO",

    "ENTREGUE"
];


const STATUS_ANDAMENTO = [

    "RECEBIDO",

    "EM_PREPARO",

    "PRONTO"
];


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


function exibirMensagem(texto, tipo = "erro") {

    mostrarMensagem(

        $("mensagem-cliente"),

        texto,

        tipo
    );
}


function formatarDataHora(valor) {

    if (!valor) {
        return "Data não informada";
    }

    const data = new Date(valor);

    if (Number.isNaN(data.getTime())) {
        return "Data inválida";
    }

    const dataFormatada = data.toLocaleDateString(
        "pt-BR"
    );

    const horaFormatada = data.toLocaleTimeString(

        "pt-BR",

        {
            hour: "2-digit",
            minute: "2-digit"
        }
    );

    return dataFormatada + " às " + horaFormatada;
}


function formatarNumeroPedido(id) {

    return "#" + String(id).padStart(4, "0");
}


function pedidoEmAndamento(pedido) {

    return STATUS_ANDAMENTO.includes(
        pedido.status
    );
}


/* =========================================
   IDENTIFICACAO DO ATENDIMENTO
   ========================================= */

async function consultarAtendimento() {

    atendimento = await requisicaoApi(

        "/api/cliente/me"
    );

    iniciarCarrinho(atendimento.mesaId);

    $("identificacao-mesa").textContent =

        "Mesa " +

        String(atendimento.numeroMesa)
            .padStart(2, "0");

    $("quantidade-navegacao").textContent =

        quantidadeTotalCarrinho();

    $("aviso-pagamento").hidden =

        atendimento.status !==
        "AGUARDANDO_PAGAMENTO";
}


/* =========================================
   CONSULTA DOS PEDIDOS
   ========================================= */

async function carregarPedidos() {

    if (carregandoPedidos || encerrandoSessao) {
        return;
    }

    carregandoPedidos = true;

    $("atualizar-pedidos").disabled = true;

    $("atualizar-pedidos").textContent =
        "Atualizando...";

    $("lista-pedidos").setAttribute(
        "aria-busy",
        "true"
    );

    $("mensagem-cliente").hidden = true;

    try {

        await consultarAtendimento();

        const resposta = await requisicaoApi(

            "/api/cliente/pedidos"
        );

        if (!Array.isArray(resposta)) {

            throw new Error(

                "O servidor retornou uma listagem inválida."
            );
        }

        pedidos = resposta;

        dadosCarregados = true;

        atualizarIndicadores();

        renderizarPedidos();

        atualizarHorario();

        verificarPedidoConfirmado();

    } catch (erro) {

        if (erro.status === 401 || erro.status === 403) {

            limparCarrinho();

            window.location.href =
                "/index.html?expirou";

            return;
        }

        if (!dadosCarregados) {

            const lista = $("lista-pedidos");

            lista.replaceChildren(

                elemento(

                    "p",

                    "estado-vazio",

                    "Não foi possível carregar seus pedidos."
                )
            );

        } else {

            $("ultima-atualizacao").textContent =

                "Falha na atualização. Dados anteriores exibidos.";
        }

        exibirMensagem(erro.message);

    } finally {

        carregandoPedidos = false;

        $("atualizar-pedidos").disabled =
            encerrandoSessao;

        $("atualizar-pedidos").textContent =
            "Atualizar pedidos";

        $("lista-pedidos").setAttribute(
            "aria-busy",
            "false"
        );
    }
}


/* =========================================
   INDICADORES DO ATENDIMENTO
   ========================================= */

function atualizarIndicadores() {

    const total = pedidos.length;

    const andamento = pedidos.filter(

        pedido => pedidoEmAndamento(pedido)

    ).length;

    const entregues = pedidos.filter(

        pedido => pedido.status === "ENTREGUE"

    ).length;


    /* Somar valores em centavos para evitar
       imprecisoes de ponto flutuante. */

    const valorCentavos = pedidos

        .filter(
            pedido => pedido.status !== "CANCELADO"
        )

        .reduce(

            (total, pedido) =>

                total +

                Math.round(
                    Number(pedido.valorTotal) * 100
                ),

            0
        );


    $("total-pedidos").textContent = total;

    $("pedidos-andamento").textContent = andamento;

    $("pedidos-entregues").textContent = entregues;

    $("valor-atendimento").textContent =

        formatarMoeda(valorCentavos / 100);
}


/* =========================================
   FILTROS
   ========================================= */

function obterPedidosFiltrados() {

    switch (filtroSelecionado) {

        case "ANDAMENTO":

            return pedidos.filter(
                pedido => pedidoEmAndamento(pedido)
            );

        case "ENTREGUE":

            return pedidos.filter(
                pedido => pedido.status === "ENTREGUE"
            );

        case "CANCELADO":

            return pedidos.filter(
                pedido => pedido.status === "CANCELADO"
            );

        default:

            return pedidos;
    }
}


function selecionarFiltro(filtro) {

    filtroSelecionado = filtro;

    document

        .querySelectorAll("[data-filtro]")

        .forEach(botao => {

            const selecionado =

                botao.dataset.filtro === filtro;

            botao.classList.toggle(
                "ativo",
                selecionado
            );

            botao.setAttribute(

                "aria-pressed",

                String(selecionado)
            );
        });

    if (dadosCarregados) {

        renderizarPedidos();
    }
}


/* =========================================
   LISTAGEM DOS PEDIDOS
   ========================================= */

function renderizarPedidos() {

    const lista = $("lista-pedidos");

    lista.replaceChildren();

    const filtrados = obterPedidosFiltrados();


    if (filtrados.length === 0) {

        let mensagem;

        if (pedidos.length === 0) {

            mensagem =

                "Você ainda não realizou pedidos " +
                "neste atendimento.";

        } else {

            mensagem =

                "Nenhum pedido encontrado " +
                "para o filtro selecionado.";
        }

        lista.appendChild(

            elemento(
                "p",
                "estado-vazio",
                mensagem
            )
        );

    } else {

        filtrados.forEach(pedido => {

            lista.appendChild(

                criarCartaoPedido(pedido)
            );
        });
    }


    $("contagem-pedidos").textContent =

        filtrados.length +

        (
            filtrados.length === 1

                ? " pedido exibido"

                : " pedidos exibidos"
        );
}


/* =========================================
   CARTAO INDIVIDUAL DO PEDIDO
   ========================================= */

function criarCartaoPedido(pedido) {

    const configuracao =

        STATUS_PEDIDOS[pedido.status] ||

        {
            nome: "Status desconhecido",
            classe: ""
        };


    const cartao = elemento(

        "article",

        "cartao-pedido " + configuracao.classe
    );

    cartao.id = "pedido-" + pedido.id;


    /* Cabecalho */

    const cabecalho = elemento(
        "div",
        "cabecalho-pedido"
    );

    const identificacao = elemento(
        "div",
        "identificacao-pedido"
    );

    identificacao.appendChild(

        elemento(

            "h3",

            "",

            "Pedido " +
            formatarNumeroPedido(pedido.id)
        )
    );

    identificacao.appendChild(

        elemento(

            "span",

            "data-pedido",

            formatarDataHora(pedido.dataHora)
        )
    );

    const status = elemento(

        "span",

        "status-pedido " + configuracao.classe,

        configuracao.nome
    );

    cabecalho.append(
        identificacao,
        status
    );

    cartao.appendChild(cabecalho);


    /* Progresso ou aviso de cancelamento */

    if (pedido.status === "CANCELADO") {

    cartao.appendChild(

        criarInformacoesCancelamento(pedido)
    );

} else if (ETAPAS_PEDIDO.includes(pedido.status)) {

    cartao.appendChild(

        criarProgressoPedido(pedido.status)
    );
}


    /* Produtos */

    cartao.appendChild(

        criarItensPedido(pedido.itens || [])
    );


    /* Observacoes */

    if (pedido.observacoes) {

        cartao.appendChild(

            criarObservacoesPedido(
                pedido.observacoes
            )
        );
    }


    /* Valor total */

    const total = elemento(
        "div",
        "total-pedido"
    );

    total.append(

        elemento(
            "span",
            "",
            pedido.status === "CANCELADO"
    ? "Valor original"
    : "Total do pedido"
        ),

        elemento(

            "strong",

            "",

            formatarMoeda(
                Number(pedido.valorTotal)
            )
        )
    );

    cartao.appendChild(total);


/* Pedidos cancelados nao geram cobranca */

if (pedido.status === "CANCELADO") {

    const cobranca = elemento(

        "div",

        "valor-cobranca-cancelado"
    );

    cobranca.append(

        elemento(

            "span",

            "",

            "Valor a cobrar"
        ),

        elemento(

            "strong",

            "",

            formatarMoeda(0)
        )
    );

    cartao.appendChild(cobranca);
}

return cartao;
}

/* =========================================
   INFORMACOES DO CANCELAMENTO - RN04
   ========================================= */

function criarInformacoesCancelamento(pedido) {

    const caixa = elemento(

        "div",

        "aviso-pedido-cancelado"
    );


    /* Titulo */

    caixa.appendChild(

        elemento(

            "strong",

            "titulo-pedido-cancelado",

            "Pedido cancelado"
        )
    );


    /* Motivo do cancelamento */

    const informacoes = elemento(

        "dl",

        "dados-cancelamento-cliente"
    );

    informacoes.append(

        elemento(

            "dt",

            "",

            "Motivo do cancelamento"
        ),

        elemento(

            "dd",

            "",

            pedido.motivoCancelamento ||
            "Motivo não informado."
        ),


        /* Data e horario */

        elemento(

            "dt",

            "",

            "Data e horário do cancelamento"
        ),

        elemento(

            "dd",

            "",

            formatarDataHora(
                pedido.dataCancelamento
            )
        )
    );

    caixa.appendChild(informacoes);


    /* Informacao sobre a cobranca */

    caixa.appendChild(

        elemento(

            "p",

            "aviso-cobranca-cancelamento",

            "Este pedido foi cancelado e não será " +
            "contabilizado no total do atendimento."
        )
    );

    return caixa;
}

/* =========================================
   PROGRESSO DO PEDIDO
   ========================================= */

function criarProgressoPedido(statusAtual) {

    const progresso = elemento(

        "ol",

        "progresso-pedido"
    );

    progresso.setAttribute(

        "aria-label",

        "Progresso do pedido"
    );

    const indiceAtual =

        ETAPAS_PEDIDO.indexOf(statusAtual);


    ETAPAS_PEDIDO.forEach((status, indice) => {

        const concluida =

            indice < indiceAtual ||

            statusAtual === "ENTREGUE";

        const atual = indice === indiceAtual;

        const etapa = elemento(
            "li",
            "etapa-pedido"
        );


        if (concluida) {

            etapa.classList.add("concluida");
        }

        if (atual) {

            etapa.classList.add("atual");

            etapa.setAttribute(
                "aria-current",
                "step"
            );
        }


        const marcador = elemento(

            "span",

            "marcador-etapa",

            concluida

                ? "✓"

                : String(indice + 1)
        );

        marcador.setAttribute(
            "aria-hidden",
            "true"
        );


        const nome = elemento(

            "span",

            "nome-etapa",

            STATUS_PEDIDOS[status].nome
        );


        etapa.append(marcador, nome);

        progresso.appendChild(etapa);
    });

    return progresso;
}


/* =========================================
   ITENS DO PEDIDO
   ========================================= */

function criarItensPedido(itens) {

    const lista = elemento(

        "div",

        "itens-pedido"
    );


    if (itens.length === 0) {

        lista.appendChild(

            elemento(

                "p",

                "estado-vazio",

                "Nenhum item registrado."
            )
        );

        return lista;
    }


    itens.forEach(item => {

        const linha = elemento(

            "div",

            "item-pedido"
        );

        const dados = elemento(

            "div",

            "dados-item-pedido"
        );


        dados.appendChild(

            elemento(

                "h4",

                "",

                item.quantidade +

                " × " +

                item.nomeProduto
            )
        );


        dados.appendChild(

            elemento(

                "span",

                "preco-unitario-pedido",

                formatarMoeda(
                    Number(item.precoUnitario)
                ) +

                " por unidade"
            )
        );


        const subtotal = elemento(

            "strong",

            "subtotal-item-pedido",

            formatarMoeda(
                Number(item.subtotal)
            )
        );


        linha.append(dados, subtotal);

        lista.appendChild(linha);
    });

    return lista;
}


/* =========================================
   OBSERVACOES DO PEDIDO
   ========================================= */

function criarObservacoesPedido(observacoes) {

    const caixa = elemento(

        "div",

        "observacoes-pedido"
    );

    caixa.append(

        elemento(
            "strong",
            "",
            "Observações do pedido"
        ),

        elemento(
            "p",
            "",
            observacoes
        )
    );

    return caixa;
}


/* =========================================
   HORARIO DA ULTIMA ATUALIZACAO
   ========================================= */

function atualizarHorario() {

    const horario = new Date().toLocaleTimeString(

        "pt-BR",

        {
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit"
        }
    );

    $("ultima-atualizacao").textContent =

        "Atualizado às " + horario;
}


/* =========================================
   CONFIRMACAO DE NOVO PEDIDO
   ========================================= */

function verificarPedidoConfirmado() {

    const parametros = new URLSearchParams(

        window.location.search
    );

    const id = parametros.get(
        "pedidoConfirmado"
    );

    if (id === null) {
        return;
    }


    /* Somente confirmar pedidos realmente
       retornados pelo backend. */

    const pedido = pedidos.find(

        registro => String(registro.id) === id
    );

    if (pedido) {

        exibirMensagem(

            "Pedido " +

            formatarNumeroPedido(pedido.id) +

            " registrado com sucesso! " +

            "Acompanhe seu andamento nesta página.",

            "sucesso"
        );

    } else {

        exibirMensagem(

            "Não foi possível localizar o pedido " +
            "informado neste atendimento."
        );
    }


    parametros.delete("pedidoConfirmado");

    const consulta = parametros.toString();

    window.history.replaceState(

        null,

        "",

        window.location.pathname +

        (consulta ? "?" + consulta : "")
    );
}


/* =========================================
   ENCERRAMENTO DA SESSAO
   ========================================= */

async function encerrarSessaoCliente() {

    if (encerrandoSessao || carregandoPedidos) {
        return;
    }

    encerrandoSessao = true;

    $("botao-sair").disabled = true;

    $("atualizar-pedidos").disabled = true;

    try {

        limparCarrinho();

        await sairDoSistema();

    } catch (erro) {

        encerrandoSessao = false;

        $("botao-sair").disabled = false;

        $("atualizar-pedidos").disabled = false;

        exibirMensagem(erro.message);
    }
}


/* =========================================
   EVENTOS
   ========================================= */

$("atualizar-pedidos").addEventListener(

    "click",

    carregarPedidos
);


document

    .querySelectorAll("[data-filtro]")

    .forEach(botao => {

        botao.addEventListener(

            "click",

            () => selecionarFiltro(
                botao.dataset.filtro
            )
        );
    });


$("botao-sair").addEventListener(

    "click",

    encerrarSessaoCliente
);


/* Atualizar ao retornar pelo historico
   de navegacao do navegador. */

window.addEventListener(

    "pageshow",

    evento => {

        if (evento.persisted) {

            carregarPedidos();
        }
    }
);


/* =========================================
   INICIALIZACAO
   ========================================= */

carregarPedidos();
