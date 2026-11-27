"use strict";

/* =========================================
   CONFIGURACOES E ESTADO DO MODULO
   ========================================= */

const API_FINANCEIRA = "/api/pagamentos";

const FORMAS_PAGAMENTO = {
    PIX: "PIX",
    DINHEIRO: "Dinheiro",
    CARTAO_CREDITO: "Cartão de crédito",
    CARTAO_DEBITO: "Cartão de débito",
    VALE_REFEICAO: "Vale-refeição"
};

const STATUS_FINANCEIROS = {
    OCUPADA: "Ocupada",
    AGUARDANDO_PAGAMENTO: "Aguardando pagamento",
    FINALIZADA: "Finalizada",
    ESTORNADA: "Estornada",
    RECEBIDO: "Recebido",
    EM_PREPARO: "Em preparo",
    PRONTO: "Pronto",
    ENTREGUE: "Entregue",
    CANCELADO: "Cancelado"
};

const PERFIS_FINANCEIROS = {
    ADMINISTRADOR: "Administrador",
    GERENTE: "Gerente",
    ATENDENTE: "Atendente"
};

const estado = {
    usuario: null,
    contasAbertas: [],
    historico: [],
    contaAberta: null,
    contaDetalhada: null,
    ocupado: false,
    carregando: false,
    operacaoIncerta: false,
    aba: "abertas"
};

const $ = id => document.getElementById(id);


/* =========================================
   FUNCOES AUXILIARES
   ========================================= */

function criarElemento(tag, classe, texto) {

    const elemento = document.createElement(tag);

    if (classe) {
        elemento.className = classe;
    }

    if (texto !== undefined && texto !== null) {
        elemento.textContent = String(texto);
    }

    return elemento;
}

function definirTexto(id, texto) {

    $(id).textContent = String(texto);
}

function ocultarMensagem(id) {

    const elemento = $(id);

    elemento.hidden = true;
    elemento.textContent = "";
}

function mensagem(id, texto, tipo = "erro") {

    mostrarMensagem($(id), texto, tipo);
}

function mensagemErro(erro) {

    return erro?.message ||
        "Não foi possível concluir a operação.";
}

function formatarMoeda(valor) {

    return Number(valor || 0).toLocaleString(
        "pt-BR",
        {
            style: "currency",
            currency: "BRL"
        }
    );
}

function formatarData(valor) {

    if (!valor) {
        return "—";
    }

    const data = new Date(valor);

    if (Number.isNaN(data.getTime())) {
        return String(valor);
    }

    return data.toLocaleString("pt-BR");
}

function paraCentavos(valor) {

    const texto = String(valor ?? "")
        .trim()
        .replace(",", ".");

    if (!/^\d+(\.\d{1,2})?$/.test(texto)) {
        return null;
    }

    const partes = texto.split(".");

    const inteiro = BigInt(partes[0]);

    const decimal = BigInt(
        (partes[1] || "").padEnd(2, "0")
    );

    const centavos = inteiro * 100n + decimal;

    if (centavos > 99999999999999n) {
        return null;
    }

    return Number(centavos);
}

function moedaCentavos(centavos) {

    return formatarMoeda(centavos / 100);
}

function criarStatus(status) {

    const classe = String(status || "")
        .toLowerCase()
        .replaceAll("_", "-");

    return criarElemento(
        "span",
        "status status-badge status-" + classe,
        STATUS_FINANCEIROS[status] || status || "—"
    );
}

function criarBotao(texto, classe, acao) {

    const botao = criarElemento(
        "button",
        classe,
        texto
    );

    botao.type = "button";

    botao.addEventListener("click", acao);

    return botao;
}

function criarDado(rotulo, valor) {

    const elemento = criarElemento(
        "div",
        "dado-conta"
    );

    elemento.append(
        criarElemento("span", "", rotulo),
        criarElemento("strong", "", valor)
    );

    return elemento;
}

function criarLinhaValor(rotulo, valor, classe = "") {

    const linha = criarElemento(
        "div",
        "linha-valor " + classe
    );

    linha.append(
        criarElemento("span", "", rotulo),
        criarElemento("strong", "", valor)
    );

    return linha;
}

function criarCelula(linha, valor, classe = "") {

    const celula = criarElemento(
        "td",
        classe
    );

    if (valor instanceof Node) {
        celula.append(valor);
    } else {
        celula.textContent = String(valor ?? "—");
    }

    linha.append(celula);

    return celula;
}

function linhaVazia(tabela, colunas, texto) {

    const linha = document.createElement("tr");

    const celula = criarElemento(
        "td",
        "sem-registros",
        texto
    );

    celula.colSpan = colunas;

    linha.append(celula);

    tabela.replaceChildren(linha);
}

function podeEstornar() {

    return [
        "ADMINISTRADOR",
        "GERENTE"
    ].includes(estado.usuario?.perfil);
}

function limiteDesconto() {

    return podeEstornar() ? 100 : 10;
}

function fecharModal(id) {

    if (estado.ocupado) {
        return;
    }

    const modal = $(id);

    if (modal.open) {
        modal.close();
    }
}

function definirOcupado(ocupado) {

    estado.ocupado = ocupado;

    const ids = [
        "atualizar-pagamentos",
        "solicitar-pagamento",
        "confirmar-estorno",
        "abrir-estorno",
        "fechar-conta-aberta",
        "fechar-detalhes-conta",
        "fechar-estorno",
        "cancelar-finalizacao",
        "cancelar-estorno",
        "voltar-detalhes",
        "adicionar-pagamento",
        "botao-sair"
    ];

    ids.forEach(id => {
        $(id).disabled = ocupado;
    });

    document.querySelectorAll(
        "#form-finalizacao input, " +
        "#form-finalizacao select, " +
        "#form-finalizacao textarea, " +
        ".remover-pagamento, " +
        "#form-estorno textarea"
    ).forEach(elemento => {
        elemento.disabled = ocupado;
    });

    atualizarCalculos();
}


/* =========================================
   AUTENTICACAO
   ========================================= */

async function carregarUsuario() {

    estado.usuario = await requisicaoApi(
        "/api/auth/me"
    );

    if (!PERFIS_FINANCEIROS[estado.usuario.perfil]) {

        window.location.href = "/dashboard.html";

        return false;
    }

    definirTexto(
        "usuario-atual",
        estado.usuario.nome + " | " +
        PERFIS_FINANCEIROS[estado.usuario.perfil]
    );

    $("percentual-desconto").max =
        String(limiteDesconto());

    definirTexto(
        "limite-desconto",
        podeEstornar()
            ? "Desconto permitido de 0% a 100%."
            : "Atendentes podem aplicar até 10% de desconto."
    );

    return true;
}


/* =========================================
   INDICADORES FINANCEIROS
   ========================================= */

async function carregarResumo() {

    const resumo = await requisicaoApi(
        API_FINANCEIRA + "/resumo"
    );

    definirTexto(
        "mesas-ocupadas",
        resumo.mesasOcupadas
    );

    definirTexto(
        "mesas-aguardando",
        resumo.mesasAguardandoPagamento
    );

    definirTexto(
        "contas-finalizadas",
        resumo.contasFinalizadas
    );

    definirTexto(
        "contas-estornadas",
        resumo.contasEstornadas
    );

    definirTexto(
        "valor-bruto",
        formatarMoeda(resumo.valorBruto)
    );

    definirTexto(
        "valor-descontos",
        formatarMoeda(resumo.valorDescontos)
    );

    definirTexto(
        "valor-recebido",
        formatarMoeda(resumo.valorRecebido)
    );

    definirTexto(
        "valor-estornado",
        formatarMoeda(resumo.valorEstornado)
    );

    definirTexto(
        "valor-liquido",
        formatarMoeda(resumo.valorLiquido)
    );
}


/* =========================================
   CONTAS EM ABERTO
   ========================================= */

async function carregarContasAbertas() {

    estado.contasAbertas = await requisicaoApi(
        API_FINANCEIRA + "/abertas"
    );

    renderizarContasAbertas();
}

function renderizarContasAbertas() {

    const grade = $("grade-contas-abertas");

    grade.replaceChildren();

    const pesquisa = $("pesquisa-mesa")
        .value.trim();

    const status = $("filtro-status-mesa").value;

    const finalizacao =
        $("filtro-finalizacao").value;

    const contas = estado.contasAbertas.filter(conta => {

        if (
            pesquisa &&
            !String(conta.numeroMesa).includes(pesquisa)
        ) {
            return false;
        }

        if (status && conta.statusMesa !== status) {
            return false;
        }

        if (
            finalizacao === "disponivel" &&
            !conta.podeFinalizar
        ) {
            return false;
        }

        if (
            finalizacao === "bloqueada" &&
            conta.podeFinalizar
        ) {
            return false;
        }

        return true;
    });

    if (contas.length === 0) {

        grade.append(
            criarElemento(
                "p",
                "sem-registros",
                "Nenhuma conta em aberto encontrada."
            )
        );
    }

    contas.forEach(conta => {

        grade.append(criarCartaoConta(conta));
    });

    definirTexto(
        "contagem-abertas",
        contas.length + " de " +
        estado.contasAbertas.length +
        " contas em aberto."
    );
}

function criarCartaoConta(conta) {

    const classeStatus = conta.statusMesa
        .toLowerCase()
        .replaceAll("_", "-");

    const cartao = criarElemento(
        "article",
        "cartao-conta-aberta " + classeStatus
    );

    const cabecalho = criarElemento(
        "div",
        "cabecalho-conta"
    );

    cabecalho.append(
        criarElemento(
            "h3",
            "",
            "Mesa " + conta.numeroMesa
        ),
        criarStatus(conta.statusMesa)
    );

    const informacoes = criarElemento(
        "div",
        "informacoes-conta"
    );

    informacoes.append(
        criarElemento(
            "p",
            "",
            "Total de pedidos: " + conta.totalPedidos
        ),
        criarElemento(
            "p",
            "",
            "Pedidos pendentes: " + conta.pedidosPendentes
        ),
        criarElemento(
            "p",
            "",
            "Pedidos entregues: " + conta.pedidosEntregues
        ),
        criarElemento(
            "p",
            "",
            "Pedidos cancelados: " + conta.pedidosCancelados
        )
    );

    const valor = criarElemento(
        "div",
        "valor-conta-aberta"
    );

    valor.append(
        criarElemento("span", "", "Subtotal da conta"),
        criarElemento(
            "strong",
            "",
            formatarMoeda(conta.subtotal)
        )
    );

    const aviso = criarElemento(
        "div",
        conta.podeFinalizar
            ? "conta-disponivel"
            : "bloqueio-conta",
        conta.podeFinalizar
            ? "Disponível para fechamento."
            : conta.motivoBloqueio ||
                "Fechamento indisponível."
    );

    const acoes = criarElemento(
        "div",
        "acoes-conta"
    );

    acoes.append(
        criarBotao(
            conta.podeFinalizar
                ? "Consultar e finalizar"
                : "Consultar conta",
            "botao-principal",
            () => abrirContaAberta(conta.mesaId)
        )
    );

    cartao.append(
        cabecalho,
        informacoes,
        valor,
        aviso,
        acoes
    );

    return cartao;
}


/* =========================================
   CONSULTA DETALHADA DA CONTA ABERTA
   ========================================= */

async function abrirContaAberta(mesaId) {

    if (estado.ocupado || estado.operacaoIncerta) {
        return;
    }

    ocultarMensagem("mensagem-pagamentos");

    try {

        estado.contaAberta = await requisicaoApi(
            API_FINANCEIRA + "/abertas/" + mesaId
        );

        ocultarMensagem("erro-conta-aberta");

        renderizarContaAberta();

        if (!$("modal-conta-aberta").open) {
            $("modal-conta-aberta").showModal();
        }

    } catch (erro) {

        mensagem(
            "mensagem-pagamentos",
            mensagemErro(erro)
        );
    }
}

function renderizarContaAberta() {

    const conta = estado.contaAberta;

    if (!conta) {
        return;
    }

    definirTexto(
        "titulo-conta-aberta",
        "Conta da mesa " + conta.numeroMesa
    );

    $("resumo-conta-aberta").replaceChildren(

        criarDado("Mesa", conta.numeroMesa),

        criarDado(
            "Situação",
            STATUS_FINANCEIROS[conta.statusMesa]
        ),

        criarDado(
            "Total de pedidos",
            conta.totalPedidos
        ),

        criarDado(
            "Pedidos pendentes",
            conta.pedidosPendentes
        ),

        criarDado(
            "Pedidos entregues",
            conta.pedidosEntregues
        ),

        criarDado(
            "Pedidos cancelados",
            conta.pedidosCancelados
        ),

        criarDado(
            "Valor original dos pedidos",
            formatarMoeda(conta.valorOriginalPedidos)
        ),

        criarDado(
            "Valor cancelado",
            formatarMoeda(conta.valorCancelado)
        ),

        criarDado(
            "Subtotal",
            formatarMoeda(conta.subtotal)
        )
    );

    renderizarPedidosConta(conta.pedidos || []);

    $("aviso-fechamento").hidden =
        conta.podeFinalizar;

    definirTexto(
        "aviso-fechamento",
        conta.motivoBloqueio || ""
    );

    $("solicitar-pagamento").hidden =
        conta.statusMesa !== "OCUPADA";

    $("form-finalizacao").hidden =
        !conta.podeFinalizar;

    if (conta.podeFinalizar) {
        prepararFormularioFinalizacao();
    }
}

function renderizarPedidosConta(pedidos) {

    const lista = $("lista-pedidos-conta");

    lista.replaceChildren();

    if (pedidos.length === 0) {

        lista.append(
            criarElemento(
                "p",
                "sem-registros",
                "Nenhum pedido registrado neste atendimento."
            )
        );

        return;
    }

    pedidos.forEach(pedido => {

        let classe = "pedido-pendente";

        if (pedido.status === "ENTREGUE") {
            classe = "pedido-entregue";
        }

        if (pedido.status === "CANCELADO") {
            classe = "pedido-cancelado";
        }

        const cartao = criarElemento(
            "article",
            "pedido-conta " + classe
        );

        const cabecalho = criarElemento(
            "div",
            "cabecalho-pedido-conta"
        );

        cabecalho.append(
            criarElemento(
                "strong",
                "",
                "Pedido #" + pedido.id
            ),
            criarStatus(pedido.status)
        );

        const itens = criarElemento(
            "div",
            "itens-pedido-conta"
        );

        (pedido.itens || []).forEach(item => {

            const linha = criarElemento(
                "div",
                "item-pedido-conta"
            );

            linha.append(
                criarElemento(
                    "span",
                    "",
                    item.quantidade + " × " +
                    item.nomeProduto + " (" +
                    formatarMoeda(item.precoUnitario) +
                    ")"
                ),
                criarElemento(
                    "strong",
                    "",
                    formatarMoeda(item.subtotal)
                )
            );

            itens.append(linha);
        });

        const total = criarElemento(
            "div",
            "total-pedido-conta"
        );

        total.append(
            criarElemento(
                "span",
                "",
                "Valor considerado na conta"
            ),
            criarElemento(
                "strong",
                "",
                formatarMoeda(pedido.valorConsiderado)
            )
        );

        cartao.append(
            cabecalho,
            criarElemento(
                "small",
                "texto-auxiliar",
                formatarData(pedido.dataHora)
            ),
            itens
        );

        if (pedido.status === "CANCELADO") {

            cartao.append(
                criarElemento(
                    "p",
                    "valor-negativo",
                    "Valor original cancelado: " +
                    formatarMoeda(pedido.valorOriginal)
                )
            );
        }

        cartao.append(total);

        lista.append(cartao);
    });
}


/* =========================================
   SOLICITACAO DE PAGAMENTO
   ========================================= */

async function solicitarPagamento() {

    const conta = estado.contaAberta;

    if (
        !conta ||
        estado.ocupado ||
        estado.operacaoIncerta ||
        conta.statusMesa !== "OCUPADA"
    ) {
        return;
    }

    if (!window.confirm(
        "Solicitar pagamento da mesa " +
        conta.numeroMesa +
        "? Novos pedidos serão bloqueados."
    )) {
        return;
    }

    ocultarMensagem("erro-conta-aberta");

    definirOcupado(true);

    let sucesso = false;

    try {

        await requisicaoApi(
            "/api/mesas/" +
            conta.mesaId +
            "/solicitar-pagamento",
            "PATCH"
        );

        sucesso = true;

    } catch (erro) {

        tratarOperacaoIncerta(
            "erro-conta-aberta",
            erro
        );

    } finally {

        definirOcupado(false);
    }

    if (sucesso) {

        fecharModal("modal-conta-aberta");

        await atualizarDados();

        await abrirContaAberta(conta.mesaId);
    }
}


/* =========================================
   FORMULARIO DE FINALIZACAO
   ========================================= */

function prepararFormularioFinalizacao() {

    $("form-finalizacao").reset();

    $("lista-pagamentos").replaceChildren();

    $("percentual-desconto").value = "0";

    $("motivo-desconto").value = "";

    const subtotal = paraCentavos(
        estado.contaAberta.subtotal
    );

    if (subtotal !== null && subtotal > 0) {

        adicionarLinhaPagamento(
            "PIX",
            (subtotal / 100).toFixed(2)
        );
    }

    atualizarCalculos();
}

function adicionarLinhaPagamento(
    forma = "PIX",
    valor = ""
) {

    const lista = $("lista-pagamentos");

    if (
        estado.ocupado ||
        lista.children.length >= 20
    ) {
        return;
    }

    const fragmento = $("modelo-pagamento")
        .content.cloneNode(true);

    const linha = fragmento.querySelector(
        ".linha-pagamento"
    );

    const seletor = linha.querySelector(
        ".forma-pagamento"
    );

    const campoValor = linha.querySelector(
        ".valor-pagamento"
    );

    seletor.value = forma;

    campoValor.value = valor;

    campoValor.addEventListener(
        "input",
        atualizarCalculos
    );

    seletor.addEventListener(
        "change",
        atualizarCalculos
    );

    linha.querySelector(
        ".remover-pagamento"
    ).addEventListener("click", () => {

        if (estado.ocupado) {
            return;
        }

        linha.remove();

        atualizarCalculos();
    });

    lista.append(fragmento);

    atualizarCalculos();
}

function adicionarPagamentoRestante() {

    const calculo = calcularFechamento();

    const restante = calculo?.restante || 0;

    adicionarLinhaPagamento(
        "PIX",
        restante > 0
            ? (restante / 100).toFixed(2)
            : ""
    );
}


/* =========================================
   CALCULOS MONETARIOS
   ========================================= */

function calcularFechamento() {

    if (!estado.contaAberta) {
        return null;
    }

    const subtotal = paraCentavos(
        estado.contaAberta.subtotal
    );

    const percentual = paraCentavos(
        $("percentual-desconto").value
    );

    const percentualValido =
        percentual !== null &&
        percentual <= limiteDesconto() * 100;

    let desconto = 0;

    if (
        subtotal !== null &&
        percentualValido
    ) {

        desconto = Number(

            (
                BigInt(subtotal) *
                BigInt(percentual) +
                5000n
            ) / 10000n
        );
    }

    const total = (subtotal || 0) - desconto;

    const pagamentos = [];

    let informado = 0;

    let pagamentosValidos = true;

    const linhas = document.querySelectorAll(
        "#lista-pagamentos .linha-pagamento"
    );

    linhas.forEach(linha => {

        const formaPagamento = linha.querySelector(
            ".forma-pagamento"
        ).value;

        const centavos = paraCentavos(

            linha.querySelector(
                ".valor-pagamento"
            ).value
        );

        if (
            !Object.hasOwn(
                FORMAS_PAGAMENTO,
                formaPagamento
            ) ||
            centavos === null ||
            centavos <= 0
        ) {

            pagamentosValidos = false;

            return;
        }

        informado += centavos;

        pagamentos.push({
            formaPagamento: formaPagamento,
            valor: centavos / 100
        });
    });

    const motivo = $("motivo-desconto")
        .value.trim();

    const motivoValido =
        percentual === 0 ||
        (motivo.length > 0 && motivo.length <= 255);

    const restante = total - informado;

    const valido =

        subtotal !== null &&

        percentualValido &&

        motivoValido &&

        pagamentosValidos &&

        linhas.length <= 20 &&

        restante === 0 &&

        estado.contaAberta.podeFinalizar;

    return {
        subtotal,
        percentual,
        percentualValido,
        motivo,
        motivoValido,
        desconto,
        total,
        informado,
        restante,
        pagamentos,
        pagamentosValidos,
        valido
    };
}

function atualizarCalculos() {

    if (!estado.contaAberta) {
        return;
    }

    const calculo = calcularFechamento();

    if (!calculo) {
        return;
    }

    const exigeMotivo =
        calculo.percentual !== null &&
        calculo.percentual > 0;

    $("campo-motivo-desconto").hidden =
        !exigeMotivo;

    $("motivo-desconto").required =
        exigeMotivo;

    $("motivo-desconto").disabled =
        estado.ocupado || !exigeMotivo;

    definirTexto(
        "subtotal-fechamento",
        moedaCentavos(calculo.subtotal || 0)
    );

    definirTexto(
        "desconto-fechamento",
        moedaCentavos(calculo.desconto)
    );

    definirTexto(
        "total-fechamento",
        moedaCentavos(calculo.total)
    );

    definirTexto(
        "total-informado",
        moedaCentavos(calculo.informado)
    );

    definirTexto(
        "rotulo-saldo",
        calculo.restante < 0
            ? "Valor excedente"
            : "Saldo restante"
    );

    definirTexto(
        "saldo-restante",
        moedaCentavos(Math.abs(calculo.restante))
    );

    $("saldo-restante").className =
        calculo.restante === 0
            ? "valor-positivo"
            : "valor-negativo";

    let aviso = "";

    if (estado.operacaoIncerta) {

        aviso =
            "Consulte o histórico antes de realizar outra operação.";

    } else if (calculo.subtotal === null) {

        aviso = "Subtotal inválido.";

    } else if (!calculo.percentualValido) {

        aviso =
            "Informe um desconto entre 0% e " +
            limiteDesconto() + "%.";

    } else if (!calculo.motivoValido) {

        aviso =
            "Informe a justificativa do desconto.";

    } else if (!calculo.pagamentosValidos) {

        aviso =
            "Todos os pagamentos devem possuir valores positivos válidos.";

    } else if (calculo.restante > 0) {

        aviso =
            "Ainda faltam " +
            moedaCentavos(calculo.restante) +
            " para completar o pagamento.";

    } else if (calculo.restante < 0) {

        aviso =
            "Os pagamentos excedem o valor devido.";

    } else if (!estado.contaAberta.podeFinalizar) {

        aviso = "A conta não pode ser finalizada.";

    } else {

        aviso = "Valores conferidos. Conta pronta para finalizar.";
    }

    const validacao = $("validacao-pagamento");

    validacao.textContent = aviso;

    validacao.className =
        "validacao-pagamento " +
        (
            calculo.valido &&
            !estado.operacaoIncerta
                ? "valido"
                : "invalido"
        );

    $("confirmar-finalizacao").disabled =

        !calculo.valido ||

        estado.ocupado ||

        estado.operacaoIncerta;

    $("adicionar-pagamento").disabled =

        estado.ocupado ||

        $("lista-pagamentos").children.length >= 20;
}


/* =========================================
   FINALIZACAO DA CONTA
   ========================================= */

async function finalizarConta(evento) {

    evento.preventDefault();

    if (
        estado.ocupado ||
        estado.operacaoIncerta ||
        !estado.contaAberta
    ) {
        return;
    }

    const calculo = calcularFechamento();

    if (
        !calculo?.valido ||
        !$("form-finalizacao").reportValidity()
    ) {

        atualizarCalculos();

        return;
    }

    const conta = estado.contaAberta;

    if (!window.confirm(

        "Finalizar a conta da mesa " +
        conta.numeroMesa +
        " no valor de " +
        moedaCentavos(calculo.total) +
        "?\n\nConfirme que os valores foram recebidos."

    )) {
        return;
    }

    const dados = {

        codigoAtendimento:
            conta.codigoAtendimento,

        percentualDesconto:
            calculo.percentual / 100,

        motivoDesconto:
            calculo.percentual > 0
                ? calculo.motivo
                : null,

        pagamentos:
            calculo.pagamentos
    };

    ocultarMensagem("erro-conta-aberta");

    definirOcupado(true);

    let resultado = null;

    try {

        resultado = await requisicaoApi(

            API_FINANCEIRA +
            "/abertas/" +
            conta.mesaId +
            "/finalizar",

            "POST",

            dados
        );

    } catch (erro) {

        tratarOperacaoIncerta(
            "erro-conta-aberta",
            erro
        );

    } finally {

        definirOcupado(false);
    }

    if (resultado) {

        fecharModal("modal-conta-aberta");

        estado.contaAberta = null;

        await atualizarDados();

        mensagem(
            "mensagem-pagamentos",
            "Conta #" + resultado.id +
            " finalizada com sucesso. Mesa " +
            resultado.numeroMesa + " liberada.",
            "sucesso"
        );
    }
}


/* =========================================
   HISTORICO FINANCEIRO
   ========================================= */

async function carregarHistorico() {

    estado.historico = await requisicaoApi(
        API_FINANCEIRA + "/contas"
    );

    renderizarHistorico();
}

function renderizarHistorico() {

    const tabela = $("lista-contas");

    tabela.replaceChildren();

    ocultarMensagem("erro-historico");

    const pesquisa = $("pesquisa-historico")
        .value.trim();

    const status = $("filtro-status-conta").value;

    const inicio = $("data-inicial").value;

    const fim = $("data-final").value;

    if (inicio && fim && inicio > fim) {

        mensagem(
            "erro-historico",
            "A data inicial não pode ser posterior à data final."
        );

        linhaVazia(
            tabela,
            10,
            "Corrija o período informado."
        );

        definirTexto("contagem-historico", "");

        return;
    }

    const contas = estado.historico.filter(conta => {

        if (
            pesquisa &&
            !String(conta.id).includes(pesquisa) &&
            !String(conta.numeroMesa).includes(pesquisa)
        ) {
            return false;
        }

        if (status && conta.status !== status) {
            return false;
        }

        const data = String(
            conta.dataFinalizacao || ""
        ).slice(0, 10);

        if (inicio && data < inicio) {
            return false;
        }

        if (fim && data > fim) {
            return false;
        }

        return true;
    });

    if (contas.length === 0) {

        linhaVazia(
            tabela,
            10,
            "Nenhuma conta encontrada."
        );
    }

    contas.forEach(conta => {

        const linha = criarElemento(
            "tr",
            conta.status === "ESTORNADA"
                ? "linha-estornada"
                : ""
        );

        criarCelula(linha, "#" + conta.id);

        criarCelula(linha, conta.numeroMesa);

        criarCelula(
            linha,
            formatarData(conta.dataFinalizacao)
        );

        criarCelula(
            linha,
            formatarMoeda(conta.subtotal),
            "valor-monetario"
        );

        criarCelula(
            linha,
            formatarMoeda(conta.valorDesconto),
            "valor-monetario valor-desconto"
        );

        criarCelula(
            linha,
            formatarMoeda(conta.valorPago),
            "valor-monetario"
        );

        criarCelula(
            linha,
            formatarMoeda(conta.valorLiquido),
            "valor-monetario valor-positivo"
        );

        criarCelula(
            linha,
            criarStatus(conta.status)
        );

        criarCelula(
            linha,
            conta.nomeResponsavel
        );

        criarCelula(
            linha,
            criarBotao(
                "Detalhes",
                "botao-secundario",
                () => abrirDetalhesConta(conta.id)
            )
        );

        tabela.append(linha);
    });

    definirTexto(
        "contagem-historico",
        contas.length + " de " +
        estado.historico.length +
        " contas registradas."
    );
}

function limparFiltrosHistorico() {

    $("form-filtros-historico").reset();

    renderizarHistorico();
}


/* =========================================
   DETALHES DA CONTA FINALIZADA
   ========================================= */

async function abrirDetalhesConta(id) {

    if (estado.ocupado) {
        return;
    }

    try {

        estado.contaDetalhada = await requisicaoApi(
            API_FINANCEIRA + "/contas/" + id
        );

        ocultarMensagem("erro-detalhes-conta");

        renderizarDetalhesConta();

        if (!$("modal-detalhes-conta").open) {

            $("modal-detalhes-conta").showModal();
        }

    } catch (erro) {

        mensagem(
            "mensagem-pagamentos",
            mensagemErro(erro)
        );
    }
}

function renderizarDetalhesConta() {

    const conta = estado.contaDetalhada;

    definirTexto(
        "titulo-detalhes-conta",
        "Detalhes da conta #" + conta.id
    );

    $("dados-conta-finalizada").replaceChildren(

        criarDado("Conta", "#" + conta.id),

        criarDado("Mesa", conta.numeroMesa),

        criarDado(
            "Situação",
            STATUS_FINANCEIROS[conta.status]
        ),

        criarDado(
            "Finalizada em",
            formatarData(conta.dataFinalizacao)
        ),

        criarDado(
            "Responsável",
            conta.nomeResponsavel
        ),

        criarDado(
            "Desconto aplicado",
            conta.percentualDesconto + "%"
        ),

        criarDado(
            "Motivo do desconto",
            conta.motivoDesconto || "Não se aplica"
        )
    );

    $("valores-conta-finalizada").replaceChildren(

        criarLinhaValor(
            "Subtotal",
            formatarMoeda(conta.subtotal)
        ),

        criarLinhaValor(
            "Desconto",
            formatarMoeda(conta.valorDesconto)
        ),

        criarLinhaValor(
            "Total da conta",
            formatarMoeda(conta.valorTotal)
        ),

        criarLinhaValor(
            "Valor pago",
            formatarMoeda(conta.valorPago)
        ),

        criarLinhaValor(
            "Valor estornado",
            formatarMoeda(conta.valorEstornado)
        ),

        criarLinhaValor(
            "Valor líquido",
            formatarMoeda(conta.valorLiquido),
            "linha-total"
        )
    );

    const tabela = $("lista-pagamentos-conta");

    tabela.replaceChildren();

    const pagamentos = conta.pagamentos || [];

    if (pagamentos.length === 0) {

        linhaVazia(
            tabela,
            3,
            "Nenhum pagamento necessário para esta conta."
        );
    }

    pagamentos.forEach(pagamento => {

        const linha = document.createElement("tr");

        criarCelula(
            linha,
            FORMAS_PAGAMENTO[pagamento.formaPagamento] ||
            pagamento.formaPagamento
        );

        criarCelula(
            linha,
            formatarMoeda(pagamento.valor),
            "valor-monetario"
        );

        criarCelula(
            linha,
            formatarData(pagamento.dataHora)
        );

        tabela.append(linha);
    });

    const estornada = conta.status === "ESTORNADA";

    $("dados-estorno").hidden = !estornada;

    $("informacoes-estorno").replaceChildren();

    if (estornada) {

        $("informacoes-estorno").append(

            criarDado(
                "Motivo",
                conta.motivoEstorno
            ),

            criarDado(
                "Data do estorno",
                formatarData(conta.dataEstorno)
            ),

            criarDado(
                "Responsável pelo estorno",
                conta.nomeResponsavelEstorno
            )
        );
    }

    $("abrir-estorno").hidden =

        !podeEstornar() ||

        estornada ||

        estado.operacaoIncerta;
}


/* =========================================
   ESTORNO FINANCEIRO
   ========================================= */

function abrirEstorno() {

    const conta = estado.contaDetalhada;

    if (
        !conta ||
        estado.ocupado ||
        estado.operacaoIncerta ||
        !podeEstornar() ||
        conta.status !== "FINALIZADA"
    ) {
        return;
    }

    $("form-estorno").reset();

    ocultarMensagem("erro-estorno");

    definirTexto(
        "titulo-estorno",
        "Estornar conta #" + conta.id
    );

    $("resumo-estorno").replaceChildren(

        criarLinhaValor(
            "Conta",
            "#" + conta.id
        ),

        criarLinhaValor(
            "Mesa",
            conta.numeroMesa
        ),

        criarLinhaValor(
            "Valor a estornar",
            formatarMoeda(conta.valorPago),
            "linha-total"
        )
    );

    fecharModal("modal-detalhes-conta");

    $("modal-estorno").showModal();
}

async function confirmarEstorno(evento) {

    evento.preventDefault();

    const conta = estado.contaDetalhada;

    if (
        !conta ||
        estado.ocupado ||
        estado.operacaoIncerta ||
        !podeEstornar() ||
        conta.status !== "FINALIZADA"
    ) {
        return;
    }

    const motivo = $("motivo-estorno")
        .value.trim();

    if (!motivo || motivo.length > 255) {

        mensagem(
            "erro-estorno",
            "Informe uma justificativa válida para o estorno."
        );

        return;
    }

    if (!window.confirm(

        "Confirma o estorno integral da conta #" +
        conta.id +
        " no valor de " +
        formatarMoeda(conta.valorPago) +
        "?\n\nEsta operação não pode ser desfeita pela interface."

    )) {
        return;
    }

    ocultarMensagem("erro-estorno");

    definirOcupado(true);

    let resultado = null;

    try {

        resultado = await requisicaoApi(

            API_FINANCEIRA +
            "/contas/" +
            conta.id +
            "/estornar",

            "PATCH",

            { motivo: motivo }
        );

    } catch (erro) {

        tratarOperacaoIncerta(
            "erro-estorno",
            erro
        );

    } finally {

        definirOcupado(false);
    }

    if (resultado) {

        fecharModal("modal-estorno");

        await atualizarDados();

        mensagem(
            "mensagem-pagamentos",
            "Conta #" + resultado.id +
            " estornada com sucesso.",
            "sucesso"
        );

        await abrirDetalhesConta(resultado.id);
    }
}


/* =========================================
   TRATAMENTO DE OPERACOES INCERTAS
   ========================================= */

function tratarOperacaoIncerta(idMensagem, erro) {

    estado.operacaoIncerta = true;

    mensagem(

        idMensagem,

        mensagemErro(erro) +

        " Feche esta janela e utilize Atualizar dados. " +

        "Confira a situação da mesa e o histórico " +

        "antes de repetir a operação."
    );

    $("confirmar-finalizacao").disabled = true;

    $("confirmar-estorno").disabled = true;

    $("solicitar-pagamento").disabled = true;

    atualizarCalculos();
}


/* =========================================
   ATUALIZACAO DOS DADOS
   ========================================= */

async function atualizarDados() {

    if (
        estado.carregando ||
        estado.ocupado
    ) {
        return false;
    }

    estado.carregando = true;

    $("atualizar-pagamentos").disabled = true;

    ocultarMensagem("mensagem-pagamentos");

    try {

        const resultados = await Promise.allSettled([

            carregarResumo(),

            carregarContasAbertas(),

            carregarHistorico()
        ]);

        const falha = resultados.find(
            resultado => resultado.status === "rejected"
        );

        if (falha) {
            throw falha.reason;
        }

        const estavaIncerta = estado.operacaoIncerta;

        estado.operacaoIncerta = false;

        if (estavaIncerta) {

            selecionarAba("historico");

            mensagem(
                "mensagem-pagamentos",
                "Dados atualizados. Confira o histórico e a situação da mesa antes de repetir qualquer operação.",
                "sucesso"
            );
        }

        definirTexto(
            "ultima-atualizacao",
            "Última atualização: " +
            new Date().toLocaleString("pt-BR")
        );

        return true;

    } catch (erro) {

        mensagem(
            "mensagem-pagamentos",
            mensagemErro(erro)
        );

        return false;

    } finally {

        estado.carregando = false;

        $("atualizar-pagamentos").disabled = false;
    }
}


/* =========================================
   NAVEGACAO ENTRE ABAS
   ========================================= */

function selecionarAba(aba) {

    estado.aba = aba;

    const abertas = aba === "abertas";

    $("painel-abertas").hidden = !abertas;

    $("painel-historico").hidden = abertas;

    $("aba-abertas").classList.toggle(
        "selecionada",
        abertas
    );

    $("aba-historico").classList.toggle(
        "selecionada",
        !abertas
    );

    $("aba-abertas").setAttribute(
        "aria-pressed",
        String(abertas)
    );

    $("aba-historico").setAttribute(
        "aria-pressed",
        String(!abertas)
    );
}


/* =========================================
   REGISTRO DOS EVENTOS
   ========================================= */

function registrarEventos() {

    $("botao-sair").addEventListener(
        "click",
        () => sairDoSistema()
    );

    $("atualizar-pagamentos").addEventListener(
        "click",
        atualizarDados
    );

    $("aba-abertas").addEventListener(
        "click",
        () => selecionarAba("abertas")
    );

    $("aba-historico").addEventListener(
        "click",
        () => selecionarAba("historico")
    );

    $("pesquisa-mesa").addEventListener(
        "input",
        renderizarContasAbertas
    );

    $("filtro-status-mesa").addEventListener(
        "change",
        renderizarContasAbertas
    );

    $("filtro-finalizacao").addEventListener(
        "change",
        renderizarContasAbertas
    );

    $("form-filtros-historico").addEventListener(
        "submit",
        evento => {

            evento.preventDefault();

            renderizarHistorico();
        }
    );

    $("limpar-filtros").addEventListener(
        "click",
        limparFiltrosHistorico
    );

    $("pesquisa-historico").addEventListener(
        "input",
        renderizarHistorico
    );

    $("filtro-status-conta").addEventListener(
        "change",
        renderizarHistorico
    );

    $("solicitar-pagamento").addEventListener(
        "click",
        solicitarPagamento
    );

    $("percentual-desconto").addEventListener(
        "input",
        atualizarCalculos
    );

    $("motivo-desconto").addEventListener(
        "input",
        atualizarCalculos
    );

    $("adicionar-pagamento").addEventListener(
        "click",
        adicionarPagamentoRestante
    );

    $("form-finalizacao").addEventListener(
        "submit",
        finalizarConta
    );

    $("abrir-estorno").addEventListener(
        "click",
        abrirEstorno
    );

    $("form-estorno").addEventListener(
        "submit",
        confirmarEstorno
    );

    const botoesFechar = {

        "fechar-conta-aberta":
            "modal-conta-aberta",

        "cancelar-finalizacao":
            "modal-conta-aberta",

        "fechar-detalhes-conta":
            "modal-detalhes-conta",

        "voltar-detalhes":
            "modal-detalhes-conta",

        "fechar-estorno":
            "modal-estorno",

        "cancelar-estorno":
            "modal-estorno"
    };

    Object.entries(botoesFechar).forEach(
        ([botao, modal]) => {

            $(botao).addEventListener(
                "click",
                () => fecharModal(modal)
            );
        }
    );

    [
        "modal-conta-aberta",
        "modal-detalhes-conta",
        "modal-estorno"
    ].forEach(id => {

        $(id).addEventListener(
            "cancel",
            evento => {

                if (estado.ocupado) {
                    evento.preventDefault();
                }
            }
        );
    });
}


/* =========================================
   INICIALIZACAO DO MODULO
   ========================================= */

async function inicializarPagamentos() {

    registrarEventos();

    try {

        const autorizado = await carregarUsuario();

        if (!autorizado) {
            return;
        }

        selecionarAba("abertas");

        await atualizarDados();

    } catch (erro) {

        mensagem(
            "mensagem-pagamentos",
            mensagemErro(erro)
        );
    }
}

document.addEventListener(
    "DOMContentLoaded",
    inicializarPagamentos
);