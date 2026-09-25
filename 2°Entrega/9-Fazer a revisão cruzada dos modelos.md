# 9. Revisão Cruzada dos Modelos

Nesta etapa foi realizada uma revisão cruzada dos modelos desenvolvidos na segunda entrega do **Sistema de Gestão de Restaurante**.

O objetivo da revisão é verificar se os requisitos, casos de uso, diagramas de atividades, modelos de dados e demais artefatos permanecem consistentes entre si.

---

## 9.1 Todo caso de uso importante possui origem em um requisito funcional?

Sim.

Os casos de uso especificados foram elaborados a partir dos requisitos funcionais definidos na primeira etapa do projeto.

A relação principal é apresentada abaixo:

| Caso de uso | Requisitos de origem |
|---|---|
| **UC01 — Realizar autenticação** | RF01, RF02, RF03 |
| **UC02 — Registrar pedido** | RF08, RF09, RF12, RF13 |
| **UC03 — Atualizar status do pedido** | RF10, RF11, RF14, RF21 |
| **UC04 — Registrar pagamento** | RF17, RF18 |
| **UC05 — Registrar movimentação de estoque** | RF06, RF15, RF16, RF21 |

Dessa forma, os casos de uso importantes definidos para a entrega possuem origem clara nos requisitos funcionais.

---

## 9.2 Os nomes utilizados nos diagramas correspondem aos nomes usados no documento?

Sim.

Durante a revisão, foi verificado que os nomes utilizados nos diagramas, casos de uso, requisitos e modelos seguem a mesma nomenclatura geral.

Exemplos:

- **Realizar autenticação**
- **Registrar pedido**
- **Atualizar status do pedido**
- **Registrar pagamento**
- **Registrar movimentação de estoque**
- **Produto**
- **Ingrediente**
- **Pedido**
- **Conta**
- **Mesa**
- **Pagamento**

Essa padronização evita ambiguidades e facilita a compreensão dos modelos.

Também foi mantida a correspondência entre os nomes utilizados no modelo conceitual, modelo lógico e dicionário de dados.

Por exemplo:

```text
Modelo conceitual: ReservaIngrediente

Modelo lógico: RESERVA_INGREDIENTE
```

A diferença ocorre apenas por convenção de nomenclatura entre o modelo conceitual e o modelo lógico.

---

## 9.3 As entidades necessárias aos processos aparecem no modelo de dados?

Sim.

As entidades necessárias para representar os processos principais estão presentes no modelo de dados.

No processo **Realizar pedido**, são utilizadas principalmente:

- MESA
- CONTA
- PEDIDO
- ITEM_PEDIDO
- PRODUTO
- INGREDIENTE
- PRODUTO_INGREDIENTE
- RESERVA_INGREDIENTE

No processo **Processar pedido na cozinha**, são utilizadas:

- PEDIDO
- ITEM_PEDIDO
- INGREDIENTE
- RESERVA_INGREDIENTE
- MOVIMENTACAO_ESTOQUE
- HISTORICO_ALTERACAO

No processo de pagamento são utilizadas:

- CONTA
- PEDIDO
- PAGAMENTO

No controle de estoque são utilizadas:

- INGREDIENTE
- MOVIMENTACAO_ESTOQUE
- USUARIO

Dessa forma, os processos descritos possuem suporte correspondente no modelo de dados.

---

## 9.4 As chaves estrangeiras representam corretamente os relacionamentos?

Sim.

As chaves estrangeiras foram definidas de acordo com os relacionamentos identificados no modelo conceitual.

Alguns exemplos são:

| Tabela | Chave estrangeira | Referência |
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

Essas chaves permitem representar corretamente os relacionamentos de um-para-muitos e os relacionamentos muitos-para-muitos transformados por tabelas associativas.

---

## 9.5 Os fluxos respeitam as regras de negócio?

Sim.

Os diagramas de atividades e os casos de uso foram revisados para garantir que os fluxos estejam de acordo com as regras de negócio definidas.

Alguns exemplos são:

### Confirmação de pedido

Antes da confirmação, o sistema verifica se existem ingredientes suficientes.

Isso atende às regras:

- **RN08** — O estoque físico não pode ficar abaixo de zero.
- **RN09** — A disponibilidade dos ingredientes deve ser verificada antes da confirmação.

---

### Reserva de ingredientes

Após a confirmação do pedido, os ingredientes necessários são reservados.

Isso atende à:

- **RN10** — Os ingredientes necessários devem ser reservados quando o pedido for confirmado.

---

### Cancelamento antes do preparo

Quando um pedido é cancelado antes do início do preparo, os ingredientes reservados devem ser liberados.

Isso atende à:

- **RN12** — As reservas devem ser liberadas quando o pedido for cancelado antes do preparo.

---

### Finalização do preparo

Quando o pedido atinge o status **Pronto**, o sistema realiza a baixa dos ingredientes utilizados.

Isso atende à:

- **RN11** — Os ingredientes utilizados devem ser baixados do estoque quando o pedido atingir o status Pronto.

---

### Pagamento da conta

A conta somente pode ser encerrada quando os pedidos estiverem finalizados e o valor total estiver pago.

Isso atende às regras:

- **RN06** — A conta somente pode ser encerrada quando todos os pedidos estiverem Entregues ou Cancelados e o pagamento estiver concluído.
- **RN07** — É possível utilizar mais de uma modalidade de pagamento, desde que a soma corresponda ao valor devido.

---

## 9.6 É possível explicar o caminho requisito → caso de uso → processo → dados → futura tela?

Sim.

A documentação permite acompanhar uma funcionalidade desde o requisito inicial até sua futura implementação em uma tela do sistema.

### Exemplo 1 — Registrar pedido

```text
RF08, RF09, RF12 e RF13
          │
          ▼
UC02 — Registrar pedido
          │
          ▼
Diagrama de Atividades — Realizar pedido
          │
          ▼
MESA
CONTA
PEDIDO
ITEM_PEDIDO
PRODUTO
INGREDIENTE
PRODUTO_INGREDIENTE
RESERVA_INGREDIENTE
          │
          ▼
Futura tela de realização de pedidos
```

Nesse exemplo, os requisitos definem o que o sistema deve fazer.

O caso de uso detalha a interação do Atendente ou Cliente com o sistema.

O diagrama de atividades representa a sequência do processo.

O modelo de dados define onde as informações necessárias serão armazenadas.

Por fim, essas informações poderão ser utilizadas na construção da futura interface de pedidos.

---

### Exemplo 2 — Processar pedido na cozinha

```text
RF10, RF11, RF14 e RF21
          │
          ▼
UC03 — Atualizar status do pedido
          │
          ▼
Diagrama de Atividades — Processar pedido na cozinha
          │
          ▼
PEDIDO
ITEM_PEDIDO
INGREDIENTE
RESERVA_INGREDIENTE
MOVIMENTACAO_ESTOQUE
HISTORICO_ALTERACAO
          │
          ▼
Futura tela da cozinha
```

A futura tela poderá exibir os pedidos recebidos e permitir que a equipe da cozinha altere seus status durante a preparação.

---

### Exemplo 3 — Registrar pagamento

```text
RF17 e RF18
      │
      ▼
UC04 — Registrar pagamento
      │
      ▼
Processo de fechamento da conta
      │
      ▼
CONTA
PEDIDO
PAGAMENTO
      │
      ▼
Futura tela de pagamento
```

A tela poderá apresentar o valor da conta e permitir o registro de pagamentos em dinheiro, cartão, Pix ou em mais de uma modalidade.

---

## 9.7 Resultado da Revisão Cruzada

Após a revisão dos modelos, foi possível verificar que:

- os principais casos de uso possuem origem nos requisitos funcionais;
- a nomenclatura utilizada permanece consistente entre os documentos;
- as entidades necessárias aos processos estão representadas no modelo de dados;
- as chaves estrangeiras representam os relacionamentos definidos no modelo conceitual;
- os fluxos de atividades respeitam as regras de negócio;
- existe rastreabilidade entre requisitos, casos de uso, processos, dados e futuras interfaces.

A revisão também permitiu corrigir e melhorar alguns pontos da modelagem, principalmente a representação da reserva de ingredientes por meio da entidade **ReservaIngrediente / RESERVA_INGREDIENTE**.

---

# Considerações Finais

A segunda entrega consolida a passagem da etapa de levantamento de requisitos para a modelagem da solução. Os diagramas e modelos apresentados foram elaborados para manter coerência com os requisitos funcionais, não funcionais e regras de negócio definidos na documentação inicial.

A modelagem comportamental demonstra como os diferentes perfis interagem com o sistema e como os processos principais são executados. A modelagem estrutural apresenta as classes e entidades necessárias para representar o domínio do problema.

Por fim, os modelos conceitual e lógico, o dicionário de dados e a matriz de rastreabilidade demonstram como as funcionalidades previstas podem ser sustentadas por uma estrutura de dados relacional.

Com os ajustes e modelos apresentados, o documento busca atender aos critérios de revisão, modelagem comportamental, modelagem de dados, rastreabilidade e legibilidade estabelecidos para a segunda entrega.
