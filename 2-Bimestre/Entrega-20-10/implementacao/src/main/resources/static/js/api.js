
/* =========================================
   TOKEN CSRF
   ========================================= */

function tratarSessaoInvalida(resposta) {

    if (resposta.status !== 401) {

        return;
    }

    const paginaAtual = window.location.pathname;

    if (
        paginaAtual !== "/" &&
        paginaAtual !== "/index.html"
    ) {

        window.location.href = "/index.html?expirou";
    }
}


async function obterCsrf() {

    const resposta = await fetch(

        "/api/auth/csrf",

        {

            method: "GET",

            credentials: "same-origin",

            cache: "no-store"
        }
    );

    tratarSessaoInvalida(resposta);

    if (!resposta.ok) {

        const erro = new Error(

            "Não foi possível iniciar a comunicação " +
            "com o sistema."
        );

        erro.status = resposta.status;

        throw erro;
    }

    return await resposta.json();
}


/* =========================================
   REQUISICOES HTTP
   ========================================= */

async function requisicaoApi(

    url,

    metodo = "GET",

    dados = undefined

) {

    metodo = metodo.toUpperCase();

    const headers = {

        "Accept": "application/json"
    };

    const opcoes = {

        method: metodo,

        headers: headers,

        credentials: "same-origin",

        cache: "no-store"
    };


    /* Adicionar CSRF nas operacoes de escrita. */

    if (!["GET", "HEAD", "OPTIONS"].includes(metodo)) {

        const csrf = await obterCsrf();

        headers[csrf.headerName] = csrf.token;
    }


    /* Converter os dados para JSON. */

    if (dados !== undefined) {

        headers["Content-Type"] = "application/json";

        opcoes.body = JSON.stringify(dados);
    }


    /* Executar a requisicao. */

    const resposta = await fetch(url, opcoes);

    tratarSessaoInvalida(resposta);


    /* Verificar redirecionamento para o login. */

    if (

        resposta.redirected &&

        new URL(resposta.url).pathname === "/index.html"

    ) {

        window.location.href = "/index.html?expirou";

        const erro = new Error("Sessão expirada.");

        erro.status = 401;

        throw erro;
    }


    /* Tratar respostas de erro. */

    if (!resposta.ok) {

        const mensagensPadrao = {

            400: "Os dados enviados são inválidos.",

            401: "Sessão inválida ou expirada.",

            403: "Acesso não autorizado.",

            404: "Registro não encontrado.",

            409: "Operação incompatível com o estado atual.",

            500: "Ocorreu um erro interno no servidor."
        };

        let mensagem =

            mensagensPadrao[resposta.status] ||

            "Não foi possível realizar a operação.";


        /* Preservar mensagens especificas do backend,
           evitando expor detalhes de erros internos. */

        if (resposta.status < 500) {

            try {

                const dadosErro = await resposta.json();

                const mensagemServidor =

                    dadosErro.erro ||

                    dadosErro.message ||

                    dadosErro.detail;

                if (

                    typeof mensagemServidor === "string" &&

                    mensagemServidor.trim() !== ""

                ) {

                    mensagem = mensagemServidor;
                }

            } catch {

                // Utilizar a mensagem padrao.
            }
        }


        const erro = new Error(mensagem);

        erro.status = resposta.status;

        throw erro;
    }


    /* Resposta sem conteudo. */

    if (resposta.status === 204) {
        return null;
    }


    /* Identificar o formato da resposta. */

    const tipo =

        resposta.headers.get("content-type") || "";

    if (tipo.includes("application/json")) {

        return await resposta.json();
    }

    return await resposta.text();
}


/* =========================================
   ENCERRAMENTO DA SESSAO
   ========================================= */

async function sairDoSistema() {

    const csrf = await obterCsrf();

    const formulario = document.createElement("form");

    formulario.method = "POST";

    formulario.action = "/logout";

    const token = document.createElement("input");

    token.type = "hidden";

    token.name = csrf.parameterName;

    token.value = csrf.token;

    formulario.appendChild(token);

    document.body.appendChild(formulario);

    formulario.submit();
}


/* =========================================
   MENSAGENS DA INTERFACE
   ========================================= */

function mostrarMensagem(

    elemento,

    mensagem,

    tipo = "erro"

) {

    elemento.textContent = mensagem;

    elemento.className = "mensagem " + tipo;

    elemento.hidden = false;
}
