
const modulos = [
    {
        id: "produtos",
        nome: "Produtos e categorias",
        descricao: "Cadastre, edite e gerencie os produtos do cardápio.",
        icone: "▤",
        pagina: "/produtos.html",
        disponivel: true,
        perfis: ["ADMINISTRADOR", "GERENTE"]
    },
    {
        id: "mesas",
        nome: "Mesas",
        descricao: "Controle a ocupação e o atendimento das mesas.",
        icone: "▦",
        pagina: "/mesas.html",
        disponivel: true,
        perfis: ["ADMINISTRADOR", "GERENTE", "ATENDENTE"]
    },
    {
        id: "cozinha",
        nome: "Cozinha",
        descricao: "Acompanhe os pedidos e atualize as etapas de preparo.",
        icone: "♨",
        pagina: "/cozinha.html",
        disponivel: true,
        perfis: ["ADMINISTRADOR", "GERENTE", "COZINHEIRO"]
    },
    {
        id: "estoque",
        nome: "Estoque",
        descricao: "Gerencie os ingredientes e as movimentações de estoque.",
        icone: "▣",
        pagina: "/estoque.html",
        disponivel: true,
        perfis: ["ADMINISTRADOR", "GERENTE", "ESTOQUISTA"]
    },
    {
        id: "pagamentos",
        nome: "Pagamentos",
        descricao: "Consulte contas, registre pagamentos, aplique descontos e acompanhe o histórico financeiro.",
        icone: "$",
        pagina: "/pagamentos.html",
        disponivel: true,
        perfis: ["ADMINISTRADOR", "GERENTE", "ATENDENTE"]
    },
    {
        id: "usuarios",
        nome: "Usuários",
        descricao: "Gerencie funcionários, perfis e permissões de acesso.",
        icone: "♙",
        pagina: "/usuarios.html",
        disponivel: true,
        perfis: ["ADMINISTRADOR"]
    },
    {
    id: "pedidos",

    nome: "Gestão de pedidos",

    descricao: "Acompanhe os pedidos do restaurante, consulte seus detalhes e registre a entrega dos alimentos.",

    icone: "📋",

    pagina: "/pedidos.html",

    disponivel: true,

    perfis: [
        "ADMINISTRADOR",
        "GERENTE",
        "ATENDENTE"
    ]
    },
];

const nomesPerfis = {
    ADMINISTRADOR: "Administrador",
    GERENTE: "Gerente",
    ATENDENTE: "Atendente",
    COZINHEIRO: "Cozinheiro",
    ESTOQUISTA: "Estoquista"
};

const descricoesPerfis = {
    ADMINISTRADOR:
        "Gerencie as operações e os acessos do restaurante em um só lugar.",

    GERENTE:
        "Acompanhe os produtos, pedidos, estoque e atendimento do restaurante.",

    ATENDENTE:
        "Organize as mesas, acompanhe os atendimentos e registre pagamentos.",

    COZINHEIRO:
        "Acompanhe os pedidos recebidos e organize o preparo dos alimentos.",

    ESTOQUISTA:
        "Controle os ingredientes e as movimentações de estoque do restaurante."
};

const menuNavegacao =
    document.getElementById("menu-navegacao");

const gradeModulos =
    document.getElementById("grade-modulos");

const mensagemDashboard =
    document.getElementById("mensagem-dashboard");

const menuLateral =
    document.getElementById("menu-lateral");

const fundoMenu =
    document.getElementById("fundo-menu");

const alternarMenu =
    document.getElementById("alternar-menu");

const botaoSair =
    document.getElementById("botao-sair");


/* IDENTIFICACAO DO FUNCIONARIO */

async function carregarDashboard() {

    try {

        const usuario = await requisicaoApi(
            "/api/auth/me"
        );

        if (!nomesPerfis[usuario.perfil]) {
            throw new Error("Perfil de acesso inválido.");
        }

        preencherUsuario(usuario);

        const modulosPermitidos = modulos.filter(
            modulo => modulo.perfis.includes(usuario.perfil)
        );

        renderizarModulos(modulosPermitidos);

        document.getElementById(
            "info-modulos"
        ).textContent =
            modulosPermitidos.filter(
                modulo => modulo.disponivel
            ).length + " de " + modulosPermitidos.length;

    } catch (erro) {

        if (erro.status === 401 ||
            erro.status === 403) {

            window.location.href =
                "/index.html?expirou";

            return;
        }

        mostrarMensagem(
            mensagemDashboard,
            erro.message ||
                "Não foi possível carregar o dashboard.",
            "erro"
        );
    }
}


/* DADOS DO USUARIO */

function preencherUsuario(usuario) {

    const nomePerfil = nomesPerfis[usuario.perfil];

    document.getElementById(
        "nome-usuario"
    ).textContent = usuario.nome;

    document.getElementById(
        "perfil-usuario"
    ).textContent = nomePerfil;

    document.getElementById(
        "avatar-usuario"
    ).textContent =
        usuario.nome.charAt(0).toUpperCase();

    document.getElementById(
        "titulo-boas-vindas"
    ).textContent =
        "Bem-vindo, " + usuario.nome + "!";

    document.getElementById(
        "descricao-perfil"
    ).textContent =
        descricoesPerfis[usuario.perfil];

    document.getElementById(
        "info-login"
    ).textContent = usuario.login;

    document.getElementById(
        "info-perfil"
    ).textContent = nomePerfil;
}


/* MENU E CARTOES */

function renderizarModulos(modulosPermitidos) {

    gradeModulos.replaceChildren();

    menuNavegacao.querySelectorAll(
        "[data-modulo]"
    ).forEach(elemento => elemento.remove());

    modulosPermitidos.forEach(modulo => {

        criarItemMenu(modulo);
        criarCartaoModulo(modulo);

    });
}

function criarItemMenu(modulo) {

    const item = document.createElement(
        modulo.disponivel ? "a" : "div"
    );

    item.className = "item-menu";
    item.dataset.modulo = modulo.id;

    if (modulo.disponivel) {

        item.href = modulo.pagina;

    } else {

        item.style.opacity = "0.6";
        item.title = "Módulo em desenvolvimento";

    }

    const icone = document.createElement("span");

    icone.className = "icone-menu";
    icone.textContent = modulo.icone;
    icone.setAttribute("aria-hidden", "true");

    const nome = document.createElement("span");

    nome.textContent = modulo.nome;

    item.append(icone, nome);

    menuNavegacao.appendChild(item);
}

function criarCartaoModulo(modulo) {

    const cartao = document.createElement(
        modulo.disponivel ? "a" : "article"
    );

    cartao.className = "cartao-modulo";

    if (modulo.disponivel) {

        cartao.href = modulo.pagina;

    } else {

        cartao.classList.add("pendente");

    }

    const icone = document.createElement("div");

    icone.className = "icone-modulo";
    icone.textContent = modulo.icone;
    icone.setAttribute("aria-hidden", "true");

    const titulo = document.createElement("h3");

    titulo.textContent = modulo.nome;

    const descricao = document.createElement("p");

    descricao.textContent = modulo.descricao;

    const acao = document.createElement("span");

    acao.className = "acao-modulo";

    acao.textContent = modulo.disponivel
        ? "Acessar módulo →"
        : "Em desenvolvimento";

    cartao.append(
        icone,
        titulo,
        descricao,
        acao
    );

    gradeModulos.appendChild(cartao);
}


/* MENU RESPONSIVO */

function definirMenuAberto(aberto) {

    menuLateral.classList.toggle(
        "aberto",
        aberto
    );

    fundoMenu.hidden = !aberto;

    alternarMenu.setAttribute(
        "aria-expanded",
        String(aberto)
    );

    alternarMenu.setAttribute(
        "aria-label",
        aberto ? "Fechar menu" : "Abrir menu"
    );
}

alternarMenu.addEventListener("click", () => {

    definirMenuAberto(
        !menuLateral.classList.contains("aberto")
    );

});

fundoMenu.addEventListener("click", () => {

    definirMenuAberto(false);

});

document.addEventListener("keydown", evento => {

    if (evento.key === "Escape") {
        definirMenuAberto(false);
    }

});

window.addEventListener("resize", () => {

    if (window.innerWidth > 900) {
        definirMenuAberto(false);
    }

});


/* ENCERRAMENTO DA SESSAO */

botaoSair.addEventListener("click", async () => {

    botaoSair.disabled = true;

    try {

        await sairDoSistema();

    } catch (erro) {

        mostrarMensagem(
            mensagemDashboard,
            "Não foi possível encerrar a sessão.",
            "erro"
        );

        botaoSair.disabled = false;
    }
});


/* INICIALIZACAO */

carregarDashboard();
