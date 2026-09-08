# 7. Dicionário de Dados

O dicionário de dados apresenta a descrição detalhada das tabelas e dos campos que compõem o banco de dados do **Sistema de Gestão de Restaurante**.

Para cada campo são apresentados seu nome, tipo de dado, tamanho, obrigatoriedade, chave e descrição.

As seguintes identificações são utilizadas:

- **PK (Primary Key):** chave primária.
- **FK (Foreign Key):** chave estrangeira.
- **PK/FK:** campo que participa da chave primária e também é uma chave estrangeira.
- **UNIQUE:** campo que não permite valores repetidos.

---

## 7.1 PERFIL

Armazena os perfis de acesso dos usuários internos.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_perfil | INT | — | Sim | PK | Identificador único do perfil. |
| nome | VARCHAR | 50 | Sim | — | Nome do perfil. |
| descricao | VARCHAR | 255 | Não | — | Descrição das características do perfil. |

---

## 7.2 USUARIO

Armazena os usuários internos que possuem acesso ao sistema.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_usuario | INT | — | Sim | PK | Identificador único do usuário. |
| nome | VARCHAR | 100 | Sim | — | Nome do usuário. |
| email | VARCHAR | 150 | Sim | UNIQUE | E-mail do usuário. |
| login | VARCHAR | 50 | Sim | UNIQUE | Login utilizado para autenticação. |
| senha_hash | VARCHAR | 255 | Sim | — | Hash da senha do usuário. |
| ativo | BOOLEAN | — | Sim | — | Indica se o usuário está ativo. |
| id_perfil | INT | — | Sim | FK | Perfil de acesso associado ao usuário. |

**Referência:** `id_perfil → PERFIL(id_perfil)`

---

## 7.3 CLIENTE

Armazena os dados dos clientes cadastrados.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_cliente | INT | — | Sim | PK | Identificador único do cliente. |
| nome | VARCHAR | 100 | Não | — | Nome do cliente. |
| telefone | VARCHAR | 20 | Não | — | Telefone do cliente. |
| email | VARCHAR | 150 | Não | — | E-mail do cliente. |

A identificação do cliente é opcional durante o atendimento presencial.

---

## 7.4 CATEGORIA

Armazena as categorias utilizadas para organizar os produtos do cardápio.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_categoria | INT | — | Sim | PK | Identificador único da categoria. |
| nome | VARCHAR | 100 | Sim | — | Nome da categoria. |
| descricao | VARCHAR | 255 | Não | — | Descrição da categoria. |

---

## 7.5 PRODUTO

Armazena os produtos disponíveis no cardápio.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_produto | INT | — | Sim | PK | Identificador único do produto. |
| nome | VARCHAR | 100 | Sim | — | Nome do produto. |
| descricao | VARCHAR | 255 | Não | — | Descrição do produto. |
| preco | DECIMAL | 10,2 | Sim | — | Preço atual do produto. |
| ativo | BOOLEAN | — | Sim | — | Indica se o produto está ativo no cardápio. |
| id_categoria | INT | — | Sim | FK | Categoria à qual o produto pertence. |

**Referência:** `id_categoria → CATEGORIA(id_categoria)`

---

## 7.6 INGREDIENTE

Armazena os ingredientes e itens controlados pelo estoque.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_ingrediente | INT | — | Sim | PK | Identificador único do ingrediente. |
| nome | VARCHAR | 100 | Sim | — | Nome do ingrediente. |
| quantidade_atual | DECIMAL | 10,3 | Sim | — | Quantidade física atual disponível no estoque. |
| quantidade_minima | DECIMAL | 10,3 | Sim | — | Quantidade mínima utilizada para geração de alerta. |
| unidade_medida | VARCHAR | 20 | Sim | — | Unidade utilizada para controlar o ingrediente. |

---

## 7.7 PRODUTO_INGREDIENTE

Representa a composição dos produtos e resolve o relacionamento muitos-para-muitos entre `PRODUTO` e `INGREDIENTE`.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_produto | INT | — | Sim | PK/FK | Produto relacionado à composição. |
| id_ingrediente | INT | — | Sim | PK/FK | Ingrediente utilizado no produto. |
| quantidade_necessaria | DECIMAL | 10,3 | Sim | — | Quantidade do ingrediente necessária para preparar uma unidade do produto. |

**Chave primária composta:** `(id_produto, id_ingrediente)`

**Referências:**

- `id_produto → PRODUTO(id_produto)`
- `id_ingrediente → INGREDIENTE(id_ingrediente)`

---

## 7.8 MESA

Armazena as mesas utilizadas no atendimento.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_mesa | INT | — | Sim | PK | Identificador único da mesa. |
| numero | INT | — | Sim | UNIQUE | Número utilizado para identificar a mesa. |
| capacidade | INT | — | Sim | — | Quantidade de pessoas suportada pela mesa. |
| status | VARCHAR | 20 | Sim | — | Situação atual da mesa. |

---

## 7.9 CONTA

Representa o agrupamento financeiro de um atendimento realizado em uma mesa.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_conta | INT | — | Sim | PK | Identificador único da conta. |
| data_abertura | DATETIME | — | Sim | — | Data e hora de abertura da conta. |
| data_fechamento | DATETIME | — | Não | — | Data e hora de encerramento da conta. |
| status | VARCHAR | 20 | Sim | — | Situação atual da conta. |
| valor_total | DECIMAL | 10,2 | Sim | — | Valor total da conta. |
| desconto | DECIMAL | 10,2 | Sim | — | Valor do desconto aplicado à conta. |
| id_mesa | INT | — | Sim | FK | Mesa associada ao atendimento. |
| id_cliente | INT | — | Não | FK | Cliente associado à conta, quando identificado. |

**Referências:**

- `id_mesa → MESA(id_mesa)`
- `id_cliente → CLIENTE(id_cliente)`

O campo `id_cliente` é opcional, permitindo atendimento presencial sem cadastro ou identificação do cliente.

---

## 7.10 PEDIDO

Armazena os pedidos realizados durante os atendimentos.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_pedido | INT | — | Sim | PK | Identificador único do pedido. |
| data_hora | DATETIME | — | Sim | — | Data e hora em que o pedido foi registrado. |
| status | VARCHAR | 20 | Sim | — | Status atual do pedido. |
| valor_total | DECIMAL | 10,2 | Sim | — | Valor total do pedido. |
| id_conta | INT | — | Sim | FK | Conta à qual o pedido pertence. |

**Referência:** `id_conta → CONTA(id_conta)`

Os principais status previstos são:

- Recebido
- Em preparo
- Pronto
- Entregue
- Cancelado

---

## 7.11 ITEM_PEDIDO

Armazena os produtos presentes em cada pedido.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_item_pedido | INT | — | Sim | PK | Identificador único do item do pedido. |
| quantidade | INT | — | Sim | — | Quantidade do produto solicitada. |
| valor_unitario | DECIMAL | 10,2 | Sim | — | Valor unitário do produto no momento do pedido. |
| observacao | VARCHAR | 255 | Não | — | Observação relacionada ao item. |
| id_pedido | INT | — | Sim | FK | Pedido ao qual o item pertence. |
| id_produto | INT | — | Sim | FK | Produto solicitado. |

**Referências:**

- `id_pedido → PEDIDO(id_pedido)`
- `id_produto → PRODUTO(id_produto)`

O `valor_unitario` é armazenado no item para preservar o preço utilizado no momento da realização do pedido, mesmo que o preço atual do produto seja alterado posteriormente.

---

## 7.12 PAGAMENTO

Armazena os pagamentos realizados para as contas.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_pagamento | INT | — | Sim | PK | Identificador único do pagamento. |
| valor | DECIMAL | 10,2 | Sim | — | Valor pago. |
| modalidade | VARCHAR | 20 | Sim | — | Modalidade utilizada no pagamento. |
| data_pagamento | DATETIME | — | Sim | — | Data e hora do pagamento. |
| id_conta | INT | — | Sim | FK | Conta à qual o pagamento pertence. |

**Referência:** `id_conta → CONTA(id_conta)`

As modalidades previstas são:

- Dinheiro
- Cartão
- Pix

Uma conta pode possuir vários registros de pagamento, permitindo utilizar mais de uma modalidade.

---

## 7.13 MOVIMENTACAO_ESTOQUE

Registra as movimentações realizadas no estoque.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_movimentacao | INT | — | Sim | PK | Identificador único da movimentação. |
| tipo | VARCHAR | 20 | Sim | — | Tipo da movimentação realizada. |
| quantidade | DECIMAL | 10,3 | Sim | — | Quantidade movimentada. |
| data_hora | DATETIME | — | Sim | — | Data e hora da movimentação. |
| id_ingrediente | INT | — | Sim | FK | Ingrediente movimentado. |
| id_usuario | INT | — | Sim | FK | Usuário responsável pela movimentação. |

**Referências:**

- `id_ingrediente → INGREDIENTE(id_ingrediente)`
- `id_usuario → USUARIO(id_usuario)`

---

## 7.14 HISTORICO_ALTERACAO

Armazena o histórico das alterações relevantes realizadas no sistema.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_historico | INT | — | Sim | PK | Identificador único do registro de histórico. |
| tipo_alteracao | VARCHAR | 50 | Sim | — | Tipo da alteração realizada. |
| descricao | VARCHAR | 255 | Sim | — | Descrição da alteração. |
| data_hora | DATETIME | — | Sim | — | Data e hora da alteração. |
| id_usuario | INT | — | Sim | FK | Usuário responsável pela alteração. |

**Referência:** `id_usuario → USUARIO(id_usuario)`

---

## 7.15 RESERVA_INGREDIENTE

Armazena os ingredientes reservados para pedidos confirmados.

| Campo | Tipo | Tamanho | Obrigatório | Chave | Descrição |
|---|---|---:|:---:|---|---|
| id_reserva | INT | — | Sim | PK | Identificador único da reserva. |
| quantidade | DECIMAL | 10,3 | Sim | — | Quantidade do ingrediente reservada. |
| id_pedido | INT | — | Sim | FK | Pedido responsável pela reserva. |
| id_ingrediente | INT | — | Sim | FK | Ingrediente reservado. |

**Referências:**

- `id_pedido → PEDIDO(id_pedido)`
- `id_ingrediente → INGREDIENTE(id_ingrediente)`

A reserva é criada quando o pedido é confirmado.

Caso o pedido seja cancelado antes do início do preparo, os ingredientes reservados são liberados.

Quando o pedido atingir o status **Pronto**, os ingredientes utilizados são baixados do estoque e suas reservas deixam de ser consideradas ativas.

---

## 7.16 Resumo das Chaves Estrangeiras

| Tabela | Campo | Referência |
|---|---|---|
| USUARIO | id_perfil | PERFIL(id_perfil) |
| PRODUTO | id_categoria | CATEGORIA(id_categoria) |
| PRODUTO_INGREDIENTE | id_produto | PRODUTO(id_produto) |
| PRODUTO_INGREDIENTE | id_ingrediente | INGREDIENTE(id_ingrediente) |
| CONTA | id_mesa | MESA(id_mesa) |
| CONTA | id_cliente | CLIENTE(id_cliente) |
| PEDIDO | id_conta | CONTA(id_conta) |
| ITEM_PEDIDO | id_pedido | PEDIDO(id_pedido) |
| ITEM_PEDIDO | id_produto | PRODUTO(id_produto) |
| PAGAMENTO | id_conta | CONTA(id_conta) |
| MOVIMENTACAO_ESTOQUE | id_ingrediente | INGREDIENTE(id_ingrediente) |
| MOVIMENTACAO_ESTOQUE | id_usuario | USUARIO(id_usuario) |
| HISTORICO_ALTERACAO | id_usuario | USUARIO(id_usuario) |
| RESERVA_INGREDIENTE | id_pedido | PEDIDO(id_pedido) |
| RESERVA_INGREDIENTE | id_ingrediente | INGREDIENTE(id_ingrediente) |

---

## Resultado

O dicionário de dados detalha a estrutura das tabelas definidas no modelo lógico do **Sistema de Gestão de Restaurante**, especificando os campos, tipos de dados, obrigatoriedade, chaves e finalidade de cada informação armazenada.

A estrutura mantém a consistência com os requisitos, regras de negócio e modelos desenvolvidos anteriormente, incluindo o controle de usuários, clientes, cardápio, mesas, contas, pedidos, pagamentos, estoque, histórico de alterações e reserva de ingredientes.
