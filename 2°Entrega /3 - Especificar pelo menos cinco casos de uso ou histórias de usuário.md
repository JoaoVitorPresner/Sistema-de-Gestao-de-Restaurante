# 3. Especificação dos Casos de Uso

Nesta etapa foram detalhados cinco casos de uso principais do **Sistema de Gestão de Restaurante**.

Cada caso de uso apresenta o ator responsável, objetivo, pré-condições, fluxo principal, fluxos alternativos, pós-condições e os requisitos relacionados.

---

## UC01 — Realizar autenticação

**Ator principal:** Usuário interno

**Objetivo:** Permitir o acesso ao sistema conforme o perfil do usuário.

**Pré-condições:**

- O usuário deve estar cadastrado no sistema.
- O usuário deve estar ativo.

### Fluxo principal

1. O usuário acessa a tela de login.
2. O usuário informa login e senha.
3. O sistema valida as credenciais informadas.
4. O sistema identifica o perfil associado ao usuário.
5. O sistema libera o acesso às funcionalidades permitidas para o perfil.

### Fluxo alternativo

**Credenciais inválidas:**

1. O sistema informa que o login ou a senha estão incorretos.
2. O sistema permite que o usuário realize uma nova tentativa.

### Pós-condições

- O usuário permanece autenticado no sistema.
- As permissões correspondentes ao seu perfil são carregadas.

### Requisitos relacionados

- RF01
- RF02
- RF03

---

## UC02 — Registrar pedido

**Ator principal:** Atendente ou Cliente, conforme a origem do pedido.

**Objetivo:** Registrar um pedido vinculado a uma mesa e encaminhá-lo para a cozinha.

**Pré-condições:**

- A mesa deve estar ativa para atendimento.
- Os produtos devem estar cadastrados e disponíveis no cardápio.

### Fluxo principal

1. O sistema identifica a mesa associada ao pedido ou o atendente seleciona a mesa.
2. O ator seleciona os produtos desejados.
3. O sistema verifica a disponibilidade dos ingredientes necessários.
4. O ator confirma o pedido.
5. O sistema registra o pedido com status **Recebido**.
6. O sistema reserva os ingredientes necessários para o preparo.
7. O sistema disponibiliza o pedido para a cozinha.

### Fluxo alternativo

**Ingredientes insuficientes:**

1. O sistema identifica que não existem ingredientes suficientes para um ou mais produtos.
2. O sistema informa a indisponibilidade.
3. O pedido não é confirmado enquanto a situação não for corrigida.

### Pós-condições

- O pedido é registrado e vinculado à mesa.
- O pedido permanece com status **Recebido**.
- Os ingredientes necessários ficam reservados.
- O pedido fica disponível para visualização pela cozinha.

### Requisitos relacionados

- RF08
- RF09
- RF12
- RF13

---

## UC03 — Atualizar status do pedido

**Ator principal:** Cozinheiro/Chef

**Objetivo:** Permitir que a equipe da cozinha acompanhe o preparo dos pedidos e atualize seus respectivos status.

**Pré-condições:**

- O usuário deve estar autenticado.
- Deve existir pelo menos um pedido disponível para a cozinha.

### Fluxo principal

1. O Cozinheiro/Chef visualiza os pedidos recebidos.
2. Seleciona um pedido.
3. O sistema apresenta os dados e itens do pedido.
4. O Cozinheiro/Chef altera o status para **Em preparo**.
5. O pedido é preparado.
6. Ao finalizar o preparo, o Cozinheiro/Chef altera o status para **Pronto**.
7. O sistema realiza a baixa dos ingredientes utilizados no estoque.
8. O sistema registra a alteração de status no histórico.

### Fluxo alternativo

**Cancelamento excepcional:**

1. Caso seja solicitado o cancelamento de um pedido já enviado à cozinha, o sistema verifica as permissões do usuário responsável pela operação.
2. Se o usuário possuir permissão, o cancelamento pode ser realizado.
3. O sistema registra a alteração no histórico.

### Pós-condições

- O status do pedido é atualizado.
- Quando o pedido atingir o status **Pronto**, os ingredientes utilizados são baixados do estoque.
- A alteração realizada fica registrada no histórico.

### Requisitos relacionados

- RF10
- RF11
- RF14
- RF21

---

## UC04 — Registrar pagamento

**Ator principal:** Atendente

**Objetivo:** Registrar o pagamento de uma conta utilizando uma ou mais modalidades.

**Pré-condições:**

- Deve existir uma conta aberta.
- Todos os pedidos vinculados à conta devem estar com status **Entregue** ou **Cancelado** para que a conta possa ser encerrada.

### Fluxo principal

1. O atendente seleciona a conta.
2. O sistema apresenta o valor total devido.
3. O atendente seleciona uma modalidade de pagamento.
4. O atendente informa o valor pago.
5. O sistema registra o pagamento.
6. Caso ainda exista valor pendente, o atendente pode registrar outra modalidade de pagamento.
7. O sistema soma os pagamentos registrados.
8. Quando o valor pago corresponder ao valor total devido, o sistema encerra a conta.

### Fluxos alternativos

**Pagamento parcial:**

1. Se o valor pago for inferior ao valor total devido, o sistema registra o pagamento.
2. A conta permanece aberta até que o valor total seja quitado.

**Valor de pagamento inválido:**

1. Caso o valor informado seja inválido, o sistema não conclui a operação.
2. O sistema informa o erro ao atendente.

### Pós-condições

- O pagamento é registrado.
- Se o valor total for quitado, a conta é encerrada.
- Caso exista saldo pendente, a conta permanece aberta.

### Requisitos relacionados

- RF17
- RF18

### Regras de negócio relacionadas

- RN06
- RN07

---

## UC05 — Registrar movimentação de estoque

**Ator principal:** Estoquista

**Objetivo:** Registrar movimentações de entrada ou saída de ingredientes e atualizar as quantidades disponíveis no estoque.

**Pré-condições:**

- O estoquista deve estar autenticado.
- O ingrediente deve estar cadastrado.

### Fluxo principal

1. O estoquista acessa a área de estoque.
2. Seleciona o ingrediente.
3. Informa o tipo de movimentação.
4. Informa a quantidade movimentada.
5. O sistema valida a operação.
6. O sistema registra a movimentação.
7. O sistema atualiza a quantidade do ingrediente no estoque.
8. O sistema verifica se a quantidade disponível atingiu ou ficou abaixo do limite mínimo.
9. Caso necessário, o sistema informa um alerta de estoque baixo.

### Fluxos alternativos

**Estoque insuficiente para saída:**

1. O sistema verifica que a movimentação resultaria em uma quantidade física inferior a zero.
2. O sistema bloqueia a operação.
3. O sistema informa que não existe quantidade suficiente disponível.

**Dados inválidos:**

1. Caso a quantidade informada seja inválida, o sistema não registra a movimentação.
2. O sistema solicita a correção dos dados.

### Pós-condições

- A movimentação fica registrada.
- A quantidade do ingrediente é atualizada.
- O usuário responsável pela movimentação fica associado ao registro.
- Quando aplicável, o sistema apresenta alerta de estoque mínimo.

### Requisitos relacionados

- RF06
- RF15
- RF16
- RF21

---

## Resumo dos Casos de Uso

| Código | Caso de uso | Ator principal | Principais requisitos |
|---|---|---|---|
| **UC01** | Realizar autenticação | Usuário interno | RF01, RF02, RF03 |
| **UC02** | Registrar pedido | Atendente ou Cliente | RF08, RF09, RF12, RF13 |
| **UC03** | Atualizar status do pedido | Cozinheiro/Chef | RF10, RF11, RF14, RF21 |
| **UC04** | Registrar pagamento | Atendente | RF17, RF18 |
| **UC05** | Registrar movimentação de estoque | Estoquista | RF06, RF15, RF16, RF21 |

---

## Resultado

A especificação dos casos de uso detalha o comportamento esperado das principais funcionalidades do sistema.

Esses casos de uso servem como base para a construção dos diagramas de atividades, definição das entidades do sistema e desenvolvimento da matriz de rastreabilidade.
