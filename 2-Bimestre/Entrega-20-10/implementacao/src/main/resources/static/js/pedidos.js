
const $ = id => document.getElementById(id);

let funcionario = null;

let pedidos = [];

let pedidoSelecionadoId = null;

let carregandoPedidos = false;

let registrandoEntrega = false;

let registrandoCancelamento = false;

let pedidoCancelamentoId = null;

const STATUS_CANCELAVEIS = [
    "RECEBIDO",
    "EM_PREPARO",
    "PRONTO"
];

let dadosCarregados = false;

let atualizacaoNecessaria = false;

let encerrandoSessao = false;


/* =========================================
   CONFIGURACOES
   ========================================= */

const PERFIS_PERMITIDOS = [

    "ADMINISTRADOR",

    "GERENTE",

    "ATENDENTE"
];


const NOMES_PERFIS = {

    ADMINISTRADOR: "Administrador",

    GERENTE: "Gerente",

    ATENDENTE: "Atendente"
};


const STATUS_PEDIDOS = {

    RECEBIDO: {
        nome: "Recebido",
        classe: "status-recebido"
    },

    EM_PREPARO: {
        nome: "Em preparo",
        classe: "status-preparo"
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


const PRIORIDADE_STATUS = {

    PRONTO: 0,

    EM_PREPARO: 1,

    RECEBIDO: 2,

    ENTREGUE: 3,

    CANCELADO: 4
};


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


function formatarMoeda(valor) {

    return new Intl.NumberFormat(

        "pt-BR",

        {
            style: "currency",
            currency: "BRL"
        }

    ).format(Number(valor));
}


function formatarNumeroPedido(id) {

    return "#" + String(id).padStart(4, "0");
}


function formatarNumeroMesa(numero) {

    return String(numero).padStart(2, "0");
}


function formatarDataHora(valor) {

    if (!valor) {

        return "Horário não informado";
    }

    const data = new Date(valor);

    if (Number.isNaN(data.getTime())) {

        return "Horário inválido";
    }

    return data.toLocaleDateString("pt-BR") +

        " às " +

        data.toLocaleTimeString(

            "pt-BR",

            {
                hour: "2-digit",
                minute: "2-digit"
            }
        );
}


function normalizarTexto(valor) {

    return String(valor ?? "")

        .normalize("NFD")

        .replace(/[\u0300-\u036f]/g, "")

        .toLowerCase()

        .trim();
}


function obterQuantidadeItens(pedido) {

    return (pedido.itens || []).reduce(

        (total, item) =>

            total + Number(item.quantidade),

        0
    );
}


function obterStatus(status) {

    return STATUS_PEDIDOS[status] || {

        nome: "Desconhecido",

        classe: ""
    };
}


function criarStatus(status) {

    const configuracao = obterStatus(status);

    return elemento(

        "span",

        "status-pedido " + configuracao.classe,

        configuracao.nome
    );
}


function buscarPedidoLocal(id) {

    return pedidos.find(

        pedido => pedido.id === id
    );
}


function exibirMensagem(texto, tipo = "erro") {

    mostrarMensagem(

        $("mensagem-pedidos"),

        texto,

        tipo
    );
}


function exibirMensagemOperacao(texto, tipo = "erro") {

    exibirMensagem(texto, tipo);

    if ($("modal-pedido").open) {

        mostrarMensagem(

            $("mensagem-modal"),

            texto,

            tipo
        );
    }
}


function tratarErroAutenticacao(erro) {

    if (erro.status === 401) {

        window.location.href =

            "/index.html?expirou";

        return true;
    }

    if (erro.status === 403) {

        window.location.href =

            "/dashboard.html";

        return true;
    }

    return false;
}


/* =========================================
   IDENTIFICACAO DO FUNCIONARIO
   ========================================= */

async function consultarFuncionario() {

    funcionario = await requisicaoApi(

        "/api/auth/me"
    );

    if (!PERFIS_PERMITIDOS.includes(funcionario.perfil)) {

        const erro = new Error(

            "Você não possui permissão para acessar os pedidos."
        );

        erro.status = 403;

        throw erro;
    }

    const perfil =

        NOMES_PERFIS[funcionario.perfil] ||

        funcionario.perfil;

    $("usuario-atual").textContent =

        funcionario.nome +

        " | " +

        perfil;
}


/* =========================================
   CONTROLE DAS OPERACOES
   ========================================= */

function atualizarControles() {

    const ocupado =
    carregandoPedidos ||
    registrandoEntrega ||
    registrandoCancelamento ||
    encerrandoSessao;


    $("atualizar-pedidos").disabled = ocupado;

    $("botao-sair").disabled = ocupado;

    $("pesquisa-pedido").disabled = ocupado;

    $("filtro-mesa").disabled = ocupado;

    $("filtro-status").disabled = ocupado;

    $("filtro-ordem").disabled = ocupado;

    $("limpar-filtros").disabled = ocupado;


    $("fechar-modal").disabled = ocupado;

    $("voltar-modal").disabled = ocupado;


    document

        .querySelectorAll(".botao-entregar")

        .forEach(botao => {

            botao.disabled =

                ocupado ||

                atualizacaoNecessaria ||

                !dadosCarregados;
        });


    document

        .querySelectorAll(".botao-detalhes-pedido")

        .forEach(botao => {

            botao.disabled = ocupado;
        });


    $("lista-prontos").setAttribute(

        "aria-busy",

        String(

            carregandoPedidos ||

            registrandoEntrega
        )
    );


    $("container-listagem").setAttribute(

        "aria-busy",

        String(

            carregandoPedidos ||

            registrandoEntrega
        )
    );

    document
    .querySelectorAll(".botao-cancelar-pedido")
    .forEach(botao => {

        botao.disabled =
            ocupado ||
            atualizacaoNecessaria ||
            !dadosCarregados;
    });

$("fechar-cancelamento").disabled = ocupado;

$("voltar-cancelamento").disabled = ocupado;

$("motivo-cancelamento").disabled = ocupado;

$("confirmar-cancelamento").disabled =
    ocupado ||
    atualizacaoNecessaria ||
    !dadosCarregados ||
    pedidoCancelamentoId === null ||
    !$("motivo-cancelamento").value.trim();
}


/* =========================================
   CONSULTA DOS PEDIDOS
   ========================================= */

async function consultarPedidos() {

    const resposta = await requisicaoApi(

        "/api/pedidos"
    );

    if (!Array.isArray(resposta)) {

        throw new Error(

            "O servidor retornou uma listagem inválida."
        );
    }


    pedidos = resposta;


    dadosCarregados = true;

    atualizacaoNecessaria = false;


    atualizarIndicadores();

    atualizarFiltroMesas();

    renderizarPedidosProntos();

    renderizarTabela();

    atualizarModalAberto();

    atualizarHorario();
}


async function carregarPedidos() {

    if (

        carregandoPedidos ||

        registrandoEntrega ||

        registrandoCancelamento ||

        encerrandoSessao

    ) {

        return;
    }


    carregandoPedidos = true;


    $("mensagem-pedidos").hidden = true;

    $("mensagem-modal").hidden = true;


    $("atualizar-pedidos").textContent =

        "Atualizando...";


    atualizarControles();


    try {

        await consultarFuncionario();

        await consultarPedidos();


    } catch (erro) {

        if (tratarErroAutenticacao(erro)) {

            return;
        }


        atualizacaoNecessaria = true;


        if (!dadosCarregados) {

            exibirFalhaInicial();

        } else {

            $("ultima-atualizacao").textContent =

                "Falha na atualização. Dados anteriores exibidos.";
        }


        exibirMensagemOperacao(

            erro.message +

            " Atualize os pedidos antes de realizar novas operações."
        );


    } finally {

        carregandoPedidos = false;


        $("atualizar-pedidos").textContent =

            "Atualizar pedidos";


        atualizarControles();
    }
}


function exibirFalhaInicial() {

    $("lista-prontos").replaceChildren(

        elemento(

            "p",

            "estado-vazio-pedidos",

            "Não foi possível carregar os pedidos."
        )
    );


    exibirTabelaVazia(

        "Não foi possível carregar a listagem."
    );


    $("ultima-atualizacao").textContent =

        "Listagem não carregada";
}


/* =========================================
   INDICADORES OPERACIONAIS
   ========================================= */

function atualizarIndicadores() {

    const contagens = {

        RECEBIDO: 0,

        EM_PREPARO: 0,

        PRONTO: 0,

        ENTREGUE: 0,

        CANCELADO: 0
    };


    pedidos.forEach(pedido => {

        if (

            Object.prototype.hasOwnProperty.call(

                contagens,

                pedido.status
            )

        ) {

            contagens[pedido.status]++;
        }
    });


    $("total-pedidos").textContent =

        pedidos.length;


    $("total-recebidos").textContent =

        contagens.RECEBIDO;


    $("total-preparo").textContent =

        contagens.EM_PREPARO;


    $("total-prontos").textContent =

        contagens.PRONTO;


    $("total-entregues").textContent =

        contagens.ENTREGUE;


    $("total-cancelados").textContent =

        contagens.CANCELADO;
}


/* =========================================
   PEDIDOS PRONTOS PARA ENTREGA
   ========================================= */

function renderizarPedidosProntos() {

    const lista = $("lista-prontos");


    const prontos = pedidos

        .filter(pedido =>

            pedido.status === "PRONTO"
        )

        .sort((a, b) =>

            compararData(a, b)
        );


    lista.replaceChildren();


    $("contagem-prontos").textContent =

        prontos.length +

        (

            prontos.length === 1

                ? " pedido"

                : " pedidos"
        );


    if (prontos.length === 0) {

        lista.appendChild(

            elemento(

                "p",

                "estado-vazio-pedidos",

                "Nenhum pedido aguardando entrega."
            )
        );

        return;
    }


    prontos.forEach(pedido => {

        lista.appendChild(

            criarCartaoEntrega(pedido)
        );
    });
}


/* =========================================
   CARTAO DE ENTREGA
   ========================================= */

function criarCartaoEntrega(pedido) {

    const cartao = elemento(

        "article",

        "cartao-entrega"
    );


    /* Cabecalho */

    const cabecalho = elemento(

        "div",

        "cabecalho-cartao-entrega"
    );


    const identificacao = elemento(

        "div",

        "identificacao-entrega"
    );


    identificacao.append(

        elemento(

            "h3",

            "",

            "Mesa " +

            formatarNumeroMesa(pedido.numeroMesa)
        ),

        elemento(

            "span",

            "",

            "Pedido " +

            formatarNumeroPedido(pedido.id)
        )
    );


    cabecalho.append(

        identificacao,

        criarStatus(pedido.status)
    );


    cartao.appendChild(cabecalho);


    /* Horario */

    cartao.appendChild(

        elemento(

            "span",

            "horario-entrega",

            "Pedido realizado em " +

            formatarDataHora(pedido.dataHora)
        )
    );


    /* Produtos */

    const listaItens = elemento(

        "ul",

        "itens-entrega"
    );


    (pedido.itens || []).forEach(item => {

        listaItens.appendChild(

            elemento(

                "li",

                "",

                item.quantidade +

                " × " +

                item.nomeProduto
            )
        );
    });


    cartao.appendChild(listaItens);


    /* Observacoes */

    if (pedido.observacoes) {

        cartao.appendChild(

            elemento(

                "div",

                "observacoes-entrega",

                "Observações: " +

                pedido.observacoes
            )
        );
    }


    /* Rodape */

    const rodape = elemento(

        "div",

        "rodape-cartao-entrega"
    );


    const valor = elemento(

        "strong",

        "valor-entrega",

        formatarMoeda(pedido.valorTotal)
    );


    const acoes = elemento(

        "div",

        "acoes-entrega"
    );


    acoes.append(
    criarBotaoDetalhes(pedido.id),
    criarBotaoEntrega(pedido.id),
    criarBotaoCancelamento(pedido.id)
);


    rodape.append(

        valor,

        acoes
    );


    cartao.appendChild(rodape);


    return cartao;
}


/* =========================================
   BOTOES DOS PEDIDOS
   ========================================= */

function criarBotaoDetalhes(id) {

    const botao = elemento(

        "button",

        "botao-detalhes-pedido",

        "Detalhes"
    );


    botao.type = "button";


    botao.setAttribute(

        "aria-label",

        "Consultar detalhes do pedido " +

        formatarNumeroPedido(id)
    );


    botao.addEventListener(

        "click",

        () => abrirDetalhes(id)
    );


    return botao;
}


function criarBotaoEntrega(id) {

    const botao = elemento(

        "button",

        "botao-entregar",

        "Registrar entrega"
    );


    botao.type = "button";


    botao.setAttribute(

        "aria-label",

        "Registrar entrega do pedido " +

        formatarNumeroPedido(id)
    );


    botao.addEventListener(

        "click",

        () => registrarEntrega(id)
    );


    return botao;
}

function pedidoPodeSerCancelado(pedido) {

    return Boolean(
        pedido &&
        STATUS_CANCELAVEIS.includes(pedido.status)
    );
}


function criarBotaoCancelamento(id) {

    const botao = elemento(
        "button",
        "botao-cancelar-pedido",
        "Cancelar pedido"
    );

    botao.type = "button";

    botao.setAttribute(
        "aria-label",
        "Cancelar pedido " +
        formatarNumeroPedido(id)
    );

    botao.addEventListener(
        "click",
        () => abrirCancelamento(id)
    );

    return botao;
}

/* =========================================
   FILTRO DE MESAS
   ========================================= */

function atualizarFiltroMesas() {

    const seletor = $("filtro-mesa");

    const selecionada = seletor.value;


    const mesas = [

        ...new Set(

            pedidos.map(

                pedido => Number(pedido.numeroMesa)
            )
        )
    ]

        .filter(numero =>

            Number.isInteger(numero) && numero > 0
        )

        .sort((a, b) => a - b);


    seletor.replaceChildren();


    const todas = elemento(

        "option",

        "",

        "Todas as mesas"
    );


    todas.value = "TODAS";


    seletor.appendChild(todas);


    mesas.forEach(numero => {

        const opcao = elemento(

            "option",

            "",

            "Mesa " +

            formatarNumeroMesa(numero)
        );


        opcao.value = String(numero);


        seletor.appendChild(opcao);
    });


    if (mesas.some(numero =>

        String(numero) === selecionada

    )) {

        seletor.value = selecionada;

    } else {

        seletor.value = "TODAS";
    }
}


/* =========================================
   PESQUISA E FILTROS
   ========================================= */

function obterPedidosFiltrados() {

    const pesquisa = normalizarTexto(

        $("pesquisa-pedido").value
    );


    const mesa = $("filtro-mesa").value;

    const status = $("filtro-status").value;

    const ordem = $("filtro-ordem").value;


    const filtrados = pedidos.filter(pedido => {

        const correspondeMesa =

            mesa === "TODAS" ||

            String(pedido.numeroMesa) === mesa;


        const correspondeStatus =

            status === "TODOS" ||

            pedido.status === status;


        const produtos = (pedido.itens || [])

            .map(item => item.nomeProduto)

            .join(" ");


        const textoPesquisa = normalizarTexto(

            String(pedido.id) +

            " " +

            formatarNumeroPedido(pedido.id) +

            " Mesa " +

            formatarNumeroMesa(pedido.numeroMesa) +

            " " +

            produtos
        );


        const correspondePesquisa =

            pesquisa === "" ||

            textoPesquisa.includes(pesquisa);


        return (

            correspondeMesa &&

            correspondeStatus &&

            correspondePesquisa
        );
    });


    return ordenarPedidos(

        filtrados,

        ordem
    );
}


/* =========================================
   ORDENACAO DOS PEDIDOS
   ========================================= */

function compararData(a, b) {

    const dataA = new Date(a.dataHora).getTime();

    const dataB = new Date(b.dataHora).getTime();


    return (

        dataA - dataB ||

        Number(a.id) - Number(b.id)
    );
}


function ordenarPedidos(registros, ordem) {

    return [...registros].sort((a, b) => {

        if (ordem === "ANTIGOS") {

            return compararData(a, b);
        }


        if (ordem === "PRIORIDADE") {

            const prioridadeA =

                PRIORIDADE_STATUS[a.status] ?? 99;


            const prioridadeB =

                PRIORIDADE_STATUS[b.status] ?? 99;


            if (prioridadeA !== prioridadeB) {

                return prioridadeA - prioridadeB;
            }


            if (

                a.status === "ENTREGUE" ||

                a.status === "CANCELADO"

            ) {

                return compararData(b, a);
            }


            return compararData(a, b);
        }


        return compararData(b, a);
    });
}


function limparFiltros() {

    $("pesquisa-pedido").value = "";

    $("filtro-mesa").value = "TODAS";

    $("filtro-status").value = "TODOS";

    $("filtro-ordem").value = "RECENTES";


    if (dadosCarregados) {

        renderizarTabela();
    }
}


/* =========================================
   TABELA DE PEDIDOS
   ========================================= */

function renderizarTabela() {

    if (!dadosCarregados) {

        return;
    }


    const tabela = $("tabela-pedidos");

    const filtrados = obterPedidosFiltrados();


    tabela.replaceChildren();


    if (filtrados.length === 0) {

        exibirTabelaVazia(

            "Nenhum pedido encontrado com os filtros selecionados."
        );

    } else {

        filtrados.forEach(pedido => {

            tabela.appendChild(

                criarLinhaPedido(pedido)
            );
        });
    }


    $("contagem-pedidos").textContent =

        filtrados.length +

        (

            filtrados.length === 1

                ? " pedido exibido de "

                : " pedidos exibidos de "
        ) +

        pedidos.length;


    atualizarControles();
}


function exibirTabelaVazia(mensagem) {

    const linha = elemento("tr", "");


    const celula = elemento(

        "td",

        "estado-vazio-pedidos",

        mensagem
    );


    celula.colSpan = 7;


    linha.appendChild(celula);


    $("tabela-pedidos").replaceChildren(linha);
}


function criarLinhaPedido(pedido) {

    const linha = elemento("tr", "");


    if (pedido.status === "PRONTO") {

        linha.classList.add("linha-pronta");
    }

    if (pedido.status === "CANCELADO") {

    linha.classList.add("linha-cancelada");
}


    /* Identificacao */

    const identificacao = elemento(

        "td",

        "numero-pedido-tabela",

        formatarNumeroPedido(pedido.id)
    );


    /* Mesa */

    const mesa = elemento(

        "td",

        "",

        "Mesa " +

        formatarNumeroMesa(pedido.numeroMesa)
    );


    /* Data e horario */

    const horario = elemento(

        "td",

        "",

        formatarDataHora(pedido.dataHora)
    );


    /* Quantidade */

    const quantidade = obterQuantidadeItens(pedido);


    const itens = elemento(

        "td",

        "",

        quantidade +

        (

            quantidade === 1

                ? " item"

                : " itens"
        )
    );


    /* Situacao */

    const status = elemento("td", "");


    status.appendChild(

        criarStatus(pedido.status)
    );


    /* Valor */

    const valor = elemento(

        "td",

        "valor-pedido-tabela",

        formatarMoeda(pedido.valorTotal)
    );


    /* Acoes */

    const acoes = elemento("td", "");


    const botoes = elemento(

        "div",

        "acoes-pedido-tabela"
    );


    botoes.appendChild(

        criarBotaoDetalhes(pedido.id)
    );


    if (pedido.status === "PRONTO") {

        botoes.appendChild(

            criarBotaoEntrega(pedido.id)
        );
    }

    if (pedidoPodeSerCancelado(pedido)) {

    botoes.appendChild(
        criarBotaoCancelamento(pedido.id)
    );
}
    acoes.appendChild(botoes);


    linha.append(

        identificacao,

        mesa,

        horario,

        itens,

        status,

        valor,

        acoes
    );


    return linha;
}


/* =========================================
   ABERTURA DO MODAL
   ========================================= */

function abrirDetalhes(id) {

    if (

        carregandoPedidos ||

        registrandoEntrega ||

        registrandoCancelamento ||

        encerrandoSessao

    ) {

        return;
    }


    const pedido = buscarPedidoLocal(id);


    if (!pedido) {

        exibirMensagem(

            "O pedido não foi encontrado na listagem."
        );

        return;
    }


    pedidoSelecionadoId = id;


    $("mensagem-modal").hidden = true;


    preencherModal(pedido);


    if (!$("modal-pedido").open) {

        $("modal-pedido").showModal();
    }


    atualizarControles();
}


/* =========================================
   PREENCHIMENTO DO MODAL
   ========================================= */

function preencherModal(pedido) {

    $("titulo-modal").textContent =

        "Detalhes do pedido " +

        formatarNumeroPedido(pedido.id);


    $("identificacao-modal").textContent =

        "Mesa " +

        formatarNumeroMesa(pedido.numeroMesa) +

        " | " +

        formatarDataHora(pedido.dataHora);


    const status = obterStatus(pedido.status);


    $("status-modal").className =

        "status-pedido " +

        status.classe;


    $("status-modal").textContent =

        status.nome;


    renderizarProgresso(pedido.status);

    renderizarItensModal(pedido.itens || []);


    /* Observacoes */

    $("secao-observacoes").hidden =

        !pedido.observacoes;


    $("observacoes-modal").textContent =

        pedido.observacoes || "";


    /* Valor total */

    $("valor-modal").textContent =

        formatarMoeda(pedido.valorTotal);


    /* Botao de entrega */

    $("entregar-modal").hidden =
    pedido.status !== "PRONTO";


$("cancelar-pedido-modal").hidden =
    !pedidoPodeSerCancelado(pedido);


/* Informacoes do cancelamento */

const cancelado =
    pedido.status === "CANCELADO";

$("secao-cancelamento").hidden =
    !cancelado;

$("motivo-cancelamento-modal").textContent =
    cancelado
        ? pedido.motivoCancelamento || "Não informado"
        : "";

$("data-cancelamento-modal").textContent =
    cancelado
        ? formatarDataHora(pedido.dataCancelamento)
        : "";

$("responsavel-cancelamento-modal").textContent =
    cancelado
        ? pedido.canceladoPor || "Não informado"
        : "";

atualizarControles();
}


/* =========================================
   PROGRESSO DO PEDIDO
   ========================================= */

function renderizarProgresso(status) {

    const container = $("progresso-modal");


    container.replaceChildren();


    if (status === "CANCELADO") {

        container.appendChild(

            elemento(

                "div",

                "aviso-cancelado-modal",

                "Este pedido foi cancelado e permanece " +

                "registrado no histórico do restaurante."
            )
        );

        return;
    }


    const indiceAtual = ETAPAS_PEDIDO.indexOf(status);


    if (indiceAtual < 0) {

        container.appendChild(

            elemento(

                "p",

                "",

                "Não foi possível identificar o progresso."
            )
        );

        return;
    }


    const lista = elemento(

        "ol",

        "etapas-pedido-modal"
    );


    lista.setAttribute(

        "aria-label",

        "Progresso do pedido"
    );


    ETAPAS_PEDIDO.forEach((etapa, indice) => {

        const item = elemento(

            "li",

            "etapa-pedido-modal"
        );


        if (

            indice < indiceAtual ||

            status === "ENTREGUE"

        ) {

            item.classList.add("concluida");
        }


        if (indice === indiceAtual) {

            item.classList.add("atual");


            item.setAttribute(

                "aria-current",

                "step"
            );
        }


        const concluida =

            indice < indiceAtual ||

            status === "ENTREGUE";


        const circulo = elemento(

            "span",

            "circulo-etapa-modal",

            concluida

                ? "✓"

                : String(indice + 1)
        );


        const nome = elemento(

            "span",

            "",

            obterStatus(etapa).nome
        );


        item.append(

            circulo,

            nome
        );


        lista.appendChild(item);
    });


    container.appendChild(lista);
}


/* =========================================
   ITENS DO MODAL
   ========================================= */

function renderizarItensModal(itens) {

    const container = $("itens-modal");


    container.replaceChildren();


    if (itens.length === 0) {

        container.appendChild(

            elemento(

                "p",

                "estado-vazio-pedidos",

                "Nenhum produto registrado."
            )
        );

        return;
    }


    itens.forEach(item => {

        const linha = elemento(

            "div",

            "item-modal-pedido"
        );


        const dados = elemento(

            "div",

            "dados-item-modal"
        );


        dados.append(

            elemento(

                "strong",

                "",

                item.quantidade +

                " × " +

                item.nomeProduto
            ),

            elemento(

                "small",

                "",

                formatarMoeda(item.precoUnitario) +

                " por unidade"
            )
        );


        const subtotal = elemento(

            "strong",

            "subtotal-item-modal",

            formatarMoeda(item.subtotal)
        );


        linha.append(

            dados,

            subtotal
        );


        container.appendChild(linha);
    });
}


/* =========================================
   ATUALIZACAO DO MODAL
   ========================================= */

function atualizarModalAberto() {

    if (

        !$("modal-pedido").open ||

        pedidoSelecionadoId === null

    ) {

        return;
    }


    const pedido = buscarPedidoLocal(

        pedidoSelecionadoId
    );


    if (!pedido) {

        $("modal-pedido").close();

        pedidoSelecionadoId = null;

        return;
    }


    preencherModal(pedido);
}


function fecharDetalhes() {

    if (
    registrandoEntrega ||
    registrandoCancelamento ||
    carregandoPedidos
) {
    return;
}


    $("modal-pedido").close();


    pedidoSelecionadoId = null;
}


/* =========================================
   REGISTRO DA ENTREGA
   ========================================= */

async function registrarEntrega(id) {

    if (

        carregandoPedidos ||

        registrandoEntrega ||

        encerrandoSessao ||

        atualizacaoNecessaria ||

        registrandoCancelamento ||

        !dadosCarregados

    ) {

        return;
    }


    const pedido = buscarPedidoLocal(id);


    if (!pedido) {

        exibirMensagemOperacao(

            "O pedido não foi encontrado."
        );

        return;
    }


    if (pedido.status !== "PRONTO") {

        exibirMensagemOperacao(

            "Somente pedidos prontos podem ser entregues."
        );

        return;
    }


    const confirmado = window.confirm(

        "Confirmar a entrega do pedido " +

        formatarNumeroPedido(pedido.id) +

        " da mesa " +

        formatarNumeroMesa(pedido.numeroMesa) +

        "?\n\n" +

        "Confirme somente se os alimentos já foram " +

        "entregues ao cliente."
    );


    if (!confirmado) {

        return;
    }


    registrandoEntrega = true;


    $("mensagem-pedidos").hidden = true;

    $("mensagem-modal").hidden = true;


    atualizarControles();


    let entregaConfirmada = false;


    try {

        await requisicaoApi(

            "/api/pedidos/" +

            encodeURIComponent(id) +

            "/entregar",

            "PATCH"
        );


        entregaConfirmada = true;


        /* Consulta novamente os dados do servidor.
           Nao altera o status antecipadamente. */

        await consultarPedidos();


        exibirMensagemOperacao(

            "Entrega registrada com sucesso para o pedido " +

            formatarNumeroPedido(id) +

            ".",

            "sucesso"
        );


    } catch (erro) {

        if (tratarErroAutenticacao(erro)) {

            return;
        }


        atualizacaoNecessaria = true;


        if (entregaConfirmada) {

            exibirMensagemOperacao(

                "A entrega foi registrada, mas não foi possível " +

                "atualizar a listagem. Feche os detalhes, caso " +

                "estejam abertos, e atualize os pedidos."
            );


        } else if (

            erro.status === 409 ||

            erro.status === 404

        ) {

            exibirMensagemOperacao(

                erro.message +

                " O pedido pode ter sido alterado por outro " +

                "funcionário. Atualize a listagem."
            );


        } else if (

            !erro.status ||

            erro.status >= 500

        ) {

            exibirMensagemOperacao(

                "Não foi possível confirmar o resultado da " +

                "operação. Atualize os pedidos antes de tentar novamente."
            );


        } else {

            exibirMensagemOperacao(

                erro.message +

                " Atualize a listagem antes de continuar."
            );
        }


        $("ultima-atualizacao").textContent =

            "Atualização necessária antes de novas operações.";


    } finally {

        registrandoEntrega = false;


        atualizarControles();
    }
}

/* =========================================
   CANCELAMENTO DE PEDIDOS - RN04
   ========================================= */

function abrirCancelamento(id) {

    if (
        carregandoPedidos ||
        registrandoEntrega ||
        registrandoCancelamento ||
        encerrandoSessao ||
        atualizacaoNecessaria ||
        !dadosCarregados
    ) {
        return;
    }

    const pedido = buscarPedidoLocal(id);

    if (!pedidoPodeSerCancelado(pedido)) {

        exibirMensagemOperacao(
            "Este pedido não pode ser cancelado."
        );

        return;
    }

    pedidoCancelamentoId = id;

    $("form-cancelamento").reset();

    $("mensagem-cancelamento").hidden = true;

    $("titulo-cancelamento").textContent =
        "Cancelar pedido " +
        formatarNumeroPedido(id);

    $("identificacao-cancelamento").textContent =
        "Mesa " +
        formatarNumeroMesa(pedido.numeroMesa) +
        " | " +
        formatarMoeda(pedido.valorTotal);

    atualizarContadorCancelamento();

    $("modal-cancelamento").showModal();

    atualizarControles();

    $("motivo-cancelamento").focus();
}


function atualizarContadorCancelamento() {

    const motivo =
        $("motivo-cancelamento").value;

    $("contador-cancelamento").textContent =
        motivo.length + "/255 caracteres";

    atualizarControles();
}


function fecharCancelamento() {

    if (registrandoCancelamento) {
        return;
    }

    $("modal-cancelamento").close();
}


function limparFormularioCancelamento() {

    pedidoCancelamentoId = null;

    $("form-cancelamento").reset();

    $("mensagem-cancelamento").hidden = true;

    $("contador-cancelamento").textContent =
        "0/255 caracteres";

    atualizarControles();
}


async function cancelarPedido(evento) {

    evento.preventDefault();

    if (
        carregandoPedidos ||
        registrandoEntrega ||
        registrandoCancelamento ||
        encerrandoSessao ||
        atualizacaoNecessaria ||
        !dadosCarregados ||
        pedidoCancelamentoId === null
    ) {
        return;
    }

    const id = pedidoCancelamentoId;

    const pedido = buscarPedidoLocal(id);

    const motivo =
        $("motivo-cancelamento").value.trim();

    if (!pedidoPodeSerCancelado(pedido)) {

        mostrarMensagem(
            $("mensagem-cancelamento"),
            "Este pedido não pode ser cancelado."
        );

        return;
    }

    if (!motivo || motivo.length > 255) {

        mostrarMensagem(
            $("mensagem-cancelamento"),
            "Informe um motivo válido de até 255 caracteres."
        );

        return;
    }

    registrandoCancelamento = true;

    $("mensagem-cancelamento").hidden = true;

    $("mensagem-pedidos").hidden = true;

    $("mensagem-modal").hidden = true;

    $("confirmar-cancelamento").textContent =
        "Cancelando...";

    atualizarControles();

    let cancelamentoConfirmado = false;

    try {

        await requisicaoApi(

            "/api/pedidos/" +
            encodeURIComponent(id) +
            "/cancelar",

            "PATCH",

            {
                motivo: motivo
            }
        );

        cancelamentoConfirmado = true;

        await consultarPedidos();

        $("modal-cancelamento").close();

        exibirMensagemOperacao(

            "Pedido " +
            formatarNumeroPedido(id) +
            " cancelado com sucesso.",

            "sucesso"
        );

    } catch (erro) {

        if (tratarErroAutenticacao(erro)) {
            return;
        }

        if (
            erro.status === 400 &&
            !cancelamentoConfirmado
        ) {

            mostrarMensagem(
                $("mensagem-cancelamento"),
                erro.message
            );

            return;
        }

        atualizacaoNecessaria = true;

        let mensagem;

        if (cancelamentoConfirmado) {

            mensagem =
                "O cancelamento foi registrado, " +
                "mas não foi possível atualizar a listagem. " +
                "Atualize os pedidos.";

        } else if (
            erro.status === 409 ||
            erro.status === 404
        ) {

            mensagem =
                erro.message +
                " O pedido pode ter sido alterado " +
                "por outro funcionário. Atualize a listagem.";

        } else if (
            !erro.status ||
            erro.status >= 500
        ) {

            mensagem =
                "Não foi possível confirmar o resultado " +
                "do cancelamento. Atualize os pedidos " +
                "antes de tentar novamente.";

        } else {

            mensagem =
                erro.message +
                " Atualize a listagem antes de continuar.";
        }

        $("modal-cancelamento").close();

        exibirMensagemOperacao(mensagem);

        $("ultima-atualizacao").textContent =
            "Atualização necessária antes de novas operações.";

    } finally {

        registrandoCancelamento = false;

        $("confirmar-cancelamento").textContent =
            "Confirmar cancelamento";

        atualizarControles();
    }
}

/* =========================================
   ULTIMA ATUALIZACAO
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

        "Atualizado às " +

        horario;
}


/* =========================================
   ENCERRAMENTO DA SESSAO
   ========================================= */

async function encerrarSessao() {

    if (

        encerrandoSessao ||

        carregandoPedidos ||

        registrandoCancelamento ||

        registrandoEntrega

    ) {

        return;
    }


    encerrandoSessao = true;


    atualizarControles();


    try {

        await sairDoSistema();


    } catch (erro) {

        encerrandoSessao = false;


        atualizarControles();


        exibirMensagem(erro.message);
    }
}


/* =========================================
   EVENTOS DA PAGINA
   ========================================= */

$("atualizar-pedidos").addEventListener(

    "click",

    carregarPedidos
);


$("pesquisa-pedido").addEventListener(

    "input",

    renderizarTabela
);


$("filtro-mesa").addEventListener(

    "change",

    renderizarTabela
);


$("filtro-status").addEventListener(

    "change",

    renderizarTabela
);


$("filtro-ordem").addEventListener(

    "change",

    renderizarTabela
);


$("limpar-filtros").addEventListener(

    "click",

    limparFiltros
);


$("fechar-modal").addEventListener(

    "click",

    fecharDetalhes
);


$("voltar-modal").addEventListener(

    "click",

    fecharDetalhes
);


$("entregar-modal").addEventListener(

    "click",

    () => {

        if (pedidoSelecionadoId !== null) {

            registrarEntrega(

                pedidoSelecionadoId
            );
        }
    }
);


$("modal-pedido").addEventListener(

    "cancel",

    evento => {

        if (
    registrandoEntrega ||
    registrandoCancelamento ||
    carregandoPedidos
) {
    return;
}
    }
);


$("modal-pedido").addEventListener(

    "close",

    () => {

        pedidoSelecionadoId = null;

        $("mensagem-modal").hidden = true;
    }
);


$("botao-sair").addEventListener(

    "click",

    encerrarSessao
);


window.addEventListener(

    "pageshow",

    evento => {

        if (evento.persisted) {

            carregarPedidos();
        }
    }
);

/* =========================================
   EVENTOS DO CANCELAMENTO
   ========================================= */

$("cancelar-pedido-modal").addEventListener(

    "click",

    () => {

        if (pedidoSelecionadoId !== null) {

            abrirCancelamento(
                pedidoSelecionadoId
            );
        }
    }
);


$("motivo-cancelamento").addEventListener(

    "input",

    atualizarContadorCancelamento
);


$("form-cancelamento").addEventListener(

    "submit",

    cancelarPedido
);


$("fechar-cancelamento").addEventListener(

    "click",

    fecharCancelamento
);


$("voltar-cancelamento").addEventListener(

    "click",

    fecharCancelamento
);


$("modal-cancelamento").addEventListener(

    "cancel",

    evento => {

        if (registrandoCancelamento) {

            evento.preventDefault();
        }
    }
);


$("modal-cancelamento").addEventListener(

    "close",

    limparFormularioCancelamento
);

/* =========================================
   INICIALIZACAO
   ========================================= */

carregarPedidos();
