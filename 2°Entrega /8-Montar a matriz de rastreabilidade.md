# 8. Matriz de Rastreabilidade

A matriz de rastreabilidade tem como objetivo relacionar os **requisitos funcionais** definidos para o Sistema de Gestão de Restaurante com os demais modelos desenvolvidos no projeto.

Ela permite verificar se cada requisito está representado nos casos de uso, processos e estruturas de dados, facilitando a identificação de possíveis inconsistências ou funcionalidades não modeladas.

---

## 8.1 Matriz de Rastreabilidade

| Requisito | Descrição resumida | Caso de uso relacionado | Processo / Diagrama | Entidades / Tabelas relacionadas |
|---|---|---|---|---|
| **RF01** | Cadastrar usuários internos e definir seus perfis | UC01 — Realizar autenticação | — | PERFIL, USUARIO |
| **RF02** | Permitir autenticação por login e senha | UC01 — Realizar autenticação | — | USUARIO |
| **RF03** | Controlar acesso conforme o perfil do usuário | UC01 — Realizar autenticação | — | PERFIL, USUARIO |
| **RF04** | Cadastrar, consultar, alterar e excluir clientes | — | — | CLIENTE |
| **RF05** | Gerenciar produtos e categorias do cardápio | — | — | PRODUTO, CATEGORIA |
| **RF06** | Cadastrar e gerenciar ingredientes do estoque | UC05 — Registrar movimentação de estoque | Registrar movimentação de estoque | INGREDIENTE, MOVIMENTACAO_ESTOQUE |
| **RF07** | Associar ingredientes aos produtos e suas quantidades | — | — | PRODUTO, INGREDIENTE, PRODUTO_INGREDIENTE |
| **RF08** | Registrar e gerenciar pedidos vinculados a uma mesa | UC02 — Registrar pedido | Realizar pedido | MESA, CONTA, PEDIDO, ITEM_PEDIDO |
| **RF09** | Permitir ao cliente realizar pedidos pela identificação da mesa | UC02 — Registrar pedido | Realizar pedido | MESA, CONTA, PEDIDO, ITEM_PEDIDO, PRODUTO |
| **RF10** | Acompanhar e atualizar o status dos pedidos | UC03 — Atualizar status do pedido | Processar pedido na cozinha | PEDIDO, HISTORICO_ALTERACAO |
| **RF11** | Disponibilizar interface para a cozinha visualizar e atualizar pedidos | UC03 — Atualizar status do pedido | Processar pedido na cozinha | PEDIDO, ITEM_PEDIDO |
| **RF12** | Verificar disponibilidade dos ingredientes antes da confirmação | UC02 — Registrar pedido | Realizar pedido | INGREDIENTE, PRODUTO_INGREDIENTE, PEDIDO |
| **RF13** | Reservar ingredientes quando o pedido for confirmado | UC02 — Registrar pedido | Realizar pedido | PEDIDO, INGREDIENTE, PRODUTO_INGREDIENTE, RESERVA_INGREDIENTE |
| **RF14** | Realizar baixa dos ingredientes quando o pedido atingir Pronto | UC03 — Atualizar status do pedido | Processar pedido na cozinha | PEDIDO, INGREDIENTE, RESERVA_INGREDIENTE, MOVIMENTACAO_ESTOQUE |
| **RF15** | Registrar e consultar movimentações de estoque | UC05 — Registrar movimentação de estoque | Registrar movimentação de estoque | INGREDIENTE, MOVIMENTACAO_ESTOQUE, USUARIO |
| **RF16** | Informar quando o estoque atingir o limite mínimo | UC05 — Registrar movimentação de estoque | Registrar movimentação de estoque | INGREDIENTE |
| **RF17** | Permitir fechamento de vendas e calcular valor da conta | UC04 — Registrar pagamento | Registrar pagamento | CONTA, PEDIDO, PAGAMENTO |
| **RF18** | Registrar pagamentos em dinheiro, cartão e Pix | UC04 — Registrar pagamento | Registrar pagamento | CONTA, PAGAMENTO |
| **RF19** | Permitir aplicação de descontos e estorno conforme permissões | — | — | CONTA, USUARIO, PERFIL, HISTORICO_ALTERACAO |
| **RF20** | Emitir comprovante não fiscal e gerar relatórios básicos | — | — | CONTA, PEDIDO, PAGAMENTO |
| **RF21** | Registrar histórico de alterações relevantes | UC03 — Atualizar status do pedido / UC05 — Registrar movimentação de estoque | Processar pedido na cozinha / Registrar movimentação de estoque | HISTORICO_ALTERACAO, USUARIO |

---

## 8.2 Relação entre Requisitos e Casos de Uso

Os cinco casos de uso especificados atendem diretamente a diversos requisitos funcionais.

| Caso de uso | Requisitos relacionados |
|---|---|
| **UC01 — Realizar autenticação** | RF01, RF02, RF03 |
| **UC02 — Registrar pedido** | RF08, RF09, RF12, RF13 |
| **UC03 — Atualizar status do pedido** | RF10, RF11, RF14, RF21 |
| **UC04 — Registrar pagamento** | RF17, RF18 |
| **UC05 — Registrar movimentação de estoque** | RF06, RF15, RF16, RF21 |

Os demais requisitos estão representados principalmente pelas entidades do banco de dados e pelas funcionalidades previstas no escopo do sistema.

---

## 8.3 Relação entre Requisitos e Diagramas de Atividades

Os diagramas de atividades detalhados anteriormente representam principalmente os processos relacionados ao ciclo do pedido.

### Diagrama — Realizar Pedido

Relaciona-se principalmente aos requisitos:

- **RF08** — Registrar pedidos vinculados a uma mesa.
- **RF09** — Permitir pedidos realizados pelo cliente.
- **RF12** — Verificar disponibilidade dos ingredientes.
- **RF13** — Reservar ingredientes após a confirmação do pedido.

O fluxo representado é:

```text
Identificar mesa
      │
      ▼
Selecionar produtos
      │
      ▼
Verificar disponibilidade
      │
      ▼
Confirmar pedido
      │
      ▼
Registrar pedido
      │
      ▼
Reservar ingredientes
      │
      ▼
Disponibilizar para cozinha
```

---

### Diagrama — Processar Pedido na Cozinha

Relaciona-se principalmente aos requisitos:

- **RF10** — Atualizar o status do pedido.
- **RF11** — Permitir visualização e atualização pela cozinha.
- **RF14** — Realizar baixa dos ingredientes quando o pedido ficar Pronto.
- **RF21** — Registrar alterações relevantes no histórico.

O fluxo representado é:

```text
Visualizar pedidos
      │
      ▼
Selecionar pedido
      │
      ▼
Em preparo
      │
      ▼
Preparar pedido
      │
      ▼
Pronto
      │
      ▼
Baixar ingredientes
      │
      ▼
Registrar histórico
```

---

## 8.4 Relação entre Requisitos e Banco de Dados

O modelo de dados foi desenvolvido para dar suporte aos requisitos funcionais definidos no projeto.

### Usuários e permissões

```text
PERFIL
   │
   ▼
USUARIO
```

Relacionados principalmente a:

- RF01
- RF02
- RF03
- RF19
- RF21

---

### Produtos e estoque

```text
PRODUTO
   │
   ▼
PRODUTO_INGREDIENTE
   ▲
   │
INGREDIENTE
```

Relacionados principalmente a:

- RF05
- RF06
- RF07
- RF12
- RF13
- RF14
- RF15
- RF16

---

### Reserva de ingredientes

```text
PEDIDO
   │
   ▼
RESERVA_INGREDIENTE
   ▲
   │
INGREDIENTE
```

Relacionada principalmente a:

- RF12
- RF13
- RF14

A tabela `RESERVA_INGREDIENTE` permite registrar quais ingredientes foram reservados para cada pedido e suas respectivas quantidades.

---

### Atendimento e pedidos

```text
MESA
 │
 ▼
CONTA
 │
 ▼
PEDIDO
 │
 ▼
ITEM_PEDIDO
 │
 ▼
PRODUTO
```

Relacionados principalmente a:

- RF08
- RF09
- RF10
- RF11
- RF17

---

### Pagamentos

```text
CONTA
  │
  ▼
PAGAMENTO
```

Relacionados principalmente a:

- RF17
- RF18
- RF19
- RF20

---

### Estoque

```text
INGREDIENTE
      │
      ▼
MOVIMENTACAO_ESTOQUE
      │
      ▼
USUARIO
```

Relacionados principalmente a:

- RF06
- RF14
- RF15
- RF16
- RF21

---

### Auditoria

```text
USUARIO
   │
   ▼
HISTORICO_ALTERACAO
```

Relacionada principalmente a:

- RF21

Também pode registrar alterações importantes relacionadas aos pedidos, estoque, descontos e estornos.

---

## 8.5 Verificação da Cobertura dos Requisitos

A matriz demonstra que os **21 requisitos funcionais** definidos no projeto possuem correspondência com pelo menos um dos modelos desenvolvidos.

Os requisitos mais importantes para o fluxo operacional do restaurante possuem rastreabilidade entre várias etapas.

Por exemplo:

```text
RF13 — Reservar ingredientes
          │
          ├── UC02 — Registrar pedido
          │
          ├── Diagrama — Realizar pedido
          │
          └── RESERVA_INGREDIENTE
```

Outro exemplo é o requisito de baixa de estoque:

```text
RF14 — Realizar baixa dos ingredientes
          │
          ├── UC03 — Atualizar status do pedido
          │
          ├── Diagrama — Processar pedido na cozinha
          │
          ├── INGREDIENTE
          │
          ├── RESERVA_INGREDIENTE
          │
          └── MOVIMENTACAO_ESTOQUE
```

Essa correspondência demonstra que os requisitos estão representados de forma consistente entre a documentação funcional, os processos e o banco de dados.

---

## Resultado

A matriz de rastreabilidade permitiu relacionar os requisitos funcionais do **Sistema de Gestão de Restaurante** com os casos de uso, diagramas de atividades e entidades do banco de dados.

Com isso, é possível verificar que as principais funcionalidades descritas nos requisitos possuem representação nos demais modelos desenvolvidos durante o projeto.

A rastreabilidade também facilita futuras alterações, pois permite identificar quais partes da documentação e do sistema podem ser afetadas quando um requisito for modificado.
