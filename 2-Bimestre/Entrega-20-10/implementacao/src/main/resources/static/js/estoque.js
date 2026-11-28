
const $ = id => document.getElementById(id);

const mensagem = $("mensagem-estoque");

const modalIngrediente = $("modal-ingrediente");
const modalMovimentacao = $("modal-movimentacao");

const formIngrediente = $("form-ingrediente");
const formMovimentacao = $("form-movimentacao");

let ingredientes = [];
let movimentacoes = [];

let operacaoEmAndamento = false;
let consultaHistorico = 0;

const unidades = {
    UNIDADE: "un",
    GRAMA: "g",
    QUILOGRAMA: "kg",
    MILILITRO: "ml",
    LITRO: "L"
};

const tiposMovimentacao = {
    ENTRADA: "Entrada",
    SAIDA_MANUAL: "Saída manual",
    RESERVA: "Reserva",
    LIBERACAO: "Liberação",
    CONSUMO: "Consumo",
    PERDA: "Perda"
};

const formatoQuantidade = new Intl.NumberFormat("pt-BR", {
    maximumFractionDigits: 3
});


/* =========================================
   FUNCOES AUXILIARES
   ========================================= */

function normalizarTexto(texto) {

    return String(texto || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}


function formatarQuantidade(valor, unidade) {

    return formatoQuantidade.format(Number(valor)) +
        " " + (unidades[unidade] || unidade);
}


function formatarData(data) {

    if (!data) {
        return "—";
    }

    const valor = new Date(data);

    return Number.isNaN(valor.getTime())
        ? "—"
        : valor.toLocaleString("pt-BR");
}


function criarCelula(texto, classe = "") {

    const td = document.createElement("td");

    td.textContent = texto;

    if (classe) {
        td.className = classe;
    }

    return td;
}


function criarEtiqueta(texto, classe) {

    const span = document.createElement("span");

    span.className = "status-registro " + classe;

    span.textContent = texto;

    return span;
}


function criarBotao(texto, classe, acao) {

    const botao = document.createElement("button");

    botao.type = "button";
    botao.className = "botao-acao " + classe;
    botao.textContent = texto;

    botao.addEventListener("click", acao);

    return botao;
}


function exibirTabelaVazia(tabela, texto, colunas) {

    const linha = document.createElement("tr");

    const celula = criarCelula(texto, "sem-registros");

    celula.colSpan = colunas;

    linha.appendChild(celula);

    tabela.appendChild(linha);
}


/* =========================================
   INICIALIZACAO
   ========================================= */

async function iniciar() {

    try {

        const usuario = await requisicaoApi("/api/auth/me");

        const permitidos = [
            "ADMINISTRADOR",
            "GERENTE",
            "ESTOQUISTA"
        ];

        if (!permitidos.includes(usuario.perfil)) {

            window.location.href = "/dashboard.html";

            return;
        }

        $("usuario-atual").textContent =
            usuario.nome + " — " + usuario.perfil;

        await carregarEstoque();

    } catch (erro) {

        mostrarMensagem(mensagem, erro.message);
    }
}


/* =========================================
   CARREGAMENTO DOS DADOS
   ========================================= */

async function carregarEstoque() {

    const [dadosIngredientes, resumo] =
        await Promise.all([

            requisicaoApi("/api/estoque/ingredientes"),

            requisicaoApi("/api/estoque/resumo")
        ]);

    ingredientes = dadosIngredientes;

    atualizarIndicadores(resumo);

    atualizarFiltroMovimentacoes();

    renderizarIngredientes();
}


function atualizarIndicadores(resumo) {

    $("total-ingredientes").textContent =
        resumo.totalIngredientes;

    $("ingredientes-ativos").textContent =
        resumo.ingredientesAtivos;

    $("ingredientes-inativos").textContent =
        resumo.ingredientesInativos;

    $("estoque-baixo").textContent =
        resumo.ingredientesEstoqueBaixo;

    $("ingredientes-indisponiveis").textContent =
        resumo.ingredientesIndisponiveis;

    $("ingredientes-reservados").textContent =
        resumo.ingredientesComReserva;
}


async function atualizarAposOperacao(texto) {

    try {

        await carregarEstoque();

        if (!$("painel-movimentacoes").hidden) {

            await carregarMovimentacoes();
        }

        mostrarMensagem(mensagem, texto, "sucesso");

    } catch (erro) {

        mostrarMensagem(
            mensagem,
            "Operação realizada, mas não foi possível " +
            "atualizar os dados. Atualize a página."
        );
    }
}


/* =========================================
   ABAS
   ========================================= */

async function selecionarAba(aba) {

    const mostrarIngredientes = aba === "ingredientes";

    $("painel-ingredientes").hidden =
        !mostrarIngredientes;

    $("painel-movimentacoes").hidden =
        mostrarIngredientes;

    $("aba-ingredientes").classList.toggle(
        "selecionada",
        mostrarIngredientes
    );

    $("aba-movimentacoes").classList.toggle(
        "selecionada",
        !mostrarIngredientes
    );

    if (!mostrarIngredientes) {

        await atualizarHistorico();
    }
}


/* =========================================
   FILTROS DE INGREDIENTES
   ========================================= */

function filtrarIngredientes() {

    const pesquisa = normalizarTexto(
        $("pesquisa-ingrediente").value
    );

    const status = $("filtro-status").value;

    return ingredientes.filter(ingrediente => {

        const nome = normalizarTexto(
            ingrediente.nome + " " +
            (ingrediente.descricao || "")
        );

        if (!nome.includes(pesquisa)) {
            return false;
        }

        switch (status) {

            case "ativo":
                return ingrediente.ativo;

            case "inativo":
                return !ingrediente.ativo;

            case "baixo":
                return ingrediente.ativo &&
                    ingrediente.estoqueBaixo;

            case "indisponivel":
                return ingrediente.ativo &&
                    Number(ingrediente.estoqueDisponivel) <= 0;

            case "reservado":
                return Number(ingrediente.estoqueReservado) > 0;

            default:
                return true;
        }
    });
}


/* =========================================
   STATUS DOS INGREDIENTES
   ========================================= */

function criarStatusIngrediente(ingrediente) {

    const td = document.createElement("td");

    if (!ingrediente.ativo) {

        td.appendChild(
            criarEtiqueta("Inativo", "status-inativo")
        );

    } else if (
        Number(ingrediente.estoqueDisponivel) <= 0
    ) {

        td.appendChild(
            criarEtiqueta(
                "Indisponível",
                "status-indisponivel"
            )
        );

    } else if (ingrediente.estoqueBaixo) {

        td.appendChild(
            criarEtiqueta("Estoque baixo", "status-baixo")
        );

    } else {

        td.appendChild(
            criarEtiqueta("Disponível", "status-ativo")
        );
    }

    return td;
}


/* =========================================
   LISTAGEM DE INGREDIENTES
   ========================================= */

function renderizarIngredientes() {

    const tabela = $("lista-ingredientes");

    tabela.replaceChildren();

    const filtrados = filtrarIngredientes();

    filtrados.forEach(ingrediente => {

        const linha = document.createElement("tr");

        const celulaNome = document.createElement("td");

        const nome = document.createElement("strong");

        nome.className = "nome-registro";
        nome.textContent = ingrediente.nome;

        celulaNome.appendChild(nome);

        if (ingrediente.descricao) {

            const descricao = document.createElement("span");

            descricao.className = "descricao-registro";

            descricao.textContent = ingrediente.descricao;

            celulaNome.appendChild(descricao);
        }

        const unidade = ingrediente.unidadeMedida;

        let classeDisponivel = "saldo-disponivel";

        if (Number(ingrediente.estoqueDisponivel) <= 0) {

            classeDisponivel = "saldo-indisponivel";

        } else if (ingrediente.estoqueBaixo) {

            classeDisponivel = "saldo-baixo";
        }

        const celulaAcoes = document.createElement("td");

        const acoes = document.createElement("div");

        acoes.className = "acoes-registro";

        acoes.append(

            criarBotao(
                "Editar",
                "botao-editar",
                () => abrirIngrediente(ingrediente)
            ),

            criarBotao(

                ingrediente.ativo ? "Desativar" : "Reativar",

                ingrediente.ativo
                    ? "botao-desativar"
                    : "botao-reativar",

                evento => alterarStatusIngrediente(
                    ingrediente,
                    evento.currentTarget
                )
            ),

            criarBotao(
                "Histórico",
                "botao-editar",
                () => abrirHistoricoIngrediente(ingrediente)
            )
        );

        if (ingrediente.ativo) {

            acoes.appendChild(

                criarBotao(
                    "Movimentar",
                    "botao-reativar",
                    () => abrirMovimentacao(ingrediente.id)
                )
            );
        }

        celulaAcoes.appendChild(acoes);

        linha.append(

            celulaNome,

            criarCelula(unidades[unidade] || unidade),

            criarCelula(
                formatarQuantidade(
                    ingrediente.estoqueFisico,
                    unidade
                ),
                "saldo-numerico"
            ),

            criarCelula(
                formatarQuantidade(
                    ingrediente.estoqueReservado,
                    unidade
                ),
                "saldo-numerico saldo-reservado"
            ),

            criarCelula(
                formatarQuantidade(
                    ingrediente.estoqueDisponivel,
                    unidade
                ),
                "saldo-numerico " + classeDisponivel
            ),

            criarCelula(
                formatarQuantidade(
                    ingrediente.estoqueMinimo,
                    unidade
                ),
                "saldo-numerico"
            ),

            criarStatusIngrediente(ingrediente),

            celulaAcoes
        );

        tabela.appendChild(linha);
    });

    if (filtrados.length === 0) {

        exibirTabelaVazia(
            tabela,
            "Nenhum ingrediente encontrado.",
            8
        );
    }

    $("contagem-ingredientes").textContent =
        "Exibindo " + filtrados.length +
        " de " + ingredientes.length + " ingredientes.";
}


/* =========================================
   CADASTRO E EDICAO DE INGREDIENTES
   ========================================= */

function abrirIngrediente(ingrediente = null) {

    if (operacaoEmAndamento) {
        return;
    }

    formIngrediente.reset();

    $("erro-ingrediente").hidden = true;

    const campos = formIngrediente.elements;

    campos.id.value = ingrediente ? ingrediente.id : "";

    $("titulo-modal-ingrediente").textContent =
        ingrediente ? "Editar ingrediente" : "Novo ingrediente";

    if (ingrediente) {

        campos.nome.value = ingrediente.nome;

        campos.descricao.value =
            ingrediente.descricao || "";

        campos.unidadeMedida.value =
            ingrediente.unidadeMedida;

        campos.estoqueMinimo.value =
            ingrediente.estoqueMinimo;
    }

    modalIngrediente.showModal();
}


formIngrediente.addEventListener("submit", async evento => {

    evento.preventDefault();

    if (operacaoEmAndamento) {
        return;
    }

    const campos = formIngrediente.elements;

    const id = campos.id.value;

    const dados = {

        nome: campos.nome.value.trim(),

        descricao: campos.descricao.value.trim(),

        unidadeMedida: campos.unidadeMedida.value,

        estoqueMinimo: campos.estoqueMinimo.value
    };

    const botao = formIngrediente.querySelector(
        'button[type="submit"]'
    );

    operacaoEmAndamento = true;

    botao.disabled = true;

    $("erro-ingrediente").hidden = true;

    try {

        await requisicaoApi(

            "/api/estoque/ingredientes" +
                (id ? "/" + id : ""),

            id ? "PUT" : "POST",

            dados
        );

        modalIngrediente.close();

        await atualizarAposOperacao(

            id
                ? "Ingrediente atualizado com sucesso."
                : "Ingrediente cadastrado com sucesso."
        );

    } catch (erro) {

        mostrarMensagem(
            $("erro-ingrediente"),
            erro.message
        );

    } finally {

        operacaoEmAndamento = false;

        botao.disabled = false;
    }
});


/* =========================================
   ATIVACAO E DESATIVACAO
   ========================================= */

async function alterarStatusIngrediente(
    ingrediente,
    botao
) {

    if (operacaoEmAndamento) {
        return;
    }

    const acao = ingrediente.ativo
        ? "desativar"
        : "ativar";

    const confirmou = window.confirm(

        "Deseja realmente " + acao +
        ' "' + ingrediente.nome + '"?'
    );

    if (!confirmou) {
        return;
    }

    operacaoEmAndamento = true;

    botao.disabled = true;

    try {

        await requisicaoApi(

            "/api/estoque/ingredientes/" +
            ingrediente.id + "/" + acao,

            "PATCH"
        );

        await atualizarAposOperacao(
            "Status do ingrediente atualizado com sucesso."
        );

    } catch (erro) {

        mostrarMensagem(mensagem, erro.message);

    } finally {

        operacaoEmAndamento = false;

        botao.disabled = false;
    }
}


/* =========================================
   REGISTRO DE MOVIMENTACOES
   ========================================= */

function abrirMovimentacao(ingredienteId = null) {

    if (operacaoEmAndamento) {
        return;
    }

    const ativos = ingredientes.filter(i => i.ativo);

    if (ativos.length === 0) {

        mostrarMensagem(
            mensagem,
            "Cadastre ou reative um ingrediente primeiro."
        );

        return;
    }

    formMovimentacao.reset();

    $("erro-movimentacao").hidden = true;

    const select = $("ingrediente-movimentacao");

    select.replaceChildren(
        new Option("Selecione um ingrediente", "")
    );

    ativos.forEach(ingrediente => {

        select.add(

            new Option(

                ingrediente.nome + " (" +
                unidades[ingrediente.unidadeMedida] + ")",

                ingrediente.id
            )
        );
    });

    if (ingredienteId !== null) {

        select.value = String(ingredienteId);
    }

    atualizarSaldoMovimentacao();

    modalMovimentacao.showModal();
}


function atualizarSaldoMovimentacao() {

    const id = $("ingrediente-movimentacao").value;

    const ingrediente = ingredientes.find(
        i => String(i.id) === id
    );

    const saldo = $("saldo-movimentacao");

    if (!ingrediente) {

        saldo.textContent =
            "Selecione um ingrediente para consultar seu saldo.";

        return;
    }

    const unidade = ingrediente.unidadeMedida;

    saldo.textContent =

        "Estoque físico: " +

        formatarQuantidade(
            ingrediente.estoqueFisico,
            unidade
        ) +

        "\nEstoque reservado: " +

        formatarQuantidade(
            ingrediente.estoqueReservado,
            unidade
        ) +

        "\nEstoque disponível: " +

        formatarQuantidade(
            ingrediente.estoqueDisponivel,
            unidade
        );
}


formMovimentacao.addEventListener("submit", async evento => {

    evento.preventDefault();

    if (operacaoEmAndamento) {
        return;
    }

    const campos = formMovimentacao.elements;

    const dados = {

        ingredienteId: Number(campos.ingredienteId.value),

        tipo: campos.tipo.value,

        quantidade: campos.quantidade.value,

        motivo: campos.motivo.value.trim()
    };

    const ingrediente = ingredientes.find(
        i => i.id === dados.ingredienteId
    );

    if (!ingrediente) {

        mostrarMensagem(
            $("erro-movimentacao"),
            "Selecione um ingrediente válido."
        );

        return;
    }

    if (!dados.motivo) {

        mostrarMensagem(
            $("erro-movimentacao"),
            "Informe o motivo da movimentação."
        );

        return;
    }

    if (

        dados.tipo === "SAIDA_MANUAL" &&

        Number(dados.quantidade) >
            Number(ingrediente.estoqueDisponivel)

    ) {

        mostrarMensagem(
            $("erro-movimentacao"),
            "A quantidade supera o estoque disponível."
        );

        return;
    }

    const confirmou = window.confirm(

        "Confirma a movimentação de " +

        formatarQuantidade(
            dados.quantidade,
            ingrediente.unidadeMedida
        ) +

        " de " + ingrediente.nome + "?"
    );

    if (!confirmou) {
        return;
    }

    const botao = formMovimentacao.querySelector(
        'button[type="submit"]'
    );

    operacaoEmAndamento = true;

    botao.disabled = true;

    $("erro-movimentacao").hidden = true;

    let resultadoIncerto = false;

    try {

        await requisicaoApi(

            "/api/estoque/movimentacoes",

            "POST",

            dados
        );

        modalMovimentacao.close();

        await atualizarAposOperacao(
            "Movimentação registrada com sucesso."
        );

    } catch (erro) {

        if (!erro.status || erro.status >= 500) {

            resultadoIncerto = true;

            mostrarMensagem(

                $("erro-movimentacao"),

                "Não foi possível confirmar o resultado. " +
                "Feche esta janela e consulte o histórico " +
                "antes de tentar novamente."
            );

        } else {

            mostrarMensagem(
                $("erro-movimentacao"),
                erro.message
            );
        }

    } finally {

        operacaoEmAndamento = false;

        botao.disabled = resultadoIncerto;
    }
});


/* =========================================
   FILTROS DE MOVIMENTACOES
   ========================================= */

function atualizarFiltroMovimentacoes() {

    const filtro = $("filtro-ingrediente-movimentacao");

    const anterior = filtro.value;

    filtro.replaceChildren(
        new Option("Todos os ingredientes", "")
    );

    ingredientes.forEach(ingrediente => {

        filtro.add(
            new Option(ingrediente.nome, ingrediente.id)
        );
    });

    filtro.value = anterior;

    if (filtro.selectedIndex === -1) {

        filtro.value = "";
    }
}


async function carregarMovimentacoes() {

    const consultaAtual = ++consultaHistorico;

    const ingrediente =
        $("filtro-ingrediente-movimentacao").value;

    const tipo =
        $("filtro-tipo-movimentacao").value;

    let url = "/api/estoque/movimentacoes";

    if (ingrediente) {

        url += "/ingrediente/" + ingrediente;

    } else if (tipo) {

        url += "/tipo/" + tipo;
    }

    let dados = await requisicaoApi(url);

    if (consultaAtual !== consultaHistorico) {
        return;
    }

    if (ingrediente && tipo) {

        dados = dados.filter(m => m.tipo === tipo);
    }

    movimentacoes = dados;

    renderizarMovimentacoes();
}


async function atualizarHistorico() {

    try {

        await carregarMovimentacoes();

    } catch (erro) {

        mostrarMensagem(mensagem, erro.message);
    }
}


async function abrirHistoricoIngrediente(ingrediente) {

    $("filtro-ingrediente-movimentacao").value =
        String(ingrediente.id);

    $("filtro-tipo-movimentacao").value = "";

    await selecionarAba("movimentacoes");
}


/* =========================================
   LISTAGEM DE MOVIMENTACOES
   ========================================= */

function renderizarMovimentacoes() {

    const tabela = $("lista-movimentacoes");

    tabela.replaceChildren();

    movimentacoes.forEach(movimentacao => {

        const linha = document.createElement("tr");

        const unidade = movimentacao.unidadeMedida;

        const celulaTipo = document.createElement("td");

        celulaTipo.appendChild(

            criarEtiqueta(

                tiposMovimentacao[movimentacao.tipo] ||
                    movimentacao.tipo,

                "tipo-" + movimentacao.tipo.toLowerCase()
            )
        );

        const saldoFisico =

            formatarQuantidade(
                movimentacao.fisicoAnterior,
                unidade
            ) +

            " → " +

            formatarQuantidade(
                movimentacao.fisicoPosterior,
                unidade
            );

        const saldoReservado =

            formatarQuantidade(
                movimentacao.reservadoAnterior,
                unidade
            ) +

            " → " +

            formatarQuantidade(
                movimentacao.reservadoPosterior,
                unidade
            );

        linha.append(

            criarCelula(
                formatarData(movimentacao.dataHora)
            ),

            criarCelula(movimentacao.nomeIngrediente),

            celulaTipo,

            criarCelula(

                formatarQuantidade(
                    movimentacao.quantidade,
                    unidade
                ),

                "saldo-numerico"
            ),

            criarCelula(saldoFisico),

            criarCelula(saldoReservado),

            criarCelula(

                movimentacao.pedidoId
                    ? "#" + movimentacao.pedidoId
                    : "—"
            ),

            criarCelula(
                movimentacao.responsavel || "Sistema"
            ),

            criarCelula(movimentacao.motivo || "—")
        );

        tabela.appendChild(linha);
    });

    if (movimentacoes.length === 0) {

        exibirTabelaVazia(
            tabela,
            "Nenhuma movimentação encontrada.",
            9
        );
    }

    $("contagem-movimentacoes").textContent =
        movimentacoes.length + " movimentações encontradas.";
}


/* =========================================
   EVENTOS
   ========================================= */

$("aba-ingredientes").onclick = () =>
    selecionarAba("ingredientes");

$("aba-movimentacoes").onclick = () =>
    selecionarAba("movimentacoes");

$("novo-ingrediente").onclick = () =>
    abrirIngrediente();

$("nova-movimentacao").onclick = () =>
    abrirMovimentacao();

$("registrar-movimentacao").onclick = () =>
    abrirMovimentacao();

$("pesquisa-ingrediente").addEventListener(
    "input",
    renderizarIngredientes
);

$("filtro-status").addEventListener(
    "change",
    renderizarIngredientes
);

$("ingrediente-movimentacao").addEventListener(
    "change",
    atualizarSaldoMovimentacao
);

$("filtro-ingrediente-movimentacao").addEventListener(
    "change",
    atualizarHistorico
);

$("filtro-tipo-movimentacao").addEventListener(
    "change",
    atualizarHistorico
);

$("atualizar-movimentacoes").onclick =
    atualizarHistorico;


$("atualizar-estoque").onclick = async () => {

    if (operacaoEmAndamento) {
        return;
    }

    try {

        await carregarEstoque();

        mostrarMensagem(
            mensagem,
            "Estoque atualizado.",
            "sucesso"
        );

    } catch (erro) {

        mostrarMensagem(mensagem, erro.message);
    }
};


function fecharModal(modal) {

    if (!operacaoEmAndamento) {

        modal.close();
    }
}


$("fechar-ingrediente").onclick = () =>
    fecharModal(modalIngrediente);

$("cancelar-ingrediente").onclick = () =>
    fecharModal(modalIngrediente);

$("fechar-movimentacao").onclick = () =>
    fecharModal(modalMovimentacao);

$("cancelar-movimentacao").onclick = () =>
    fecharModal(modalMovimentacao);


[modalIngrediente, modalMovimentacao].forEach(modal => {

    modal.addEventListener("cancel", evento => {

        if (operacaoEmAndamento) {

            evento.preventDefault();
        }
    });
});


modalMovimentacao.addEventListener("close", () => {

    formMovimentacao.querySelector(
        'button[type="submit"]'
    ).disabled = false;
});


$("botao-sair").onclick = async () => {

    if (operacaoEmAndamento) {
        return;
    }

    try {

        await sairDoSistema();

    } catch (erro) {

        mostrarMensagem(mensagem, erro.message);
    }
};


iniciar();