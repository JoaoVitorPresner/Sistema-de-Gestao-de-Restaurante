# Sistema de Gestão de Restaurante

Sistema web desenvolvido para auxiliar restaurantes e lanchonetes no gerenciamento integrado de suas principais atividades, incluindo mesas, pedidos, cardápio, cozinha, estoque, vendas e pagamentos.

---

## Problema

Restaurantes precisam administrar simultaneamente diferentes atividades, como atendimento de clientes, registro de pedidos, comunicação com a cozinha, controle de mesas, acompanhamento do estoque e fechamento das vendas.

Quando essas atividades são realizadas manualmente ou por meio de ferramentas separadas, podem ocorrer erros nos pedidos, falhas de comunicação entre atendimento e cozinha, atrasos no preparo, dificuldades no controle de estoque e problemas no acompanhamento das vendas.

O projeto busca centralizar essas informações e processos em um único sistema.

---

## Objetivo

Desenvolver um sistema web para gerenciar e otimizar as principais operações de um restaurante, incluindo pedidos, mesas, produtos, estoque, vendas, pagamentos e usuários, buscando melhorar a organização e o controle das atividades do estabelecimento.

O sistema também busca integrar o atendimento, a cozinha e o estoque, permitindo que as informações dos pedidos sejam acompanhadas durante todo o fluxo de atendimento.

---

## Equipe

- **João Vitor Barros Presner**
- **Guilhermy Martineli Brenny**
- **Matheus Borges**
- **Mateus Henrique Presner**

---

## Principais funcionalidades

Entre as principais funcionalidades previstas para o sistema estão:

- autenticação de usuários internos;
- controle de acesso de acordo com o perfil do usuário;
- gerenciamento de usuários e clientes;
- gerenciamento de produtos e categorias do cardápio;
- gerenciamento de mesas;
- registro e acompanhamento de pedidos;
- acesso do cliente ao cardápio por meio da identificação da mesa;
- interface de acompanhamento dos pedidos para a cozinha;
- controle de ingredientes e estoque;
- verificação e reserva de ingredientes durante a realização dos pedidos;
- registro de movimentações de estoque;
- fechamento de contas;
- registro de pagamentos em dinheiro, cartão e Pix;
- aplicação de descontos conforme as permissões do usuário;
- emissão de comprovantes não fiscais;
- geração de relatórios básicos;
- registro do histórico de alterações relevantes.

---

## Tecnologias previstas

| Camada / elemento | Tecnologia |
|---|---|
| **Interface / Frontend** | HTML, CSS e JavaScript |
| **Backend / Servidor** | Java + Spring Boot |
| **Banco de dados** | MySQL |
| **Controle de versão** | Git e GitHub |
| **Protótipos** | Figma |
| **Hospedagem** | A definir |

A aplicação será desenvolvida para funcionar em navegadores modernos e possuir interface responsiva para diferentes tamanhos de tela.

---

## MVP

O MVP será uma versão reduzida do sistema capaz de executar o fluxo principal de atendimento do restaurante, desde a autenticação do usuário até o fechamento da conta.

O fluxo principal previsto é:

**Autenticar usuário → Selecionar mesa → Consultar cardápio → Selecionar produtos → Verificar disponibilidade dos ingredientes → Confirmar pedido → Reservar ingredientes → Enviar para cozinha → Preparar pedido → Marcar como pronto → Entregar pedido → Registrar pagamento → Encerrar conta.**

O MVP deverá contemplar principalmente:

- autenticação;
- controle de acesso por perfil;
- controle de mesas;
- cardápio;
- registro de pedidos;
- acesso do cliente pela identificação da mesa;
- verificação e reserva de ingredientes;
- interface da cozinha;
- acompanhamento dos status dos pedidos;
- baixa dos ingredientes;
- fechamento da conta;
- registro de pagamento.

Funcionalidades complementares poderão ser desenvolvidas posteriormente, após a validação desse fluxo principal.

---

# Organização do projeto

O repositório está organizado por etapas de desenvolvimento. Cada entrega possui sua própria pasta com os documentos e artefatos correspondentes.

```text
Sistema-de-Gestao-de-Restaurante/
│
├── 1°Entrega/
│   └── Visão do projeto e requisitos
│
├── 2°Entrega/
│   └── Modelagem do software e banco de dados
│
├── 3°Entrega/
│   └── Planejamento, arquitetura, protótipos e MVP
│
└── README.md
```

---

# 1ª Entrega — Visão e Requisitos

A primeira entrega apresenta a definição inicial do projeto, incluindo problema, objetivos, público-alvo, escopo, requisitos e regras de negócio.

### Documentação

1. [📄 Identificação do Projeto](./1%C2%B0Entrega/1-Identifica%C3%A7%C3%A3o.md)
2. [📄 Contexto e Problema](./1%C2%B0Entrega/2-Contexto%20e%20Problema.md)
3. [📄 Objetivos](./1%C2%B0Entrega/3-Objetivos.md)
4. [📄 Público-Alvo e Perfis de Usuário](./1%C2%B0Entrega/4-P%C3%BAblico%20Alvo%20e%20Perfis%20de%20Usu%C3%A1rio.md)
5. [📄 Escopo](./1%C2%B0Entrega/5-Escopo.md)
6. [📄 Requisitos Funcionais](./1%C2%B0Entrega/6-Requisitos%20Funcionais.md)
7. [📄 Requisitos Não Funcionais](./1%C2%B0Entrega/7-Requisitos%20n%C3%A3o%20funcionais.md)
8. [📄 Regras de Negócio](./1%C2%B0Entrega/8-Regras%20de%20Neg%C3%B3cio.md)

### Documento completo

[📑 Visualizar PDF da 1ª Entrega](./1%C2%B0Entrega/Sistema%20de%20Gestao%20de%20Restaurante.pdf)

---

# 2ª Entrega — Modelagem do Software e Banco de Dados

A segunda entrega transforma os requisitos levantados anteriormente em modelos comportamentais e estruturais, além de definir a estrutura lógica do banco de dados.

### Documentação

1. [📄 Revisão da Entrega 1](./2%C2%B0Entrega/1-Revisar%20a%20Entrega%201.md)
2. [📄 Diagrama de Casos de Uso](./2%C2%B0Entrega/2-Construir%20o%20diagrama%20de%20casos%20de%20uso.md)
3. [📄 Especificação dos Casos de Uso](./2%C2%B0Entrega/3-Especificar%20pelo%20menos%20cinco%20casos%20de%20uso%20ou%20hist%C3%B3rias%20de%20usu%C3%A1rio.md)
4. [📄 Diagramas de Atividades](./2%C2%B0Entrega/4-Criar%20pelo%20menos%20dois%20diagramas%20de%20atividades%20ou%20fluxos.md)
5. [📄 Modelo Conceitual do Banco de Dados](./2%C2%B0Entrega/5-Construir%20o%20modelo%20conceitual%20do%20banco%20de%20dados.md)
6. [📄 Modelo Lógico do Banco de Dados](./2%C2%B0Entrega/6-Transformar%20o%20modelo%20conceitual%20em%20modelo%20l%C3%B3gico.md)
7. [📄 Dicionário de Dados](./2%C2%B0Entrega/7-Criar%20o%20dicion%C3%A1rio%20de%20dados.md)
8. [📄 Matriz de Rastreabilidade](./2%C2%B0Entrega/8-Montar%20a%20matriz%20de%20rastreabilidade.md)
9. [📄 Revisão Cruzada dos Modelos](./2%C2%B0Entrega/9-Fazer%20a%20revis%C3%A3o%20cruzada%20dos%20modelos.md)

### Documento completo

[📑 Visualizar PDF da 2ª Entrega](./2%C2%B0Entrega/Entrega2doSistemadeGestaodeRestaurante.pdf)

---

# 3ª Entrega — Planejamento, Arquitetura, Protótipos e MVP

A terceira entrega organiza a preparação para a implementação do sistema, incluindo planejamento do trabalho, definição das tecnologias, arquitetura, navegação, protótipos, MVP e rastreabilidade.

### Conteúdo

- organização e atualização do backlog;
- definição da arquitetura;
- definição das tecnologias;
- levantamento das telas;
- mapa de navegação;
- protótipos das principais telas;
- definição do MVP;
- definição do fluxo principal;
- rastreabilidade entre requisitos, funcionalidades, entidades, telas e MVP.

### Protótipos previstos

Os protótipos desenvolvidos representam as principais interfaces do fluxo do MVP:

- Tela de Login;
- Tela Inicial;
- Tela de Mesas;
- Tela de Novo Pedido;
- Tela da Cozinha;
- Tela de Acompanhamento;
- Tela de Pagamento;
- Tela de Estoque.

> Os links individuais da documentação da 3ª Entrega devem ser adicionados aqui após os respectivos arquivos serem enviados para a pasta `3°Entrega`.

---

## Fluxo de evolução do projeto

```text
1ª Entrega
Visão e Requisitos
      ↓
2ª Entrega
Modelagem do Software e Banco de Dados
      ↓
3ª Entrega
Planejamento, Arquitetura, Protótipos e MVP
      ↓
Implementação do Sistema
```

Cada etapa utiliza os artefatos produzidos anteriormente, mantendo a rastreabilidade entre **requisitos, casos de uso, processos, dados, interfaces e MVP**.

---

## Repositório

Este repositório reúne a documentação, os diagramas, os modelos de banco de dados, os protótipos e, posteriormente, o código-fonte desenvolvido durante a evolução do projeto **Sistema de Gestão de Restaurante**.
