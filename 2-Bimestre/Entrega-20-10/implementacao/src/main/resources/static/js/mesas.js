
const $ = id => document.getElementById(id);

let mesas = [];
let usuarioAtual = null;

let carregando = false;
let operacaoEmAndamento = false;
let intervaloAtualizacao = null;

const formulario = $("form-mesa");
const modal = $("modal-mesa");
const mensagem = $("mensagem-mesas");


/* =========================================
   CONFIGURACAO DOS ESTADOS
   ========================================= */

const estados = {

    LIVRE: {
        nome: "Livre",
        classe: "livre",
        descricao:
            "Disponível para iniciar um atendimento."
    },

    OCUPADA: {
        nome: "Ocupada",
        classe: "ocupada",
        descricao:
            "Atendimento em andamento."
    },

    AGUARDANDO_PAGAMENTO: {
        nome: "Aguardando pagamento",
        classe: "aguardando-pagamento",
        descricao:
            "Aguardando a quitação da conta."
    },

    INATIVA: {
        nome: "Inativa",
        classe: "inativa",
        descricao:
            "Indisponível para receber clientes."
    }
};


/* =========================================
   PERMISSOES
   ========================================= */

function podeAdministrarMesas() {

    return usuarioAtual &&
        ["ADMINISTRADOR", "GERENTE"]
            .includes(usuarioAtual.perfil);
}


/* =========================================
   INICIALIZACAO
   ========================================= */

async function iniciar() {

    try {

        usuarioAtual = await requisicaoApi(
            "/api/auth/me"
        );

        const perfisPermitidos = [
            "ADMINISTRADOR",
            "GERENTE",
            "ATENDENTE"
        ];

        if (!perfisPermitidos.includes(
            usuarioAtual.perfil
        )) {

            window.location.href = "/dashboard.html";
            return;
        }

        $("usuario-atual").textContent =
            usuarioAtual.nome +
            " — " +
            usuarioAtual.perfil;

        $("nova-mesa").hidden =
            !podeAdministrarMesas();

        await carregarMesas();

        // Atualizacao manual para preservar a expiracao da sessao.

    } catch (erro) {

        mostrarMensagem(
            mensagem,
            erro.message,
            "erro"
        );
    }
}


/* =========================================
   CARREGAMENTO DOS DADOS
   ========================================= */

async function carregarMesas() {

    if (carregando) {
        return;
    }

    carregando = true;

    $("atualizar-mesas").disabled = true;

    try {

        mesas = await requisicaoApi(
            "/api/mesas"
        );

        atualizarIndicadores();

        renderizarMesas();

        $("ultima-atualizacao").textContent =
            "Última atualização: " +
            new Date().toLocaleTimeString("pt-BR");

    } finally {

        carregando = false;

        $("atualizar-mesas").disabled = false;
    }
}


/* =========================================
   ATUALIZACAO AUTOMATICA
   ========================================= */

async function atualizarAutomaticamente() {

    if (
        carregando ||
        operacaoEmAndamento ||
        modal.open ||
        document.hidden
    ) {
        return;
    }

    try {

        await carregarMesas();

    } catch (erro) {

        $("ultima-atualizacao").textContent =
            "Não foi possível atualizar as mesas.";

        if (
            erro.status === 401 ||
            erro.status === 403
        ) {

            clearInterval(intervaloAtualizacao);

            window.location.href =
                "/index.html?expirou";
        }
    }
}


/* =========================================
   INDICADORES
   ========================================= */

function atualizarIndicadores() {

    $("total-mesas").textContent =
        mesas.length;

    $("mesas-livres").textContent =
        mesas.filter(
            mesa => mesa.status === "LIVRE"
        ).length;

    $("mesas-ocupadas").textContent =
        mesas.filter(
            mesa => mesa.status === "OCUPADA"
        ).length;

    $("mesas-pagamento").textContent =
        mesas.filter(
            mesa =>
                mesa.status ===
                "AGUARDANDO_PAGAMENTO"
        ).length;

    $("mesas-inativas").textContent =
        mesas.filter(
            mesa => mesa.status === "INATIVA"
        ).length;
}


/* =========================================
   PESQUISA E FILTROS
   ========================================= */

function filtrarMesas() {

    const pesquisa =
        $("pesquisa-mesa").value.trim();

    const status =
        $("filtro-status").value;

    return mesas.filter(mesa => {

        const numero = String(mesa.numero);

        const numeroFormatado =
            numero.padStart(2, "0");

        const correspondeNumero =
            !pesquisa ||
            numero.includes(pesquisa) ||
            numeroFormatado.includes(pesquisa);

        const correspondeStatus =
            !status || mesa.status === status;

        return correspondeNumero &&
               correspondeStatus;
    });
}


/* =========================================
   COMPONENTES DOS CARTOES
   ========================================= */

function criarElemento(
    tag,
    classe,
    texto = ""
) {

    const elemento = document.createElement(tag);

    elemento.className = classe;
    elemento.textContent = texto;

    return elemento;
}


function criarIconeMesa() {

    const icone = criarElemento(
        "div",
        "icone-mesa"
    );

    icone.setAttribute("aria-hidden", "true");

    // SVG fixo, sem dados fornecidos pelo usuario.

    icone.innerHTML = `
        <svg viewBox="0 0 40 40"
             fill="none"
             xmlns="http://www.w3.org/2000/svg">

            <rect x="11" y="11"
                  width="18" height="18"
                  rx="3"
                  stroke="currentColor"
                  stroke-width="2"/>

            <path d="M15 6 H25
                     M15 34 H25
                     M6 15 V25
                     M34 15 V25"
                  stroke="currentColor"
                  stroke-width="3"
                  stroke-linecap="round"/>

        </svg>
    `;

    return icone;
}


function criarBotao(
    texto,
    classe,
    acao
) {

    const botao = criarElemento(
        "button",
        "botao-mesa " + classe,
        texto
    );

    botao.type = "button";

    botao.addEventListener(
        "click",
        acao
    );

    return botao;
}


/* =========================================
   LISTAGEM DAS MESAS
   ========================================= */

function renderizarMesas() {

    const grade = $("grade-mesas");

    const filtradas = filtrarMesas();

    grade.replaceChildren();

    filtradas.forEach(mesa => {

        grade.appendChild(
            criarCartaoMesa(mesa)
        );
    });

    if (filtradas.length === 0) {

        grade.appendChild(

            criarElemento(
                "p",
                "sem-mesas",
                "Nenhuma mesa encontrada."
            )
        );
    }

    $("contagem-mesas").textContent =
        "Exibindo " +
        filtradas.length +
        " de " +
        mesas.length +
        " mesas.";
}


function criarCartaoMesa(mesa) {

    const estado = estados[mesa.status] || {
        nome: "Desconhecido",
        classe: "inativa",
        descricao: "Situação não reconhecida."
    };

    const cartao = criarElemento(
        "article",
        "cartao-mesa " + estado.classe
    );

    const cabecalho = criarElemento(
        "div",
        "cabecalho-mesa"
    );

    const identificacao = criarElemento(
        "div",
        "identificacao-mesa"
    );

    const numero = criarElemento(
        "h3",
        "",
        "Mesa " +
        String(mesa.numero).padStart(2, "0")
    );

    const capacidade = criarElemento(
        "p",
        "",
        mesa.capacidade +
        (mesa.capacidade === 1
            ? " lugar"
            : " lugares")
    );

    identificacao.append(
        numero,
        capacidade
    );

    cabecalho.append(
        criarIconeMesa(),
        identificacao
    );

    const status = criarElemento(
        "span",
        "status-mesa",
        estado.nome
    );

    const descricao = criarElemento(
        "p",
        "descricao-mesa",
        estado.descricao
    );

    const acoes = criarElemento(
        "div",
        "acoes-mesa"
    );

    adicionarAcoesMesa(
        acoes,
        mesa
    );

    cartao.append(
        cabecalho,
        status,
        descricao,
        acoes
    );

    return cartao;
}


/* =========================================
   ACOES DISPONIVEIS POR ESTADO
   ========================================= */

function adicionarAcoesMesa(
    acoes,
    mesa
) {

    if (mesa.status === "LIVRE") {

        acoes.appendChild(

            criarBotao(
                "Ocupar mesa",
                "principal",
                evento => executarAcao(
                    mesa,
                    "ocupar",
                    "Mesa ocupada com sucesso.",
                    evento.currentTarget
                )
            )
        );

        if (podeAdministrarMesas()) {

            acoes.append(

                criarBotao(
                    "Editar",
                    "",
                    () => abrirFormulario(mesa)
                ),

                criarBotao(
                    "Desativar",
                    "perigo",
                    evento => executarAcao(
                        mesa,
                        "desativar",
                        "Mesa desativada com sucesso.",
                        evento.currentTarget
                    )
                )
            );
        }
    }


    else if (mesa.status === "OCUPADA") {

        acoes.appendChild(

            criarBotao(
                "Solicitar pagamento",
                "principal",
                evento => executarAcao(
                    mesa,
                    "solicitar-pagamento",
                    "Pagamento solicitado com sucesso.",
                    evento.currentTarget
                )
            )
        );
    }


    else if (
        mesa.status === "AGUARDANDO_PAGAMENTO"
    ) {

        acoes.appendChild(

            criarElemento(
                "span",
                "informacao-pagamento",
                "Aguardando encerramento pelo módulo de pagamentos."
            )
        );
    }


    else if (mesa.status === "INATIVA") {

        if (podeAdministrarMesas()) {

            acoes.append(

                criarBotao(
                    "Editar",
                    "",
                    () => abrirFormulario(mesa)
                ),

                criarBotao(
                    "Reativar",
                    "reativar",
                    evento => executarAcao(
                        mesa,
                        "ativar",
                        "Mesa reativada com sucesso.",
                        evento.currentTarget
                    )
                )
            );

        } else {

            acoes.appendChild(

                criarElemento(
                    "span",
                    "descricao-mesa",
                    "Aguardando reativação pela gerência."
                )
            );
        }
    }
}


/* =========================================
   OPERACOES DE ATENDIMENTO
   ========================================= */

async function executarAcao(
    mesa,
    acao,
    textoSucesso,
    botao
) {

    if (
        operacaoEmAndamento ||
        carregando ||
        modal.open
    ) {
        return;
    }

    const descricoes = {

        ocupar: "ocupar",

        "solicitar-pagamento":
            "solicitar o pagamento da",

        desativar: "desativar",

        ativar: "reativar"
    };

    const confirmou = window.confirm(

        "Deseja realmente " +
        descricoes[acao] +
        " mesa " +
        mesa.numero +
        "?"
    );

    if (!confirmou) {
        return;
    }

    operacaoEmAndamento = true;

    botao.disabled = true;

    try {

        await requisicaoApi(

            "/api/mesas/" +
            mesa.id +
            "/" +
            acao,

            "PATCH"
        );

        await atualizarAposOperacao(
            textoSucesso
        );

    } catch (erro) {

        mostrarMensagem(
            mensagem,
            erro.message,
            "erro"
        );

        // Atualiza o estado caso outro funcionario
        // tenha alterado a mesma mesa.

        try {

            await carregarMesas();

        } catch {

            $("ultima-atualizacao").textContent =
                "Não foi possível atualizar as mesas.";
        }

    } finally {

        operacaoEmAndamento = false;

        botao.disabled = false;
    }
}


/* =========================================
   CADASTRO E EDICAO
   ========================================= */

function abrirFormulario(mesa = null) {

    if (
        !podeAdministrarMesas() ||
        operacaoEmAndamento ||
        carregando
    ) {
        return;
    }

    formulario.reset();

    $("erro-mesa").hidden = true;

    const campos = formulario.elements;

    campos.id.value = mesa ? mesa.id : "";

    $("titulo-modal").textContent =
        mesa ? "Editar mesa" : "Nova mesa";

    if (mesa) {

        campos.numero.value = mesa.numero;

        campos.capacidade.value =
            mesa.capacidade;
    }

    modal.showModal();
}


formulario.addEventListener(
    "submit",
    async evento => {

        evento.preventDefault();

        if (
            !podeAdministrarMesas() ||
            operacaoEmAndamento
        ) {
            return;
        }

        const campos = formulario.elements;

        const id = campos.id.value;

        const dados = {

            numero: Number(campos.numero.value),

            capacidade: Number(
                campos.capacidade.value
            )
        };

        if (
            !Number.isInteger(dados.numero) ||
            !Number.isInteger(dados.capacidade) ||
            dados.numero < 1 ||
            dados.capacidade < 1
        ) {

            mostrarMensagem(
                $("erro-mesa"),
                "Informe valores inteiros maiores que zero.",
                "erro"
            );

            return;
        }

        const duplicada = mesas.some(

            mesa =>
                mesa.numero === dados.numero &&
                String(mesa.id) !== id
        );

        if (duplicada) {

            mostrarMensagem(
                $("erro-mesa"),
                "Este número de mesa já está cadastrado.",
                "erro"
            );

            return;
        }

        const botao = formulario.querySelector(
            'button[type="submit"]'
        );

        operacaoEmAndamento = true;

        botao.disabled = true;

        $("erro-mesa").hidden = true;

        try {

            await requisicaoApi(

                id
                    ? "/api/mesas/" + id
                    : "/api/mesas",

                id ? "PUT" : "POST",

                dados
            );

            modal.close();

            await atualizarAposOperacao(

                id
                    ? "Mesa atualizada com sucesso."
                    : "Mesa cadastrada com sucesso."
            );

        } catch (erro) {

            mostrarMensagem(
                $("erro-mesa"),
                erro.message,
                "erro"
            );

        } finally {

            operacaoEmAndamento = false;

            botao.disabled = false;
        }
    }
);


/* =========================================
   ATUALIZACAO APOS OPERACOES
   ========================================= */

async function atualizarAposOperacao(texto) {

    try {

        await carregarMesas();

        mostrarMensagem(
            mensagem,
            texto,
            "sucesso"
        );

    } catch (erro) {

        mostrarMensagem(
            mensagem,
            "Operação realizada, mas não foi possível " +
            "atualizar a listagem. Recarregue a página.",
            "erro"
        );
    }
}


/* =========================================
   EVENTOS
   ========================================= */

$("nova-mesa").onclick = () =>
    abrirFormulario();

$("fechar-modal").onclick = () => {

    if (!operacaoEmAndamento) {
        modal.close();
    }
};

$("cancelar-modal").onclick = () => {

    if (!operacaoEmAndamento) {
        modal.close();
    }
};

modal.addEventListener(
    "cancel",
    evento => {

        if (operacaoEmAndamento) {
            evento.preventDefault();
        }
    }
);


$("pesquisa-mesa").addEventListener(
    "input",
    renderizarMesas
);

$("filtro-status").addEventListener(
    "change",
    renderizarMesas
);


$("atualizar-mesas").addEventListener(
    "click",
    async () => {

        if (operacaoEmAndamento) {
            return;
        }

        try {

            await carregarMesas();

            mostrarMensagem(
                mensagem,
                "Listagem atualizada.",
                "sucesso"
            );

        } catch (erro) {

            mostrarMensagem(
                mensagem,
                erro.message,
                "erro"
            );
        }
    }
);


$("botao-sair").addEventListener(
    "click",
    async () => {

        try {

            await sairDoSistema();

        } catch (erro) {

            mostrarMensagem(
                mensagem,
                erro.message,
                "erro"
            );
        }
    }
);


window.addEventListener(
    "pagehide",
    () => clearInterval(intervaloAtualizacao)
);

window.addEventListener(
    "pageshow",
    evento => {

        if (evento.persisted) {
            window.location.reload();
        }
    }
);


iniciar();
