
const $ = id => document.getElementById(id);

let produtos = [];
let categorias = [];

const formProduto = $("form-produto");
const formCategoria = $("form-categoria");

const modalProduto = $("modal-produto");
const modalCategoria = $("modal-categoria");

const mensagem = $("mensagem-produtos");

const moeda = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL"
});


/* =========================================
   INICIALIZACAO
   ========================================= */

async function iniciar() {

    try {

        const usuario = await requisicaoApi(
            "/api/auth/me"
        );

        if (!["ADMINISTRADOR", "GERENTE"]
                .includes(usuario.perfil)) {

            window.location.href = "/dashboard.html";
            return;
        }

        $("usuario-atual").textContent =
            usuario.nome + " — " + usuario.perfil;

        await carregarDados();

    } catch (erro) {

        mostrarMensagem(
            mensagem,
            erro.message,
            "erro"
        );
    }
}


async function carregarDados() {

    const [dadosCategorias, dadosProdutos] =
        await Promise.all([

            requisicaoApi("/api/categorias"),

            requisicaoApi("/api/produtos")

        ]);

    categorias = dadosCategorias;
    produtos = dadosProdutos;

    atualizarIndicadores();
    atualizarFiltroCategorias();
    renderizarProdutos();
    renderizarCategorias();
}


/* =========================================
   INDICADORES
   ========================================= */

function atualizarIndicadores() {

    $("total-produtos").textContent =
        produtos.length;

    $("produtos-ativos").textContent =
        produtos.filter(p => p.ativo).length;

    $("produtos-inativos").textContent =
        produtos.filter(p => !p.ativo).length;

    $("total-categorias").textContent =
        categorias.length;
}


/* =========================================
   ABAS
   ========================================= */

function selecionarAba(aba) {

    const mostrarProdutos = aba === "produtos";

    $("painel-produtos").hidden =
        !mostrarProdutos;

    $("painel-categorias").hidden =
        mostrarProdutos;

    $("aba-produtos").classList.toggle(
        "selecionada",
        mostrarProdutos
    );

    $("aba-categorias").classList.toggle(
        "selecionada",
        !mostrarProdutos
    );
}

$("aba-produtos").onclick = () =>
    selecionarAba("produtos");

$("aba-categorias").onclick = () =>
    selecionarAba("categorias");


/* =========================================
   PESQUISA E FILTROS
   ========================================= */

function normalizarTexto(texto) {

    return String(texto || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}


function atualizarFiltroCategorias() {

    const filtro = $("filtro-categoria");

    const valorAnterior = filtro.value;

    filtro.replaceChildren(
        new Option("Todas as categorias", "")
    );

    categorias.forEach(categoria => {

        filtro.add(
            new Option(
                categoria.nome,
                categoria.id
            )
        );
    });

    filtro.value = valorAnterior;

    if (filtro.selectedIndex === -1) {
        filtro.value = "";
    }
}


function filtrarProdutos() {

    const pesquisa = normalizarTexto(
        $("pesquisa-produto").value
    );

    const categoria = $("filtro-categoria").value;

    const status = $("filtro-status-produto").value;

    return produtos.filter(produto => {

        const correspondeNome =
            normalizarTexto(produto.nome)
                .includes(pesquisa);

        const correspondeCategoria =
            !categoria ||
            String(produto.categoria.id) === categoria;

        const correspondeStatus =
            !status ||
            String(produto.ativo) === status;

        return correspondeNome &&
               correspondeCategoria &&
               correspondeStatus;
    });
}


function filtrarCategorias() {

    const pesquisa = normalizarTexto(
        $("pesquisa-categoria").value
    );

    const status =
        $("filtro-status-categoria").value;

    return categorias.filter(categoria => {

        const correspondeNome =
            normalizarTexto(categoria.nome)
                .includes(pesquisa);

        const correspondeStatus =
            !status ||
            String(categoria.ativo) === status;

        return correspondeNome &&
               correspondeStatus;
    });
}


/* =========================================
   COMPONENTES DAS TABELAS
   ========================================= */

function criarCelula(texto) {

    const td = document.createElement("td");

    td.textContent = texto;

    return td;
}


function criarStatus(ativo, feminino = false) {

    const td = document.createElement("td");

    const span = document.createElement("span");

    span.className =
        "status-registro " +
        (ativo ? "status-ativo" : "status-inativo");

    span.textContent = ativo
        ? (feminino ? "Ativa" : "Ativo")
        : (feminino ? "Inativa" : "Inativo");

    td.appendChild(span);

    return td;
}


function criarBotao(texto, classe, acao) {

    const botao = document.createElement("button");

    botao.type = "button";
    botao.textContent = texto;

    botao.className =
        "botao-acao " + classe;

    botao.addEventListener("click", acao);

    return botao;
}


function exibirTabelaVazia(tabela, texto) {

    const linha = document.createElement("tr");

    const celula = criarCelula(texto);

    celula.colSpan = 5;
    celula.className = "sem-registros";

    linha.appendChild(celula);

    tabela.appendChild(linha);
}


/* =========================================
   LISTAGEM DE PRODUTOS
   ========================================= */

function renderizarProdutos() {

    const tabela = $("lista-produtos");

    tabela.replaceChildren();

    const filtrados = filtrarProdutos();

    filtrados.forEach(produto => {

        const linha = document.createElement("tr");

        const celulaNome = document.createElement("td");

        const nome = document.createElement("strong");

        nome.className = "nome-registro";
        nome.textContent = produto.nome;

        celulaNome.appendChild(nome);

        if (produto.descricao) {

            const descricao =
                document.createElement("span");

            descricao.className = "descricao-registro";
            descricao.textContent = produto.descricao;

            celulaNome.appendChild(descricao);
        }

        const celulaAcoes =
            document.createElement("td");

        const acoes = document.createElement("div");

        acoes.className = "acoes-registro";

        acoes.append(

            criarBotao(
                "Editar",
                "botao-editar",
                () => abrirProduto(produto)
            ),

            criarBotao(
                produto.ativo ? "Desativar" : "Reativar",

                produto.ativo
                    ? "botao-desativar"
                    : "botao-reativar",

                evento => alterarStatus(
                    "produtos",
                    produto,
                    evento.currentTarget
                )
            )
        );

        acoes.appendChild(
    criarBotao(
        "Composição",
        "botao-composicao",
        () => abrirComposicao(produto)
    )
);

        celulaAcoes.appendChild(acoes);

        linha.append(

            celulaNome,

            criarCelula(produto.categoria.nome),

            criarCelula(
                moeda.format(Number(produto.preco))
            ),

            criarStatus(produto.ativo),

            celulaAcoes
        );

        tabela.appendChild(linha);
    });

    if (filtrados.length === 0) {

        exibirTabelaVazia(
            tabela,
            "Nenhum produto encontrado."
        );
    }

    $("contagem-produtos").textContent =
        "Exibindo " + filtrados.length +
        " de " + produtos.length + " produtos.";
}


/* =========================================
   LISTAGEM DE CATEGORIAS
   ========================================= */

function renderizarCategorias() {

    const tabela = $("lista-categorias");

    tabela.replaceChildren();

    const filtradas = filtrarCategorias();

    filtradas.forEach(categoria => {

        const linha = document.createElement("tr");

        const quantidadeAtivos = produtos.filter(

            produto =>
                produto.categoria.id === categoria.id &&
                produto.ativo

        ).length;

        const celulaAcoes =
            document.createElement("td");

        const acoes = document.createElement("div");

        acoes.className = "acoes-registro";

        acoes.append(

            criarBotao(
                "Editar",
                "botao-editar",
                () => abrirCategoria(categoria)
            ),

            criarBotao(
                categoria.ativo ? "Desativar" : "Reativar",

                categoria.ativo
                    ? "botao-desativar"
                    : "botao-reativar",

                evento => alterarStatus(
                    "categorias",
                    categoria,
                    evento.currentTarget
                )
            )
        );

        celulaAcoes.appendChild(acoes);

        linha.append(

            criarCelula(categoria.nome),

            criarCelula(categoria.descricao || "—"),

            criarCelula(quantidadeAtivos),

            criarStatus(categoria.ativo, true),

            celulaAcoes
        );

        tabela.appendChild(linha);
    });

    if (filtradas.length === 0) {

        exibirTabelaVazia(
            tabela,
            "Nenhuma categoria encontrada."
        );
    }

    $("contagem-categorias").textContent =
        "Exibindo " + filtradas.length +
        " de " + categorias.length + " categorias.";
}


/* =========================================
   FORMULARIO DE PRODUTOS
   ========================================= */

function abrirProduto(produto = null) {

    formProduto.reset();

    $("erro-produto").hidden = true;

    const campos = formProduto.elements;

    campos.id.value = produto ? produto.id : "";

    $("titulo-modal-produto").textContent =
        produto ? "Editar produto" : "Novo produto";

    const select = campos.categoria;

    select.replaceChildren(
        new Option("Selecione uma categoria", "")
    );

    const permitidas = categorias.filter(categoria =>

        categoria.ativo ||
        (produto && !produto.ativo)

    );

    if (permitidas.length === 0) {

        mostrarMensagem(
            mensagem,
            "Cadastre ou reative uma categoria primeiro.",
            "erro"
        );

        return;
    }

    permitidas.forEach(categoria => {

        const nome = categoria.nome +
            (categoria.ativo ? "" : " (Inativa)");

        select.add(
            new Option(nome, categoria.id)
        );
    });

    if (produto) {

        campos.nome.value = produto.nome;

        campos.descricao.value =
            produto.descricao || "";

        campos.preco.value = produto.preco;

        campos.categoria.value =
            String(produto.categoria.id);
    }

    modalProduto.showModal();
}


formProduto.addEventListener(
    "submit",
    async evento => {

        evento.preventDefault();

        const campos = formProduto.elements;

        const id = campos.id.value;

        const dados = {

            nome: campos.nome.value.trim(),

            descricao: campos.descricao.value.trim(),

            preco: campos.preco.value,

            categoria: {
                id: Number(campos.categoria.value)
            }
        };

        const botao = formProduto.querySelector(
            'button[type="submit"]'
        );

        botao.disabled = true;

        $("erro-produto").hidden = true;

        try {

            await requisicaoApi(

                id
                    ? "/api/produtos/" + id
                    : "/api/produtos",

                id ? "PUT" : "POST",

                dados
            );

        } catch (erro) {

            mostrarMensagem(
                $("erro-produto"),
                erro.message,
                "erro"
            );

            botao.disabled = false;

            return;
        }

        modalProduto.close();

        botao.disabled = false;

        await atualizarAposOperacao(

            id
                ? "Produto atualizado com sucesso."
                : "Produto cadastrado com sucesso."
        );
    }
);


/* =========================================
   FORMULARIO DE CATEGORIAS
   ========================================= */

function abrirCategoria(categoria = null) {

    formCategoria.reset();

    $("erro-categoria").hidden = true;

    const campos = formCategoria.elements;

    campos.id.value =
        categoria ? categoria.id : "";

    $("titulo-modal-categoria").textContent =
        categoria ? "Editar categoria" : "Nova categoria";

    if (categoria) {

        campos.nome.value = categoria.nome;

        campos.descricao.value =
            categoria.descricao || "";
    }

    modalCategoria.showModal();
}


formCategoria.addEventListener(
    "submit",
    async evento => {

        evento.preventDefault();

        const campos = formCategoria.elements;

        const id = campos.id.value;

        const dados = {

            nome: campos.nome.value.trim(),

            descricao: campos.descricao.value.trim()
        };

        const botao = formCategoria.querySelector(
            'button[type="submit"]'
        );

        botao.disabled = true;

        $("erro-categoria").hidden = true;

        try {

            await requisicaoApi(

                id
                    ? "/api/categorias/" + id
                    : "/api/categorias",

                id ? "PUT" : "POST",

                dados
            );

        } catch (erro) {

            mostrarMensagem(
                $("erro-categoria"),
                erro.message,
                "erro"
            );

            botao.disabled = false;

            return;
        }

        modalCategoria.close();

        botao.disabled = false;

        await atualizarAposOperacao(

            id
                ? "Categoria atualizada com sucesso."
                : "Categoria cadastrada com sucesso."
        );
    }
);


/* =========================================
   DESATIVACAO E REATIVACAO
   ========================================= */

async function alterarStatus(
    tipo,
    registro,
    botao
) {

    const desativar = registro.ativo;

    const acao = desativar
        ? "desativar"
        : "reativar";

    if (tipo === "categorias" && desativar) {

        const possuiAtivos = produtos.some(

            produto =>
                produto.categoria.id === registro.id &&
                produto.ativo
        );

        if (possuiAtivos) {

            mostrarMensagem(
                mensagem,
                "A categoria possui produtos ativos. " +
                "Desative ou transfira esses produtos primeiro.",
                "erro"
            );

            return;
        }
    }

    if (tipo === "produtos" && !desativar) {

        const categoria = categorias.find(
            c => c.id === registro.categoria.id
        );

        if (!categoria || !categoria.ativo) {

            mostrarMensagem(
                mensagem,
                "Reative a categoria antes de reativar o produto.",
                "erro"
            );

            return;
        }
    }

    const confirmou = window.confirm(

        "Deseja realmente " + acao +
        ' "' + registro.nome + '"?'
    );

    if (!confirmou) {
        return;
    }

    botao.disabled = true;

    try {

        const url =
            "/api/" + tipo + "/" + registro.id;

        if (desativar) {

            await requisicaoApi(
                url,
                "DELETE"
            );

        } else {

            await requisicaoApi(
                url + "/ativar",
                "PATCH"
            );
        }

        await atualizarAposOperacao(
            "Status atualizado com sucesso."
        );

    } catch (erro) {

        mostrarMensagem(
            mensagem,
            erro.message,
            "erro"
        );

    } finally {

        botao.disabled = false;
    }
}


/* =========================================
   ATUALIZACAO APOS OPERACOES
   ========================================= */

async function atualizarAposOperacao(texto) {

    try {

        await carregarDados();

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

$("novo-produto").onclick = () =>
    abrirProduto();

$("nova-categoria").onclick = () =>
    abrirCategoria();

$("fechar-produto").onclick = () =>
    modalProduto.close();

$("cancelar-produto").onclick = () =>
    modalProduto.close();

$("fechar-categoria").onclick = () =>
    modalCategoria.close();

$("cancelar-categoria").onclick = () =>
    modalCategoria.close();


$("pesquisa-produto").addEventListener(
    "input",
    renderizarProdutos
);

$("filtro-categoria").addEventListener(
    "change",
    renderizarProdutos
);

$("filtro-status-produto").addEventListener(
    "change",
    renderizarProdutos
);

$("pesquisa-categoria").addEventListener(
    "input",
    renderizarCategorias
);

$("filtro-status-categoria").addEventListener(
    "change",
    renderizarCategorias
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

/* =========================================
   COMPOSICAO DOS PRODUTOS
   ========================================= */

const modalComposicao = $("modal-composicao");
const formComposicao = $("form-composicao");
const mensagemComposicao = $("mensagem-composicao");

let produtoComposicao = null;
let ingredientesComposicao = [];
let itensComposicao = [];
let ingredienteEmEdicao = null;
let composicaoOcupada = false;

const unidadesComposicao = {
    UNIDADE: "un",
    GRAMA: "g",
    QUILOGRAMA: "kg",
    MILILITRO: "ml",
    LITRO: "L"
};

const formatoComposicao = new Intl.NumberFormat("pt-BR", {
    maximumFractionDigits: 3
});


function urlComposicao() {

    return "/api/estoque/composicoes/" +
        produtoComposicao.id;
}


function formatarComposicao(valor, unidade) {

    return formatoComposicao.format(Number(valor)) +
        " " + (unidadesComposicao[unidade] || unidade);
}


function bloquearComposicao(bloquear) {

    composicaoOcupada = bloquear;

    modalComposicao.querySelectorAll(
        "button, input, select"
    ).forEach(elemento => {

        elemento.disabled = bloquear;
    });

    if (!bloquear) {

        $("ingrediente-composicao").disabled =
            ingredienteEmEdicao !== null;

        $("salvar-composicao").disabled =
            !$("ingrediente-composicao").value;
    }
}


/* ABERTURA DO MODAL */

async function abrirComposicao(produto) {

    if (composicaoOcupada) {
        return;
    }

    produtoComposicao = produto;

    ingredientesComposicao = [];
    itensComposicao = [];
    ingredienteEmEdicao = null;

    $("nome-produto-composicao").textContent =
        produto.nome;

    mensagemComposicao.hidden = true;

    $("lista-composicao").replaceChildren();

    formComposicao.reset();

    modalComposicao.showModal();

    bloquearComposicao(true);

    try {

        await carregarComposicao();

    } catch (erro) {

        modalComposicao.close();

        mostrarMensagem(mensagem, erro.message);

    } finally {

        bloquearComposicao(false);
    }
}


/* CARREGAMENTO */

async function carregarComposicao() {

    const [produto, ingredientes, composicao] =
        await Promise.all([

            requisicaoApi(
                "/api/produtos/" + produtoComposicao.id
            ),

            requisicaoApi(
                "/api/estoque/ingredientes"
            ),

            requisicaoApi(urlComposicao())
        ]);

    produtoComposicao = produto;

    ingredientesComposicao = ingredientes;

    itensComposicao = composicao;

    $("controle-estoque-produto").checked =
        produto.controlaEstoque;

    limparEdicaoComposicao();

    renderizarComposicao();
}


/* LISTAGEM */

function renderizarComposicao() {

    const tabela = $("lista-composicao");

    tabela.replaceChildren();

    itensComposicao.forEach(item => {

        const linha = document.createElement("tr");

        const status = !item.ingredienteAtivo
            ? "Inativo"
            : item.suficiente
                ? "Suficiente"
                : "Insuficiente";

        const celulaAcoes = document.createElement("td");

        const acoes = document.createElement("div");

        acoes.className = "acoes-registro";

        acoes.append(

            criarBotao(
                "Editar",
                "botao-editar",
                () => editarItemComposicao(item)
            ),

            criarBotao(
                "Remover",
                "botao-desativar",
                () => removerItemComposicao(item)
            )
        );

        celulaAcoes.appendChild(acoes);

        linha.append(

            criarCelula(item.nomeIngrediente),

            criarCelula(
                formatarComposicao(
                    item.quantidade,
                    item.unidadeMedida
                )
            ),

            criarCelula(
                formatarComposicao(
                    item.estoqueDisponivel,
                    item.unidadeMedida
                )
            ),

            criarCelula(status),

            celulaAcoes
        );

        tabela.appendChild(linha);
    });

    if (itensComposicao.length === 0) {

        exibirTabelaVazia(
            tabela,
            "Nenhum ingrediente na composição."
        );
    }
}


/* SELECAO DOS INGREDIENTES */

function preencherIngredientesComposicao() {

    const select = $("ingrediente-composicao");

    select.replaceChildren(
        new Option("Selecione um ingrediente", "")
    );

    ingredientesComposicao.filter(ingrediente => {

        const jaCadastrado = itensComposicao.some(
            item => item.ingredienteId === ingrediente.id
        );

        return (

            ingrediente.ativo && !jaCadastrado

        ) || ingrediente.id === ingredienteEmEdicao;

    }).forEach(ingrediente => {

        select.add(

            new Option(

                ingrediente.nome + " (" +

                unidadesComposicao[
                    ingrediente.unidadeMedida
                ] + ")",

                ingrediente.id
            )
        );
    });

    if (ingredienteEmEdicao !== null) {

        select.value = String(ingredienteEmEdicao);
    }

    select.disabled =
        composicaoOcupada || ingredienteEmEdicao !== null;

    atualizarUnidadeComposicao();
}


function atualizarUnidadeComposicao() {

    const id = Number(
        $("ingrediente-composicao").value
    );

    const ingrediente = ingredientesComposicao.find(
        i => i.id === id
    );

    $("unidade-composicao").textContent = ingrediente

        ? "Unidade: " +
            unidadesComposicao[ingrediente.unidadeMedida]

        : "";

    $("salvar-composicao").disabled =
        composicaoOcupada || !ingrediente;
}


/* EDICAO */

function editarItemComposicao(item) {

    if (composicaoOcupada) {
        return;
    }

    ingredienteEmEdicao = item.ingredienteId;

    $("titulo-form-composicao").textContent =
        "Editar ingrediente da composição";

    $("salvar-composicao").textContent =
        "Salvar alteração";

    $("cancelar-edicao-composicao").hidden = false;

    preencherIngredientesComposicao();

    $("quantidade-composicao").value =
        item.quantidade;

    $("quantidade-composicao").focus();
}


function limparEdicaoComposicao() {

    ingredienteEmEdicao = null;

    formComposicao.reset();

    $("titulo-form-composicao").textContent =
        "Adicionar ingrediente";

    $("salvar-composicao").textContent =
        "Adicionar ingrediente";

    $("cancelar-edicao-composicao").hidden = true;

    preencherIngredientesComposicao();
}


/* EXECUCAO DAS OPERACOES */

async function executarOperacaoComposicao(
    operacao,
    textoSucesso
) {

    if (composicaoOcupada) {
        return;
    }

    bloquearComposicao(true);

    mensagemComposicao.hidden = true;

    try {

        await operacao();

    } catch (erro) {

        mostrarMensagem(
            mensagemComposicao,
            erro.message
        );

        bloquearComposicao(false);

        return;
    }

    try {

        await carregarComposicao();

        mostrarMensagem(
            mensagemComposicao,
            textoSucesso,
            "sucesso"
        );

    } catch (erro) {

        modalComposicao.close();

        mostrarMensagem(

            mensagem,

            "Operação realizada, mas não foi possível " +
            "atualizar a composição. Reabra o produto."
        );

    } finally {

        bloquearComposicao(false);
    }
}


/* CADASTRO E ATUALIZACAO */

formComposicao.addEventListener(
    "submit",
    async evento => {

        evento.preventDefault();

        const ingredienteId = Number(
            $("ingrediente-composicao").value
        );

        const quantidade =
            $("quantidade-composicao").value;

        const editando = ingredienteEmEdicao !== null;

        if (!ingredienteId || Number(quantidade) <= 0) {

            mostrarMensagem(
                mensagemComposicao,
                "Informe um ingrediente e uma quantidade válida."
            );

            return;
        }

        const dados = {
            ingredienteId,
            quantidade
        };

        await executarOperacaoComposicao(

            () => requisicaoApi(

                urlComposicao() +

                    (editando ? "/" + ingredienteId : ""),

                editando ? "PUT" : "POST",

                dados
            ),

            editando
                ? "Composição atualizada com sucesso."
                : "Ingrediente adicionado com sucesso."
        );
    }
);


/* REMOCAO */

async function removerItemComposicao(item) {

    if (composicaoOcupada) {
        return;
    }

    if (!window.confirm(

        "Remover " + item.nomeIngrediente +
        " da composição deste produto?"

    )) {
        return;
    }

    await executarOperacaoComposicao(

        () => requisicaoApi(

            urlComposicao() + "/" + item.ingredienteId,

            "DELETE"
        ),

        "Ingrediente removido da composição."
    );
}


/* CONFIGURACAO DO CONTROLE DE ESTOQUE */

$("salvar-controle-estoque").onclick = async () => {

    const controlaEstoque =
        $("controle-estoque-produto").checked;

    if (!controlaEstoque && itensComposicao.length > 0) {

        $("controle-estoque-produto").checked =
            produtoComposicao.controlaEstoque;

        mostrarMensagem(

            mensagemComposicao,

            "Remova os ingredientes da composição " +
            "antes de desabilitar o controle de estoque."
        );

        return;
    }

    await executarOperacaoComposicao(

        () => requisicaoApi(

            urlComposicao() + "/controle",

            "PATCH",

            { controlaEstoque }
        ),

        "Configuração de estoque atualizada."
    );
};


/* FECHAMENTO E EVENTOS */

function fecharComposicao() {

    if (!composicaoOcupada) {

        modalComposicao.close();
    }
}


$("fechar-composicao").onclick =
    fecharComposicao;

$("concluir-composicao").onclick =
    fecharComposicao;

$("cancelar-edicao-composicao").onclick =
    limparEdicaoComposicao;

$("ingrediente-composicao").addEventListener(

    "change",

    atualizarUnidadeComposicao
);


modalComposicao.addEventListener(
    "cancel",
    evento => {

        if (composicaoOcupada) {

            evento.preventDefault();
        }
    }
);


modalComposicao.addEventListener(
    "close",
    async () => {

        try {

            await carregarDados();

        } catch (erro) {

            mostrarMensagem(
                mensagem,
                erro.message
            );
        }
    }
);

iniciar();
