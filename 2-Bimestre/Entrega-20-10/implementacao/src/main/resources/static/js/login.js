
const formCliente =
    document.getElementById("form-cliente");

const formFuncionario =
    document.getElementById("form-funcionario");

const mensagemCliente =
    document.getElementById("mensagem-cliente");

const mensagemFuncionario =
    document.getElementById("mensagem-funcionario");

const mensagemGeral =
    document.getElementById("mensagem-geral");

const botaoCliente =
    document.getElementById("botao-cliente");

const botaoFuncionario =
    document.getElementById("botao-funcionario");

/* Acesso do cliente */

formCliente.addEventListener("submit", async (evento) => {

    evento.preventDefault();

    mensagemCliente.hidden = true;

    const numeroMesa = Number(
        document.getElementById("numero-mesa").value
    );

    if (!Number.isInteger(numeroMesa) || numeroMesa < 1) {

        mostrarMensagem(
            mensagemCliente,
            "Informe um número de mesa válido."
        );

        return;
    }

    botaoCliente.disabled = true;

    botaoCliente.textContent = "Acessando...";

    try {

        await requisicaoApi(
            "/api/cliente/login",
            "POST",
            { numeroMesa: numeroMesa }
        );
        sessionStorage.removeItem("restaurante.carrinho");
        window.location.href = "/cardapio.html";

    } catch (erro) {

        let mensagem = erro.message;

        if (erro.status === 404) {
            mensagem = "A mesa informada não existe.";
        }

        if (erro.status === 409) {
            mensagem =
                "Esta mesa está indisponível para atendimento.";
        }

        mostrarMensagem(
            mensagemCliente,
            mensagem
        );

    } finally {

        botaoCliente.disabled = false;

        botaoCliente.textContent = "Acessar cardápio";
    }
});

/* Acesso do funcionário */

formFuncionario.addEventListener(
    "submit",
    async (evento) => {

        evento.preventDefault();

        mensagemFuncionario.hidden = true;

        botaoFuncionario.disabled = true;

        botaoFuncionario.textContent = "Entrando...";

        try {

            const csrf = await obterCsrf();

            const campoCsrf =
                document.getElementById("csrf-funcionario");

            campoCsrf.name = csrf.parameterName;

            campoCsrf.value = csrf.token;

            HTMLFormElement.prototype.submit.call(
                formFuncionario
            );

        } catch (erro) {

            mostrarMensagem(
                mensagemFuncionario,
                erro.message
            );

            botaoFuncionario.disabled = false;

            botaoFuncionario.textContent =
                "Entrar no sistema";
        }
    }
);

/* Exibir ou ocultar a senha */

const campoSenha =
    document.getElementById("password");

const alternarSenha =
    document.getElementById("alternar-senha");

alternarSenha.addEventListener("click", () => {

    const senhaVisivel =
        campoSenha.type === "text";

    campoSenha.type =
        senhaVisivel ? "password" : "text";

    alternarSenha.textContent =
        senhaVisivel ? "Mostrar" : "Ocultar";

    alternarSenha.setAttribute(
        "aria-pressed",
        String(!senhaVisivel)
    );
});

/* Mensagens retornadas pelo sistema */

const parametros =
    new URLSearchParams(window.location.search);

if (parametros.has("erro")) {

    mostrarMensagem(
        mensagemFuncionario,
        "Credenciais inválidas ou conta indisponível."
    );
}

if (parametros.has("saiu")) {

    mostrarMensagem(
        mensagemGeral,
        "Você saiu do sistema com sucesso.",
        "sucesso"
    );
}

if (parametros.has("expirou")) {

    mostrarMensagem(
        mensagemGeral,
        "Sua sessão expirou. Acesse o sistema novamente.",
        "info"
    );
}
