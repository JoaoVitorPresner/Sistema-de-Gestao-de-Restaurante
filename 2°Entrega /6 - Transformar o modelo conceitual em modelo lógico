# 6. Transformação do Modelo Conceitual em Modelo Lógico

Nesta etapa, o **modelo conceitual do banco de dados** foi transformado em um **modelo lógico relacional**.

No modelo lógico, as entidades identificadas anteriormente são transformadas em tabelas, com a definição de suas **chaves primárias (PK)** e **chaves estrangeiras (FK)**.

Os relacionamentos entre as entidades também são representados por meio das chaves estrangeiras, preparando a estrutura para a futura implementação em um Sistema Gerenciador de Banco de Dados (SGBD).

---

## 6.1 Modelo Lógico

O modelo lógico do **Sistema de Gestão de Restaurante** é apresentado abaixo.

![Modelo Lógico do Banco de Dados](./imagens/modelo-logico-banco-de-dados.png)

[🔎 Abrir Modelo Lógico em tamanho original](./imagens/modelo-logico-banco-de-dados.png)

---

## 6.2 Tabelas do Modelo Lógico

O modelo lógico é composto pelas seguintes tabelas:

- **PERFIL**
- **USUARIO**
- **CLIENTE**
- **CATEGORIA**
- **PRODUTO**
- **INGREDIENTE**
- **PRODUTO_INGREDIENTE**
- **MESA**
- **CONTA**
- **PEDIDO**
- **ITEM_PEDIDO**
- **PAGAMENTO**
- **MOVIMENTACAO_ESTOQUE**
- **HISTORICO_ALTERACAO**
- **RESERVA_INGREDIENTE**

---

## 6.3 Estrutura das Tabelas

### PERFIL

```text
PERFIL
-------------------------
PK  id_perfil
    nome
    descricao
```

Armazena os perfis de acesso utilizados pelos usuários internos do sistema.

---

### USUARIO

```text
USUARIO
-------------------------
PK  id_usuario
    nome
    email
    login
    senha_hash
    ativo
FK  id_perfil
```

**Relacionamento:**

`USUARIO.id_perfil → PERFIL.id_perfil`

Cada usuário interno possui um perfil responsável por determinar suas permissões de acesso.

---

### CLIENTE

```text
CLIENTE
-------------------------
PK  id_cliente
    nome
    telefone
    email
```

Armazena os dados dos clientes cadastrados.

A identificação do cliente é opcional durante o atendimento presencial.

---

### CATEGORIA

```text
CATEGORIA
-------------------------
PK  id_categoria
    nome
    descricao
```

Armazena as categorias utilizadas para organizar os produtos do cardápio.

---

### PRODUTO

```text
PRODUTO
-------------------------
PK  id_produto
    nome
    descricao
    preco
    ativo
FK  id_categoria
```

**Relacionamento:**

`PRODUTO.id_categoria → CATEGORIA.id_categoria`

Cada produto pertence a uma categoria.

---

### INGREDIENTE

```text
INGREDIENTE
-------------------------
PK  id_ingrediente
    nome
    quantidade_atual
    quantidade_minima
    unidade_medida
```

Armazena os ingredientes controlados pelo estoque e suas respectivas quantidades.

---

### PRODUTO_INGREDIENTE

```text
PRODUTO_INGREDIENTE
-------------------------
PK/FK  id_produto
PK/FK  id_ingrediente
       quantidade_necessaria
```

**Relacionamentos:**

`PRODUTO_INGREDIENTE.id_produto → PRODUTO.id_produto`

`PRODUTO_INGREDIENTE.id_ingrediente → INGREDIENTE.id_ingrediente`

A tabela resolve o relacionamento muitos-para-muitos entre **PRODUTO** e **INGREDIENTE**.

A chave primária é composta por:

`(id_produto, id_ingrediente)`

O atributo `quantidade_necessaria` informa quanto de determinado ingrediente é necessário para preparar uma unidade do produto.

---

### MESA

```text
MESA
-------------------------
PK  id_mesa
    numero
    capacidade
    status
```

Representa as mesas utilizadas durante os atendimentos.

O número da mesa deve ser único.

---

### CONTA

```text
CONTA
-------------------------
PK  id_conta
    data_abertura
    data_fechamento
    status
    valor_total
    desconto
FK  id_mesa
FK  id_cliente
```

**Relacionamentos:**

`CONTA.id_mesa → MESA.id_mesa`

`CONTA.id_cliente → CLIENTE.id_cliente`

A conta representa o agrupamento financeiro do atendimento.

O campo `id_cliente` pode ser nulo, pois a identificação do cliente não é obrigatória no atendimento presencial.

---

### PEDIDO

```text
PEDIDO
-------------------------
PK  id_pedido
    data_hora
    status
    valor_total
FK  id_conta
```

**Relacionamento:**

`PEDIDO.id_conta → CONTA.id_conta`

Cada pedido pertence a uma conta.

---

### ITEM_PEDIDO

```text
ITEM_PEDIDO
-------------------------
PK  id_item_pedido
    quantidade
    valor_unitario
    observacao
FK  id_pedido
FK  id_produto
```

**Relacionamentos:**

`ITEM_PEDIDO.id_pedido → PEDIDO.id_pedido`

`ITEM_PEDIDO.id_produto → PRODUTO.id_produto`

Cada registro representa um produto incluído em determinado pedido.

O valor unitário é armazenado no item para preservar o preço utilizado no momento da realização do pedido, mesmo que o preço do produto seja alterado posteriormente.

---

### PAGAMENTO

```text
PAGAMENTO
-------------------------
PK  id_pagamento
    valor
    modalidade
    data_pagamento
FK  id_conta
```

**Relacionamento:**

`PAGAMENTO.id_conta → CONTA.id_conta`

Uma conta pode possuir vários pagamentos, permitindo a utilização de mais de uma modalidade de pagamento.

---

### MOVIMENTACAO_ESTOQUE

```text
MOVIMENTACAO_ESTOQUE
-------------------------
PK  id_movimentacao
    tipo
    quantidade
    data_hora
FK  id_ingrediente
FK  id_usuario
```

**Relacionamentos:**

`MOVIMENTACAO_ESTOQUE.id_ingrediente → INGREDIENTE.id_ingrediente`

`MOVIMENTACAO_ESTOQUE.id_usuario → USUARIO.id_usuario`

Registra as movimentações realizadas no estoque e o usuário responsável pela operação.

---

### HISTORICO_ALTERACAO

```text
HISTORICO_ALTERACAO
-------------------------
PK  id_historico
    tipo_alteracao
    descricao
    data_hora
FK  id_usuario
```

**Relacionamento:**

`HISTORICO_ALTERACAO.id_usuario → USUARIO.id_usuario`

Registra alterações relevantes realizadas no sistema e o usuário responsável.

---

### RESERVA_INGREDIENTE

```text
RESERVA_INGREDIENTE
-------------------------
PK  id_reserva
    quantidade
FK  id_pedido
FK  id_ingrediente
```

**Relacionamentos:**

`RESERVA_INGREDIENTE.id_pedido → PEDIDO.id_pedido`

`RESERVA_INGREDIENTE.id_ingrediente → INGREDIENTE.id_ingrediente`

Essa tabela representa os ingredientes reservados para pedidos confirmados.

Ela permite identificar qual pedido realizou a reserva, qual ingrediente foi reservado e a quantidade correspondente.

Quando o pedido é cancelado antes do início do preparo, as reservas correspondentes são liberadas.

Quando o pedido atinge o status **Pronto**, os ingredientes são baixados do estoque e as reservas correspondentes deixam de ser consideradas ativas.

---

## 6.4 Principais Relacionamentos

| Tabela de origem | Chave estrangeira | Tabela referenciada | Cardinalidade |
|---|---|---|---|
| USUARIO | id_perfil | PERFIL | N:1 |
| PRODUTO | id_categoria | CATEGORIA | N:1 |
| PRODUTO_INGREDIENTE | id_produto | PRODUTO | N:1 |
| PRODUTO_INGREDIENTE | id_ingrediente | INGREDIENTE | N:1 |
| CONTA | id_mesa | MESA | N:1 |
| CONTA | id_cliente | CLIENTE | N:1 opcional |
| PEDIDO | id_conta | CONTA | N:1 |
| ITEM_PEDIDO | id_pedido | PEDIDO | N:1 |
| ITEM_PEDIDO | id_produto | PRODUTO | N:1 |
| PAGAMENTO | id_conta | CONTA | N:1 |
| MOVIMENTACAO_ESTOQUE | id_ingrediente | INGREDIENTE | N:1 |
| MOVIMENTACAO_ESTOQUE | id_usuario | USUARIO | N:1 |
| HISTORICO_ALTERACAO | id_usuario | USUARIO | N:1 |
| RESERVA_INGREDIENTE | id_pedido | PEDIDO | N:1 |
| RESERVA_INGREDIENTE | id_ingrediente | INGREDIENTE | N:1 |

---

## 6.5 Principais Restrições do Modelo

Durante a transformação do modelo conceitual para o modelo lógico, foram consideradas as seguintes restrições:

| Regra | Implementação no modelo lógico |
|---|---|
| Cada usuário possui um perfil ativo | `USUARIO.id_perfil` é uma chave estrangeira obrigatória |
| Login de usuário não pode ser repetido | `USUARIO.login` deve possuir restrição `UNIQUE` |
| E-mail de usuário não pode ser repetido | `USUARIO.email` deve possuir restrição `UNIQUE` |
| Número da mesa não pode ser repetido | `MESA.numero` deve possuir restrição `UNIQUE` |
| Cliente é opcional no atendimento presencial | `CONTA.id_cliente` pode possuir valor nulo |
| Produto pertence a uma categoria | `PRODUTO.id_categoria` referencia `CATEGORIA` |
| Produto pode utilizar vários ingredientes | Relação realizada por `PRODUTO_INGREDIENTE` |
| Ingrediente pode participar de vários produtos | Relação realizada por `PRODUTO_INGREDIENTE` |
| Um pedido pode possuir vários itens | Relação `PEDIDO 1:N ITEM_PEDIDO` |
| Uma conta pode possuir vários pedidos | Relação `CONTA 1:N PEDIDO` |
| Uma conta pode possuir vários pagamentos | Relação `CONTA 1:N PAGAMENTO` |
| Estoque físico não pode ficar abaixo de zero | Movimentações devem impedir `quantidade_atual < 0` |
| Ingredientes devem ser reservados após a confirmação do pedido | Reservas são registradas em `RESERVA_INGREDIENTE` |
| Cancelamento antes do preparo libera os ingredientes | Reservas correspondentes ao pedido devem ser liberadas |
| Pedido com status Pronto realiza a baixa dos ingredientes | Quantidades são baixadas do estoque e as reservas correspondentes são encerradas |
| Alterações relevantes devem possuir histórico | Registros são armazenados em `HISTORICO_ALTERACAO` |

---

## 6.6 Transformação dos Relacionamentos N:N

O principal relacionamento muitos-para-muitos identificado no modelo conceitual ocorre entre **Produto** e **Ingrediente**.

Conceitualmente:

```text
PRODUTO N ───────── N INGREDIENTE
```

No modelo lógico, esse relacionamento é transformado utilizando a tabela associativa `PRODUTO_INGREDIENTE`:

```text
PRODUTO
   │
   │ 1
   │
   │ N
PRODUTO_INGREDIENTE
   │
   │ N
   │
   │ 1
INGREDIENTE
```

A tabela associativa possui as chaves estrangeiras:

- `id_produto`
- `id_ingrediente`

Essas duas chaves formam uma **chave primária composta**.

Além disso, a tabela possui o atributo `quantidade_necessaria`, responsável por indicar a quantidade do ingrediente utilizada na preparação de uma unidade do produto.

---

## 6.7 Representação da Reserva de Estoque

A entidade conceitual **ReservaIngrediente** foi transformada na tabela `RESERVA_INGREDIENTE`.

Sua estrutura lógica é:

```text
PEDIDO
   │
   │ 1
   │
   │ N
RESERVA_INGREDIENTE
   │
   │ N
   │
   │ 1
INGREDIENTE
```

Essa estrutura permite que um pedido possua várias reservas e que um mesmo ingrediente possa estar reservado para diferentes pedidos.

Com isso, o banco de dados consegue representar as regras relacionadas à verificação, reserva, liberação e baixa dos ingredientes.

---

## Resultado

A transformação do modelo conceitual em modelo lógico definiu a estrutura relacional necessária para armazenar os dados do **Sistema de Gestão de Restaurante**.

As entidades foram transformadas em tabelas e seus relacionamentos passaram a ser representados por chaves primárias e estrangeiras.

Também foram representadas as relações entre produtos e ingredientes, pedidos e itens, contas e pagamentos, usuários e perfis, movimentações de estoque e reservas de ingredientes.

O modelo lógico servirá como base para a definição detalhada dos campos no **dicionário de dados** e para uma futura implementação do banco de dados em um SGBD relacional.
