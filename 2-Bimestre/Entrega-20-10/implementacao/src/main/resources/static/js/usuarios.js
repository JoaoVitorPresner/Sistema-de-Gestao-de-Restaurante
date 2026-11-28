"use strict";

const API_USUARIOS = "/api/usuarios";

const PERFIS_USUARIOS = Object.freeze({
    ADMINISTRADOR: "Administrador",
    GERENTE: "Gerente",
    ATENDENTE: "Atendente",
    COZINHEIRO: "Cozinheiro",
    ESTOQUISTA: "Estoquista"
});

const estadoUsuarios = {
    usuarioAtual: null,
    usuarios: [],
    usuarioEditado: null,
    usuarioSenha: null,
    operacaoPendente: null,
    ocupado: false,
    dadosDesatualizados: true
};

const elementosUsuarios = {};


// ========================================
// INICIALIZACAO
// ========================================

document.addEventListener("DOMContentLoaded", iniciarUsuarios);

async function iniciarUsuarios() {

    identificarElementos();
    configurarEventos();
    atualizarControles();

    try {

        estadoUsuarios.usuarioAtual =
            await requisicaoApi("/api/auth/me");

        if (estadoUsuarios.usuarioAtual.perfil !== "ADMINISTRADOR") {

            window.location.replace("/dashboard.html");
            return;
        }

        elementosUsuarios["usuario-atual"].textContent =
            estadoUsuarios.usuarioAtual.nome + " | Administrador";

        await carregarUsuarios();

    } catch (erro) {

        mostrarMensagem(
            elementosUsuarios["mensagem-usuarios"],
            erro.message || "Nao foi possivel iniciar o modulo.",
            "erro"
        );
    }
}


// ========================================
// REFERENCIAS HTML
// ========================================

function identificarElementos() {

    const ids = [
        "usuario-atual",
        "botao-sair",
        "atualizar-usuarios",
        "mensagem-usuarios",
        "total-usuarios",
        "usuarios-ativos",
        "usuarios-inativos",
        "usuarios-bloqueados",
        "administradores-disponiveis",
        "contagem-usuarios",
        "novo-usuario",
        "pesquisa-usuario",
        "filtro-perfil",
        "filtro-situacao",
        "limpar-filtros",
        "lista-usuarios",
        "ultima-atualizacao",

        "modal-usuario",
        "titulo-modal-usuario",
        "fechar-modal-usuario",
        "form-usuario",
        "erro-usuario",
        "nome-usuario",
        "login-usuario",
        "perfil-usuario",
        "campos-senha-cadastro",
        "senha-usuario",
        "confirmacao-senha-usuario",
        "aviso-propria-conta",
        "cancelar-usuario",
        "salvar-usuario",

        "modal-senha",
        "fechar-modal-senha",
        "form-senha",
        "erro-senha",
        "identificacao-senha",
        "nova-senha",
        "confirmacao-nova-senha",
        "cancelar-senha",
        "salvar-senha",

        "modal-confirmacao",
        "titulo-confirmacao",
        "fechar-confirmacao",
        "erro-confirmacao",
        "texto-confirmacao",
        "cancelar-confirmacao",
        "confirmar-operacao"
    ];

    ids.forEach(id => {

        const elemento = document.getElementById(id);

        if (!elemento) {

            throw new Error(
                "Elemento HTML nao encontrado: " + id
            );
        }

        elementosUsuarios[id] = elemento;
    });
}


// ========================================
// EVENTOS
// ========================================

function configurarEventos() {

    elementosUsuarios["botao-sair"]
        .addEventListener("click", async () => {

            if (estadoUsuarios.ocupado) {
                return;
            }

            try {

                await sairDoSistema();

            } catch (erro) {

                mostrarMensagem(
                    elementosUsuarios["mensagem-usuarios"],
                    erro.message || "Nao foi possivel sair.",
                    "erro"
                );
            }
        });

    elementosUsuarios["atualizar-usuarios"]
        .addEventListener("click", carregarUsuarios);

    elementosUsuarios["novo-usuario"]
        .addEventListener("click", abrirCadastro);

    elementosUsuarios["pesquisa-usuario"]
        .addEventListener("input", renderizarUsuarios);

    elementosUsuarios["filtro-perfil"]
        .addEventListener("change", renderizarUsuarios);

    elementosUsuarios["filtro-situacao"]
        .addEventListener("change", renderizarUsuarios);

    elementosUsuarios["limpar-filtros"]
        .addEventListener("click", limparFiltros);

    elementosUsuarios["form-usuario"]
        .addEventListener("submit", salvarUsuario);

    elementosUsuarios["form-senha"]
        .addEventListener("submit", salvarSenha);

    elementosUsuarios["confirmar-operacao"]
        .addEventListener("click", executarOperacao);

    configurarFechamentoModal(
        "modal-usuario",
        ["fechar-modal-usuario", "cancelar-usuario"]
    );

    configurarFechamentoModal(
        "modal-senha",
        ["fechar-modal-senha", "cancelar-senha"]
    );

    configurarFechamentoModal(
        "modal-confirmacao",
        ["fechar-confirmacao", "cancelar-confirmacao"]
    );

    elementosUsuarios["modal-usuario"]
        .addEventListener("close", () => {

            elementosUsuarios["senha-usuario"].value = "";
            elementosUsuarios["confirmacao-senha-usuario"].value = "";

            estadoUsuarios.usuarioEditado = null;
        });

    elementosUsuarios["modal-senha"]
        .addEventListener("close", () => {

            elementosUsuarios["nova-senha"].value = "";
            elementosUsuarios["confirmacao-nova-senha"].value = "";

            estadoUsuarios.usuarioSenha = null;
        });

    elementosUsuarios["modal-confirmacao"]
        .addEventListener("close", () => {

            estadoUsuarios.operacaoPendente = null;
        });
}

function configurarFechamentoModal(idModal, idsBotoes) {

    const modal = elementosUsuarios[idModal];

    idsBotoes.forEach(id => {

        elementosUsuarios[id].addEventListener("click", () => {

            if (!estadoUsuarios.ocupado) {
                modal.close();
            }
        });
    });

    modal.addEventListener("cancel", evento => {

        if (estadoUsuarios.ocupado) {
            evento.preventDefault();
        }
    });
}


// ========================================
// CONTROLE DE REQUISICOES
// ========================================

function definirOcupado(ocupado) {

    estadoUsuarios.ocupado = ocupado;

    atualizarControles();
}

function atualizarControles() {

    const bloqueado =
        estadoUsuarios.ocupado
        || estadoUsuarios.dadosDesatualizados
        || !estadoUsuarios.usuarioAtual;

    elementosUsuarios["novo-usuario"].disabled = bloqueado;

    elementosUsuarios["atualizar-usuarios"].disabled =
        estadoUsuarios.ocupado;

    elementosUsuarios["botao-sair"].disabled =
        estadoUsuarios.ocupado;

    [
        "salvar-usuario",
        "salvar-senha",
        "confirmar-operacao"
    ].forEach(id => {

        elementosUsuarios[id].disabled = bloqueado;
    });

    [
        "fechar-modal-usuario",
        "cancelar-usuario",
        "fechar-modal-senha",
        "cancelar-senha",
        "fechar-confirmacao",
        "cancelar-confirmacao"
    ].forEach(id => {

        elementosUsuarios[id].disabled =
            estadoUsuarios.ocupado;
    });

    document.querySelectorAll(
        ".botao-acao-usuario"
    ).forEach(botao => {

        botao.disabled =
            bloqueado
            || botao.dataset.restrito === "true";
    });
}

function podeModificarUsuarios() {

    return !estadoUsuarios.ocupado
        && !estadoUsuarios.dadosDesatualizados
        && estadoUsuarios.usuarioAtual !== null;
}


// ========================================
// CARREGAMENTO DOS DADOS
// ========================================

async function carregarUsuarios() {

    if (estadoUsuarios.ocupado) {
        return false;
    }

    definirOcupado(true);

    elementosUsuarios["mensagem-usuarios"].hidden = true;

    try {

        await buscarDadosUsuarios();

        return true;

    } catch (erro) {

        estadoUsuarios.dadosDesatualizados = true;

        mostrarMensagem(
            elementosUsuarios["mensagem-usuarios"],
            erro.message || "Nao foi possivel carregar os usuarios.",
            "erro"
        );

        return false;

    } finally {

        definirOcupado(false);
    }
}

async function buscarDadosUsuarios() {

    const [usuarios, resumo] = await Promise.all([

        requisicaoApi(API_USUARIOS),

        requisicaoApi(API_USUARIOS + "/resumo")
    ]);

    if (!Array.isArray(usuarios)) {

        throw new Error("Resposta invalida da listagem de usuarios.");
    }

    estadoUsuarios.usuarios = usuarios;
    estadoUsuarios.dadosDesatualizados = false;

    atualizarIndicadores(resumo);
    renderizarUsuarios();

    elementosUsuarios["ultima-atualizacao"].textContent =
        "Ultima atualizacao: "
        + new Date().toLocaleString("pt-BR");
}

function atualizarIndicadores(resumo) {

    elementosUsuarios["total-usuarios"].textContent =
        resumo.totalUsuarios;

    elementosUsuarios["usuarios-ativos"].textContent =
        resumo.usuariosAtivos;

    elementosUsuarios["usuarios-inativos"].textContent =
        resumo.usuariosInativos;

    elementosUsuarios["usuarios-bloqueados"].textContent =
        resumo.usuariosBloqueados;

    elementosUsuarios["administradores-disponiveis"].textContent =
        resumo.administradoresDisponiveis;
}


// ========================================
// PESQUISA E FILTROS
// ========================================

function normalizarPesquisa(valor) {

    return String(valor ?? "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .trim();
}

function filtrarUsuarios(usuarios, pesquisa, perfil, situacao) {

    const termo = normalizarPesquisa(pesquisa);

    return usuarios.filter(usuario => {

        const nome = normalizarPesquisa(usuario.nome);
        const login = normalizarPesquisa(usuario.login);

        if (termo
                && !nome.includes(termo)
                && !login.includes(termo)) {

            return false;
        }

        if (perfil && usuario.perfil !== perfil) {

            return false;
        }

        if (situacao === "ATIVO"
                && (!usuario.ativo || usuario.bloqueado)) {

            return false;
        }

        if (situacao === "INATIVO" && usuario.ativo) {

            return false;
        }

        if (situacao === "BLOQUEADO" && !usuario.bloqueado) {

            return false;
        }

        return true;
    });
}

function limparFiltros() {

    elementosUsuarios["pesquisa-usuario"].value = "";
    elementosUsuarios["filtro-perfil"].value = "";
    elementosUsuarios["filtro-situacao"].value = "";

    renderizarUsuarios();
}

function propriaConta(usuario) {

    return estadoUsuarios.usuarioAtual !== null
        && String(usuario.id)
            === String(estadoUsuarios.usuarioAtual.id);
}


// ========================================
// RENDERIZACAO DA TABELA
// ========================================

function renderizarUsuarios() {

    const filtrados = filtrarUsuarios(

        estadoUsuarios.usuarios,

        elementosUsuarios["pesquisa-usuario"].value,

        elementosUsuarios["filtro-perfil"].value,

        elementosUsuarios["filtro-situacao"].value
    );

    const tbody = elementosUsuarios["lista-usuarios"];

    tbody.replaceChildren();

    elementosUsuarios["contagem-usuarios"].textContent =
        filtrados.length
        + " de "
        + estadoUsuarios.usuarios.length
        + " funcionarios";

    if (filtrados.length === 0) {

        const linha = document.createElement("tr");
        const celula = document.createElement("td");

        celula.colSpan = 6;
        celula.className = "lista-usuarios-vazia";

        celula.textContent =
            "Nenhum funcionario encontrado.";

        linha.appendChild(celula);
        tbody.appendChild(linha);

        return;
    }

    filtrados.forEach(usuario => {

        tbody.appendChild(criarLinhaUsuario(usuario));
    });

    atualizarControles();
}

function criarLinhaUsuario(usuario) {

    const linha = document.createElement("tr");

    const celulaNome = document.createElement("td");
    const nome = document.createElement("span");

    nome.className = "nome-funcionario";
    nome.textContent = usuario.nome;

    celulaNome.appendChild(nome);

    if (propriaConta(usuario)) {

        const identificacao = document.createElement("small");

        identificacao.className = "identificacao-propria-conta";
        identificacao.textContent = "Sua conta";

        celulaNome.appendChild(identificacao);
    }

    linha.appendChild(celulaNome);

    linha.appendChild(criarCelula(usuario.login));

    linha.appendChild(
        criarCelula(
            PERFIS_USUARIOS[usuario.perfil] || usuario.perfil
        )
    );

    const celulaSituacao = document.createElement("td");

    const situacoes = document.createElement("div");

    situacoes.className = "situacoes-usuario";

    situacoes.appendChild(
        criarStatusUsuario(
            usuario.ativo ? "Ativo" : "Inativo",
            usuario.ativo ? "ativo" : "inativo"
        )
    );

    if (usuario.bloqueado) {

        situacoes.appendChild(
            criarStatusUsuario("Bloqueado", "bloqueado")
        );
    }

    celulaSituacao.appendChild(situacoes);
    linha.appendChild(celulaSituacao);

    const celulaTentativas = document.createElement("td");

    const tentativas = document.createElement("span");

    tentativas.className = "tentativas-usuario";

    if (usuario.tentativasFalhas >= 3) {

        tentativas.classList.add("alerta");
    }

    tentativas.textContent =
        usuario.tentativasFalhas + " / 5";

    celulaTentativas.appendChild(tentativas);
    linha.appendChild(celulaTentativas);

    const celulaAcoes = document.createElement("td");

    const acoes = document.createElement("div");

    acoes.className = "acoes-usuario";

    acoes.appendChild(
        criarBotaoAcao(
            "Editar",
            () => abrirEdicao(usuario)
        )
    );

    acoes.appendChild(
        criarBotaoAcao(
            "Redefinir senha",
            () => abrirRedefinicaoSenha(usuario)
        )
    );

    if (usuario.ativo) {

        acoes.appendChild(
            criarBotaoAcao(
                "Desativar",
                () => abrirConfirmacao(usuario, "desativar"),
                "perigo",
                propriaConta(usuario)
            )
        );

    } else {

        acoes.appendChild(
            criarBotaoAcao(
                "Ativar",
                () => abrirConfirmacao(usuario, "ativar"),
                "sucesso"
            )
        );
    }

    if (usuario.bloqueado || usuario.tentativasFalhas > 0) {

        acoes.appendChild(
            criarBotaoAcao(
                usuario.bloqueado
                    ? "Desbloquear"
                    : "Zerar tentativas",

                () => abrirConfirmacao(usuario, "desbloquear"),

                "sucesso"
            )
        );
    }

    celulaAcoes.appendChild(acoes);
    linha.appendChild(celulaAcoes);

    return linha;
}

function criarCelula(texto) {

    const celula = document.createElement("td");

    celula.textContent = texto ?? "";

    return celula;
}

function criarStatusUsuario(texto, classe) {

    const elemento = document.createElement("span");

    elemento.className = "status-usuario " + classe;
    elemento.textContent = texto;

    return elemento;
}

function criarBotaoAcao(
        texto,
        acao,
        classe = "",
        restrito = false) {

    const botao = document.createElement("button");

    botao.type = "button";

    botao.className =
        "botao-acao-usuario"
        + (classe ? " " + classe : "");

    botao.textContent = texto;

    botao.dataset.restrito = String(restrito);

    botao.disabled =
        restrito || !podeModificarUsuarios();

    if (restrito) {

        botao.title =
            "Nao e permitido desativar a propria conta.";
    }

    botao.addEventListener("click", () => {

        if (podeModificarUsuarios() && !restrito) {

            acao();
        }
    });

    return botao;
}


// ========================================
// CADASTRO
// ========================================

function abrirCadastro() {

    if (!podeModificarUsuarios()) {
        return;
    }

    estadoUsuarios.usuarioEditado = null;

    elementosUsuarios["form-usuario"].reset();

    elementosUsuarios["titulo-modal-usuario"].textContent =
        "Novo usuario";

    elementosUsuarios["erro-usuario"].hidden = true;

    elementosUsuarios["campos-senha-cadastro"].hidden = false;

    elementosUsuarios["senha-usuario"].required = true;

    elementosUsuarios["confirmacao-senha-usuario"].required = true;

    elementosUsuarios["login-usuario"].readOnly = false;

    elementosUsuarios["perfil-usuario"].disabled = false;

    elementosUsuarios["aviso-propria-conta"].hidden = true;

    elementosUsuarios["modal-usuario"].showModal();

    elementosUsuarios["nome-usuario"].focus();
}


// ========================================
// EDICAO
// ========================================

function abrirEdicao(usuario) {

    if (!podeModificarUsuarios()) {
        return;
    }

    estadoUsuarios.usuarioEditado = usuario;

    elementosUsuarios["form-usuario"].reset();

    elementosUsuarios["titulo-modal-usuario"].textContent =
        "Editar usuario";

    elementosUsuarios["erro-usuario"].hidden = true;

    elementosUsuarios["nome-usuario"].value = usuario.nome;

    elementosUsuarios["login-usuario"].value = usuario.login;

    elementosUsuarios["perfil-usuario"].value = usuario.perfil;

    elementosUsuarios["campos-senha-cadastro"].hidden = true;

    elementosUsuarios["senha-usuario"].required = false;

    elementosUsuarios["confirmacao-senha-usuario"].required = false;

    const propria = propriaConta(usuario);

    elementosUsuarios["login-usuario"].readOnly = propria;

    elementosUsuarios["perfil-usuario"].disabled = propria;

    elementosUsuarios["aviso-propria-conta"].hidden = !propria;

    elementosUsuarios["modal-usuario"].showModal();

    elementosUsuarios["nome-usuario"].focus();
}


// ========================================
// VALIDACAO
// ========================================

function validarSenhaUsuario(senha, confirmacao) {

    if (!senha.trim() || senha.length < 8) {

        return "A senha deve possuir pelo menos 8 caracteres.";
    }

    const quantidadeBytes =
        new TextEncoder().encode(senha).length;

    if (quantidadeBytes > 72) {

        return "A senha nao pode ultrapassar 72 bytes.";
    }

    if (senha !== confirmacao) {

        return "As senhas informadas nao coincidem.";
    }

    return null;
}

function validarDadosUsuario(dados) {

    if (!dados.nome || dados.nome.length > 100) {

        return "Informe um nome valido de ate 100 caracteres.";
    }

    if (!/^[a-zA-Z0-9._-]{3,50}$/.test(dados.login)) {

        return "Informe um login valido de 3 a 50 caracteres.";
    }

    if (!Object.hasOwn(PERFIS_USUARIOS, dados.perfil)) {

        return "Selecione um perfil valido.";
    }

    return null;
}


// ========================================
// SALVAR CADASTRO OU EDICAO
// ========================================

async function salvarUsuario(evento) {

    evento.preventDefault();

    if (!podeModificarUsuarios()) {
        return;
    }

    const erroElemento = elementosUsuarios["erro-usuario"];

    erroElemento.hidden = true;

    const editado = estadoUsuarios.usuarioEditado;

    const dados = {

        nome: elementosUsuarios["nome-usuario"].value.trim(),

        login: elementosUsuarios["login-usuario"]
            .value.trim().toLowerCase(),

        perfil: elementosUsuarios["perfil-usuario"].value
    };

    const erroDados = validarDadosUsuario(dados);

    if (erroDados) {

        mostrarMensagem(erroElemento, erroDados, "erro");

        return;
    }

    if (!editado) {

        const senha = elementosUsuarios["senha-usuario"].value;

        const confirmacao =
            elementosUsuarios["confirmacao-senha-usuario"].value;

        const erroSenha = validarSenhaUsuario(senha, confirmacao);

        if (erroSenha) {

            mostrarMensagem(erroElemento, erroSenha, "erro");

            return;
        }

        dados.senha = senha;
    }

    await realizarMutacao({

        url: editado
            ? API_USUARIOS + "/" + editado.id
            : API_USUARIOS,

        metodo: editado ? "PUT" : "POST",

        dados: dados,

        modal: elementosUsuarios["modal-usuario"],

        erroElemento: erroElemento,

        mensagemSucesso: editado
            ? "Usuario atualizado com sucesso."
            : "Usuario cadastrado com sucesso."
    });
}


// ========================================
// REDEFINICAO DE SENHA
// ========================================

function abrirRedefinicaoSenha(usuario) {

    if (!podeModificarUsuarios()) {
        return;
    }

    estadoUsuarios.usuarioSenha = usuario;

    elementosUsuarios["form-senha"].reset();

    elementosUsuarios["erro-senha"].hidden = true;

    elementosUsuarios["identificacao-senha"].textContent =
        "Funcionario: "
        + usuario.nome
        + " ("
        + usuario.login
        + ")";

    elementosUsuarios["modal-senha"].showModal();

    elementosUsuarios["nova-senha"].focus();
}

async function salvarSenha(evento) {

    evento.preventDefault();

    if (!podeModificarUsuarios()
            || !estadoUsuarios.usuarioSenha) {

        return;
    }

    const erroElemento = elementosUsuarios["erro-senha"];

    erroElemento.hidden = true;

    const senha = elementosUsuarios["nova-senha"].value;

    const confirmacao =
        elementosUsuarios["confirmacao-nova-senha"].value;

    const erro = validarSenhaUsuario(senha, confirmacao);

    if (erro) {

        mostrarMensagem(erroElemento, erro, "erro");

        return;
    }

    const usuario = estadoUsuarios.usuarioSenha;

    await realizarMutacao({

        url: API_USUARIOS + "/" + usuario.id + "/senha",

        metodo: "PATCH",

        dados: { senha: senha },

        modal: elementosUsuarios["modal-senha"],

        erroElemento: erroElemento,

        mensagemSucesso: "Senha redefinida com sucesso."
    });
}


// ========================================
// ALTERACOES DE SITUACAO
// ========================================

function abrirConfirmacao(usuario, operacao) {

    if (!podeModificarUsuarios()) {
        return;
    }

    if (operacao === "desativar" && propriaConta(usuario)) {
        return;
    }

    const configuracoes = {

        desativar: {

            titulo: "Desativar usuario",

            texto: "Deseja desativar o funcionario "
                + usuario.nome
                + "? Ele nao podera realizar novos logins.",

            botao: "Desativar"
        },

        ativar: {

            titulo: "Reativar usuario",

            texto: "Deseja reativar o funcionario "
                + usuario.nome
                + "? Um eventual bloqueio sera mantido.",

            botao: "Ativar"
        },

        desbloquear: {

            titulo: "Desbloquear ou zerar tentativas",

            texto: "Deseja remover o bloqueio e zerar "
                + "as tentativas incorretas de "
                + usuario.nome
                + "?",

            botao: "Confirmar"
        }
    };

    const configuracao = configuracoes[operacao];

    if (!configuracao) {
        return;
    }

    estadoUsuarios.operacaoPendente = {

        usuario: usuario,
        operacao: operacao
    };

    elementosUsuarios["erro-confirmacao"].hidden = true;

    elementosUsuarios["titulo-confirmacao"].textContent =
        configuracao.titulo;

    elementosUsuarios["texto-confirmacao"].textContent =
        configuracao.texto;

    elementosUsuarios["confirmar-operacao"].textContent =
        configuracao.botao;

    elementosUsuarios["modal-confirmacao"].showModal();
}

async function executarOperacao() {

    if (!podeModificarUsuarios()
            || !estadoUsuarios.operacaoPendente) {

        return;
    }

    const { usuario, operacao } =
        estadoUsuarios.operacaoPendente;

    const mensagens = {

        desativar: "Usuario desativado com sucesso.",

        ativar: "Usuario reativado com sucesso.",

        desbloquear: "Bloqueio e tentativas removidos com sucesso."
    };

    if (!Object.hasOwn(mensagens, operacao)) {
        return;
    }

    await realizarMutacao({

        url: API_USUARIOS
            + "/"
            + usuario.id
            + "/"
            + operacao,

        metodo: "PATCH",

        dados: undefined,

        modal: elementosUsuarios["modal-confirmacao"],

        erroElemento: elementosUsuarios["erro-confirmacao"],

        mensagemSucesso: mensagens[operacao]
    });
}


// ========================================
// EXECUCAO SEGURA DE MUTACOES
// ========================================

async function realizarMutacao(configuracao) {

    if (!podeModificarUsuarios()) {
        return false;
    }

    definirOcupado(true);

    configuracao.erroElemento.hidden = true;

    elementosUsuarios["mensagem-usuarios"].hidden = true;

    try {

        await requisicaoApi(
            configuracao.url,
            configuracao.metodo,
            configuracao.dados
        );

    } catch (erro) {

        estadoUsuarios.dadosDesatualizados = true;

        mostrarMensagem(

            configuracao.erroElemento,

            (erro.message || "Nao foi possivel realizar a operacao.")
            + " Feche esta janela e atualize a listagem antes de tentar novamente.",

            "erro"
        );

        definirOcupado(false);

        return false;
    }

    // A operacao foi confirmada pelo servidor.

    estadoUsuarios.dadosDesatualizados = true;

    configuracao.modal.close();

    try {

        await buscarDadosUsuarios();

        mostrarMensagem(

            elementosUsuarios["mensagem-usuarios"],

            configuracao.mensagemSucesso,

            "sucesso"
        );

    } catch (erro) {

        estadoUsuarios.dadosDesatualizados = true;

        mostrarMensagem(

            elementosUsuarios["mensagem-usuarios"],

            configuracao.mensagemSucesso
            + " Entretanto, nao foi possivel atualizar a listagem."
            + " Utilize o botao Atualizar.",

            "erro"
        );

    } finally {

        definirOcupado(false);
    }

    return true;
}