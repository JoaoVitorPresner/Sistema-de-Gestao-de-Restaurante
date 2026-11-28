
const $ = id => document.getElementById(id);

let funcionario = null;

let pedidos = [];

let carregandoPedidos = false;

let dadosCarregados = false;

let alterandoPedido = false;

let encerrandoSessao = false;

let atualizacaoNecessaria = false;


/* =========================================
   CONFIGURACOES
   ========================================= */

const PERFIS_PERMITIDOS = [

    "ADMINISTRADOR",

    "GERENTE",

    "COZINHEIRO"
];


const NOMES_PERFIS = {

    ADMINISTRADOR: "Administrador",

    GERENTE: "Gerente",

    COZINHEIRO: "Cozinheiro"
};


const COLUNAS = [

    {
        status: "RECEBIDO",
        coluna: "coluna-recebidos",
        lista: "lista-recebidos",
        contagem: "contagem-recebidos",
        mensagem: "Nenhum pedido aguardando preparo."
    },

    {
        status: "EM_PREPARO",
        coluna: "coluna-preparo",
        lista: "lista-preparo",
        contagem: "contagem-preparo",
        mensagem: "Nenhum pedido em preparo."
    },

    {
        status: "PRONTO",
        coluna: "coluna-prontos",
        lista: "lista-prontos",
        contagem: "contagem-prontos",
        mensagem: "Nenhum pedido aguardando entrega."
    }
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

        $("mensagem-cozinha"),

        texto,

        tipo
    );
}


function normalizarTexto(valor) {

    return String(valor ?? "")

        .normalize("NFD")

        .replace(/[\u0300-\u036f]/g, "")

        .toLowerCase()

        .trim();
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

    const dia = data.toLocaleDateString(
        "pt-BR"
    );

    const horario = data.toLocaleTimeString(

        "pt-BR",

        {
            hour: "2-digit",
            minute: "2-digit"
        }
    );

    return dia + " às " + horario;
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

            "Você não possui permissão para acessar a cozinha."
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
   CONTROLE DOS BOTOES
   ========================================= */

function atualizarControles() {

    const ocupado =

        carregandoPedidos ||

        alterandoPedido ||

        encerrandoSessao;

    $("atualizar-cozinha").disabled = ocupado;

    $("botao-sair").disabled = ocupado;

    $("pesquisa-pedido").disabled = ocupado;

    $("filtro-mesa").disabled = ocupado;

    $("filtro-status").disabled = ocupado;

    $("limpar-filtros").disabled = ocupado;

    document

        .querySelectorAll(".botao-acao-cozinha")

        .forEach(botao => {

            botao.disabled =

                ocupado ||

                atualizacaoNecessaria ||

                !dadosCarregados;
        });

    $("quadro-cozinha").setAttribute(

        "aria-busy",

        String(
            carregandoPedidos ||
            alterandoPedido
        )
    );
}


/* =========================================
   CONSULTA DA FILA
   ========================================= */

async function consultarFila() {

    const resposta = await requisicaoApi(

        "/api/cozinha/pedidos"
    );

    if (!Array.isArray(resposta)) {

        throw new Error(

            "O servidor retornou uma fila de pedidos inválida."
        );
    }

    pedidos = resposta

        .filter(pedido =>

            COLUNAS.some(

                coluna => coluna.status === pedido.status
            )
        )

        .sort((a, b) => {

            const diferenca =

                new Date(a.dataHora).getTime() -

                new Date(b.dataHora).getTime();

            return diferenca || Number(a.id) - Number(b.id);
        });

    dadosCarregados = true;

    atualizacaoNecessaria = false;

    atualizarIndicadores();

    atualizarFiltroMesas();

    renderizarFila();

    atualizarHorario();
}


async function carregarPedidos() {

    if (

        carregandoPedidos ||

        alterandoPedido ||

        encerrandoSessao

    ) {

        return;
    }

    carregandoPedidos = true;

    $("mensagem-cozinha").hidden = true;

    $("atualizar-cozinha").textContent =

        "Atualizando...";

    atualizarControles();

    try {

        await consultarFuncionario();

        await consultarFila();

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

        exibirMensagem(

            erro.message +

            " Atualize a fila antes de realizar novas operações."
        );

    } finally {

        carregandoPedidos = false;

        $("atualizar-cozinha").textContent =

            "Atualizar pedidos";

        atualizarControles();
    }
}


function exibirFalhaInicial() {

    COLUNAS.forEach(coluna => {

        $(coluna.lista).replaceChildren(

            elemento(

                "p",

                "cozinha-vazia",

                "Não foi possível carregar os pedidos."
            )
        );
    });

    $("ultima-atualizacao").textContent =

        "Fila não carregada";
}


/* =========================================
   INDICADORES OPERACIONAIS
   ========================================= */

function atualizarIndicadores() {

    const recebidos = pedidos.filter(

        pedido => pedido.status === "RECEBIDO"

    ).length;

    const preparo = pedidos.filter(

        pedido => pedido.status === "EM_PREPARO"

    ).length;

    const prontos = pedidos.filter(

        pedido => pedido.status === "PRONTO"

    ).length;

    $("total-pedidos").textContent = pedidos.length;

    $("total-recebidos").textContent = recebidos;

    $("total-preparo").textContent = preparo;

    $("total-prontos").textContent = prontos;
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

            "Mesa " + formatarNumeroMesa(numero)
        );

        opcao.value = String(numero);

        seletor.appendChild(opcao);
    });


    if (mesas.some(numero => String(numero) === selecionada)) {

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


    return pedidos.filter(pedido => {

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
}


function limparFiltros() {

    $("pesquisa-pedido").value = "";

    $("filtro-mesa").value = "TODAS";

    $("filtro-status").value = "TODOS";

    if (dadosCarregados) {

        renderizarFila();
    }
}


/* =========================================
   RENDERIZACAO DO KANBAN
   ========================================= */

function renderizarFila() {

    if (!dadosCarregados) {

        return;
    }

    const filtrados = obterPedidosFiltrados();

    const statusSelecionado = $("filtro-status").value;


    COLUNAS.forEach(coluna => {

        const secao = $(coluna.coluna);

        const lista = $(coluna.lista);

        const registros = filtrados.filter(

            pedido => pedido.status === coluna.status
        );


        secao.hidden =

            statusSelecionado !== "TODOS" &&

            statusSelecionado !== coluna.status;


        lista.replaceChildren();

        $(coluna.contagem).textContent = registros.length;


        if (registros.length === 0) {

            lista.appendChild(

                elemento(

                    "p",

                    "cozinha-vazia",

                    coluna.mensagem
                )
            );

        } else {

            registros.forEach(pedido => {

                lista.appendChild(

                    criarCartaoPedido(pedido)
                );
            });
        }
    });


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


/* =========================================
   CARTAO INDIVIDUAL DO PEDIDO
   ========================================= */

function criarCartaoPedido(pedido) {

    const cartao = elemento(

        "article",

        "cartao-cozinha"
    );

    cartao.id = "pedido-" + pedido.id;


    /* Cabecalho */

    const cabecalho = elemento(

        "div",

        "cabecalho-cartao-cozinha"
    );

    const identificacao = elemento(

        "div",

        "identificacao-pedido-cozinha"
    );


    identificacao.appendChild(

        elemento(

            "h3",

            "",

            "Mesa " +

            formatarNumeroMesa(pedido.numeroMesa)
        )
    );


    identificacao.appendChild(

        elemento(

            "span",

            "numero-pedido-cozinha",

            "Pedido " +

            formatarNumeroPedido(pedido.id)
        )
    );


    const tempo = elemento(

        "span",

        "tempo-pedido-cozinha",

        calcularTempoDecorrido(pedido.dataHora)
    );

    tempo.dataset.dataHora = pedido.dataHora || "";

    tempo.title = "Tempo desde a realização do pedido";


    cabecalho.append(

        identificacao,

        tempo
    );

    cartao.appendChild(cabecalho);


    /* Horario */

    cartao.appendChild(

        elemento(

            "span",

            "horario-pedido-cozinha",

            "Recebido em " +

            formatarDataHora(pedido.dataHora)
        )
    );


    /* Produtos */

    cartao.appendChild(

        criarItensPedido(pedido.itens || [])
    );


    /* Observacoes */

    if (pedido.observacoes) {

        cartao.appendChild(

            criarObservacoes(pedido.observacoes)
        );
    }


    /* Acoes */

    cartao.appendChild(

        criarAcoesPedido(pedido)
    );

    return cartao;
}


/* =========================================
   PRODUTOS E QUANTIDADES
   ========================================= */

function criarItensPedido(itens) {

    const lista = elemento(

        "ul",

        "itens-cozinha"
    );


    if (itens.length === 0) {

        lista.appendChild(

            elemento(

                "li",

                "cozinha-vazia",

                "Nenhum item registrado."
            )
        );

        return lista;
    }


    itens.forEach(item => {

        const linha = elemento(

            "li",

            "item-cozinha"
        );

        const quantidade = elemento(

            "span",

            "quantidade-item-cozinha",

            item.quantidade + "×"
        );

        const nome = elemento(

            "span",

            "nome-item-cozinha",

            item.nomeProduto
        );

        linha.append(

            quantidade,

            nome
        );

        lista.appendChild(linha);
    });

    return lista;
}


/* =========================================
   OBSERVACOES
   ========================================= */

function criarObservacoes(observacoes) {

    const caixa = elemento(

        "div",

        "observacoes-cozinha"
    );

    caixa.append(

        elemento(

            "strong",

            "",

            "Observações do cliente"
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
   ACOES OPERACIONAIS
   ========================================= */

function criarAcoesPedido(pedido) {

    const caixa = elemento(

        "div",

        "acoes-pedido-cozinha"
    );


    if (pedido.status === "RECEBIDO") {

        const botao = elemento(

            "button",

            "botao-acao-cozinha botao-iniciar-preparo",

            "Iniciar preparo"
        );

        botao.type = "button";

        botao.addEventListener(

            "click",

            () => alterarStatusPedido(

                pedido,

                "iniciar"
            )
        );

        caixa.appendChild(botao);


    } else if (pedido.status === "EM_PREPARO") {

        const botao = elemento(

            "button",

            "botao-acao-cozinha botao-finalizar-preparo",

            "Marcar como pronto"
        );

        botao.type = "button";

        botao.addEventListener(

            "click",

            () => alterarStatusPedido(

                pedido,

                "pronto"
            )
        );

        caixa.appendChild(botao);


    } else if (pedido.status === "PRONTO") {

        caixa.appendChild(

            elemento(

                "div",

                "aviso-pronto-cozinha",

                "Aguardando entrega pelo atendente"
            )
        );
    }

    return caixa;
}


/* =========================================
   ALTERACAO DO STATUS
   ========================================= */

async function alterarStatusPedido(pedido, acao) {

    if (

        carregandoPedidos ||

        alterandoPedido ||

        encerrandoSessao ||

        atualizacaoNecessaria ||

        !dadosCarregados

    ) {

        return;
    }


    if (

        acao !== "iniciar" &&

        acao !== "pronto"

    ) {

        return;
    }


    const statusEsperado =

        acao === "iniciar"

            ? "RECEBIDO"

            : "EM_PREPARO";


    if (pedido.status !== statusEsperado) {

        exibirMensagem(

            "O pedido não está em uma situação válida para esta operação."
        );

        return;
    }


    const mensagemConfirmacao =

        acao === "iniciar"

            ? "Iniciar o preparo do pedido "

            : "Marcar como pronto o pedido ";


    const confirmado = window.confirm(

        mensagemConfirmacao +

        formatarNumeroPedido(pedido.id) +

        " da mesa " +

        formatarNumeroMesa(pedido.numeroMesa) +

        "?"
    );


    if (!confirmado) {

        return;
    }


    alterandoPedido = true;

    $("mensagem-cozinha").hidden = true;

    atualizarControles();


    let alteracaoConfirmada = false;


    try {

        await requisicaoApi(

            "/api/cozinha/pedidos/" +

            encodeURIComponent(pedido.id) +

            "/" +

            acao,

            "PATCH"
        );


        alteracaoConfirmada = true;


        /* Consultar novamente o servidor.
           O estado local nao e alterado
           antecipadamente. */

        await consultarFila();


        const mensagemSucesso =

            acao === "iniciar"

                ? "Preparo iniciado para o pedido "

                : "Pedido marcado como pronto: ";


        exibirMensagem(

            mensagemSucesso +

            formatarNumeroPedido(pedido.id) +

            ".",

            "sucesso"
        );


    } catch (erro) {

        if (tratarErroAutenticacao(erro)) {

            return;
        }


        atualizacaoNecessaria = true;


        if (alteracaoConfirmada) {

            exibirMensagem(

                "A alteração foi registrada, mas não foi possível " +

                "atualizar a fila. Utilize Atualizar pedidos."
            );


        } else if (

            erro.status === 409 ||

            erro.status === 404

        ) {

            exibirMensagem(

                erro.message +

                " O pedido pode ter sido alterado por outro funcionário. " +

                "Atualize a fila antes de continuar."
            );


        } else if (

            !erro.status ||

            erro.status >= 500

        ) {

            exibirMensagem(

                "Não foi possível confirmar o resultado da operação. " +

                "Atualize a fila antes de tentar novamente."
            );


        } else {

            exibirMensagem(

                erro.message +

                " Atualize a fila antes de continuar."
            );
        }


        $("ultima-atualizacao").textContent =

            "Atualização necessária antes de novas operações.";


    } finally {

        alterandoPedido = false;

        atualizarControles();
    }
}


/* =========================================
   TEMPO DECORRIDO
   ========================================= */

function calcularTempoDecorrido(valor) {

    if (!valor) {

        return "Tempo indisponível";
    }

    const data = new Date(valor);

    if (Number.isNaN(data.getTime())) {

        return "Tempo indisponível";
    }


    const diferenca = Math.max(

        0,

        Date.now() - data.getTime()
    );


    const minutosTotais = Math.floor(

        diferenca / 60000
    );


    if (minutosTotais === 0) {

        return "Agora";
    }


    if (minutosTotais < 60) {

        return minutosTotais + " min";
    }


    const horas = Math.floor(

        minutosTotais / 60
    );

    const minutos = minutosTotais % 60;


    if (horas < 24) {

        return horas + "h " + minutos + "min";
    }


    const dias = Math.floor(

        horas / 24
    );

    const horasRestantes = horas % 24;


    return dias + "d " + horasRestantes + "h";
}


function atualizarTemposDecorridos() {

    if (document.hidden) {

        return;
    }

    document

        .querySelectorAll(".tempo-pedido-cozinha")

        .forEach(elementoTempo => {

            elementoTempo.textContent =

                calcularTempoDecorrido(

                    elementoTempo.dataset.dataHora
                );
        });
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

        "Atualizado às " + horario;
}


/* =========================================
   ENCERRAMENTO DA SESSAO
   ========================================= */

async function encerrarSessao() {

    if (

        encerrandoSessao ||

        carregandoPedidos ||

        alterandoPedido

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
   EVENTOS
   ========================================= */

$("atualizar-cozinha").addEventListener(

    "click",

    carregarPedidos
);


$("pesquisa-pedido").addEventListener(

    "input",

    renderizarFila
);


$("filtro-mesa").addEventListener(

    "change",

    renderizarFila
);


$("filtro-status").addEventListener(

    "change",

    renderizarFila
);


$("limpar-filtros").addEventListener(

    "click",

    limparFiltros
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


document.addEventListener(

    "visibilitychange",

    () => {

        if (!document.hidden) {

            atualizarTemposDecorridos();
        }
    }
);


/* =========================================
   INICIALIZACAO
   ========================================= */

carregarPedidos();


/* Atualiza somente os relogios locais.
   Nao realiza requisicoes ao servidor. */

setInterval(

    atualizarTemposDecorridos,

    30000
);
