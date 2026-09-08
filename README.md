# Sistema de Gestão de Restaurante

## Equipe

* João Vitor Barros Presner
* Guilhermy Martineli Brenny
* Matheus Borges
* Mateus Henrique Presner

---

# 1ª Entrega — Documento de Visão e Requisitos

A primeira entrega tem como objetivo definir o projeto, compreender o problema que será resolvido, delimitar o escopo do sistema e especificar seus requisitos funcionais, requisitos não funcionais e regras de negócio.

## Documentação

### 1. Identificação do Projeto

[📄 Acessar Identificação](./1%C2%B0Entrega/1-Identifica%C3%A7%C3%A3o.md)

Apresenta o tema, os integrantes da equipe e uma descrição geral do sistema.

### 2. Contexto e Problema

[📄 Acessar Contexto e Problema](./1%C2%B0Entrega/2-Contexto%20e%20Problema.md)

Apresenta a situação que motivou a criação do sistema, os usuários afetados, o processo atual e a justificativa para o desenvolvimento da solução.

### 3. Objetivos

[📄 Acessar Objetivos](./1%C2%B0Entrega/3-Objetivos.md)

Apresenta o objetivo geral e os objetivos específicos que o projeto pretende alcançar.

### 4. Público-Alvo e Perfis de Usuário

[📄 Acessar Público-Alvo e Perfis de Usuário](./1%C2%B0Entrega/4-P%C3%BAblico%20Alvo%20e%20Perfis%20de%20Usu%C3%A1rio.md)

Apresenta os principais perfis de usuário, suas necessidades, responsabilidades e permissões.

### 5. Escopo

[📄 Acessar Escopo](./1%C2%B0Entrega/5-Escopo.md)

Apresenta as funcionalidades incluídas na primeira versão, os itens que não fazem parte do projeto neste momento e suas principais restrições.

### 6. Requisitos Funcionais

[📄 Acessar Requisitos Funcionais](./1%C2%B0Entrega/6-Requisitos%20Funcionais.md)

Apresenta os serviços e comportamentos que o sistema deverá oferecer, identificados pelos códigos RF01, RF02, RF03 e assim por diante.

### 7. Requisitos Não Funcionais

[📄 Acessar Requisitos Não Funcionais](./1%C2%B0Entrega/7-Requisitos%20n%C3%A3o%20funcionais.md)

Apresenta os requisitos relacionados à segurança, usabilidade, desempenho, compatibilidade, disponibilidade e responsividade do sistema.

### 8. Regras de Negócio

[📄 Acessar Regras de Negócio](./1%C2%B0Entrega/8-Regras%20de%20Neg%C3%B3cio.md)

Apresenta as condições e restrições que deverão ser respeitadas pelo sistema no contexto de gestão de um restaurante.

---

## Documento Completo

[📑 Visualizar Documento Completo da 1ª Entrega](./1%C2%B0Entrega/Sistema%20de%20Gestao%20de%20Restaurante.pdf)

---

## Estrutura da Entrega

```text
1ª Entrega/
├── 1-Identificação.md
├── 2-Contexto e Problema.md
├── 3-Objetivos.md
├── 4-Público Alvo e Perfis de Usuário.md
├── 5-Escopo.md
├── 6-Requisitos Funcionais.md
├── 7-Requisitos não funcionais.md
├── 8-Regras de Negócio.md
└── Sistema de Gestao de Restaurante.pdf
```

# 2ª Entrega — Modelagem do Software e Banco de Dados

O objetivo desta entrega é transformar os requisitos definidos anteriormente em modelos que representem o comportamento do sistema e sua estrutura de dados.

---

## Projeto

[**Sistema de Gestão de Restaurante**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante)

---

## PDF da Entrega

[📄 **Entrega 2 — Sistema de Gestão de Restaurante — PDF Final**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/%20Entrega2doSistemadeGestaodeRestaurante.pdf)

---

## 1. Revisão da Entrega 1
Acessar: [**Revisar a Entrega 1**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/1-Revisar%20a%20Entrega%201.md)

A documentação inicial foi revisada para garantir consistência entre:

- contexto e problema;
- objetivos;
- público-alvo;
- escopo;
- requisitos funcionais;
- requisitos não funcionais;
- regras de negócio.

Também foram realizados ajustes de nomenclatura e coerência para que os modelos da segunda entrega utilizem os mesmos conceitos definidos anteriormente.

---

## 2. Diagrama de Casos de Uso
Acessar: [**Construir o diagrama de casos de uso**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/2-Construir%20o%20diagrama%20de%20casos%20de%20uso.md)

O diagrama de casos de uso apresenta os principais atores do sistema e suas interações com as funcionalidades disponíveis.

Os principais atores identificados são:

- Administrador;
- Gerente;
- Atendente;
- Cozinheiro/Chef;
- Estoquista;
- Cliente.

Entre os principais casos de uso estão:

- realizar autenticação;
- registrar pedido;
- acompanhar pedido;
- atualizar status do pedido;
- registrar pagamento;
- registrar movimentação de estoque;
- consultar cardápio;
- consultar estoque;
- gerenciar usuários;
- consultar relatórios.

---

## 3. Especificação dos Casos de Uso
Acessar: [**Especificar pelo menos cinco casos de uso ou histórias de usuário**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/3-Especificar%20pelo%20menos%20cinco%20casos%20de%20uso%20ou%20hist%C3%B3rias%20de%20usu%C3%A1rio.md)

Foram especificados cinco casos de uso principais:

| Código | Caso de uso | Ator principal |
|---|---|---|
| **UC01** | Realizar autenticação | Usuário interno |
| **UC02** | Registrar pedido | Atendente ou Cliente |
| **UC03** | Atualizar status do pedido | Cozinheiro/Chef |
| **UC04** | Registrar pagamento | Atendente |
| **UC05** | Registrar movimentação de estoque | Estoquista |

Cada caso de uso possui:

- objetivo;
- ator principal;
- pré-condições;
- fluxo principal;
- fluxos alternativos;
- pós-condições;
- requisitos relacionados.

---

## 4. Diagramas de Atividades
Acessar: [**Criar pelo menos dois diagramas de atividades ou fluxos**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/4-Criar%20pelo%20menos%20dois%20diagramas%20de%20atividades%20ou%20fluxos.md)

Foram desenvolvidos dois diagramas de atividades principais.

### Realizar Pedido

Representa o fluxo desde a identificação da mesa e seleção dos produtos até a confirmação e envio do pedido para a cozinha.

O processo inclui:

- seleção da mesa;
- seleção dos produtos;
- verificação da disponibilidade dos ingredientes;
- confirmação do pedido;
- registro do pedido;
- reserva dos ingredientes;
- disponibilização para a cozinha.

### Processar Pedido na Cozinha

Representa o fluxo utilizado pela equipe da cozinha.

O processo inclui:

- visualização dos pedidos recebidos;
- seleção do pedido;
- alteração para **Em preparo**;
- preparação;
- alteração para **Pronto**;
- baixa dos ingredientes;
- registro no histórico.

---

## 5. Modelo Conceitual do Banco de Dados
Acessar: [**Construir o modelo conceitual do banco de dados**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/5-Construir%20o%20modelo%20conceitual%20do%20banco%20de%20dados.md)

O modelo conceitual representa as principais entidades e seus relacionamentos.

As principais entidades identificadas são:

- Perfil;
- Usuario;
- Cliente;
- Categoria;
- Produto;
- Ingrediente;
- ProdutoIngrediente;
- Mesa;
- Conta;
- Pedido;
- ItemPedido;
- Pagamento;
- MovimentacaoEstoque;
- HistoricoAlteracao;
- ReservaIngrediente.

A entidade **ProdutoIngrediente** representa a relação entre produtos e ingredientes.

A entidade **ReservaIngrediente** representa os ingredientes reservados para pedidos confirmados.

---

## 6. Modelo Lógico do Banco de Dados
Acessar: [**Transformar o modelo conceitual em modelo lógico**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/6-Transformar%20o%20modelo%20conceitual%20em%20modelo%20l%C3%B3gico.md)

O modelo conceitual foi transformado em um modelo lógico relacional.

As entidades foram convertidas em tabelas e foram definidas:

- chaves primárias;
- chaves estrangeiras;
- relacionamentos;
- restrições;
- tabelas associativas.

Entre as principais relações estão:

```text
PERFIL → USUARIO

CATEGORIA → PRODUTO

PRODUTO → PRODUTO_INGREDIENTE ← INGREDIENTE

MESA → CONTA → PEDIDO → ITEM_PEDIDO

CONTA → PAGAMENTO

PEDIDO → RESERVA_INGREDIENTE ← INGREDIENTE

INGREDIENTE → MOVIMENTACAO_ESTOQUE

USUARIO → HISTORICO_ALTERACAO
```

---

## 7. Dicionário de Dados
Acessar: [**Criar o dicionário de dados**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/7-Criar%20o%20dicion%C3%A1rio%20de%20dados.md)

O dicionário de dados apresenta os detalhes das tabelas e campos do banco.

Para cada campo são apresentados:

- nome;
- tipo;
- tamanho;
- obrigatoriedade;
- chave;
- descrição.

Também são apresentadas as referências entre as chaves estrangeiras.

---

## 8. Matriz de Rastreabilidade
Acessar: [**Montar a matriz de rastreabilidade**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/8-Montar%20a%20matriz%20de%20rastreabilidade.md)

A matriz de rastreabilidade relaciona os requisitos funcionais aos demais modelos desenvolvidos.

Ela permite acompanhar o caminho:

```text
Requisito
   ↓
Caso de Uso
   ↓
Processo
   ↓
Modelo de Dados
   ↓
Futura Interface
```

Dessa forma, é possível verificar se as funcionalidades definidas nos requisitos possuem representação nos demais artefatos do projeto.

---

## 9. Revisão Cruzada dos Modelos
Acessar: [**Fazer a revisão cruzada dos modelos**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/2%C2%B0Entrega%20/9-Fazer%20a%20revis%C3%A3o%20cruzada%20dos%20modelos.md)

Ao final da entrega foi realizada uma revisão cruzada para verificar a consistência entre os diferentes modelos.

Foram analisadas as seguintes questões:

- Todo caso de uso importante possui origem em um requisito funcional?
- Os nomes utilizados nos diagramas correspondem aos nomes usados no documento?
- As entidades necessárias aos processos aparecem no modelo de dados?
- As chaves estrangeiras representam corretamente os relacionamentos?
- Os fluxos respeitam as regras de negócio?
- É possível explicar o caminho requisito → caso de uso → processo → dados → futura tela?

A revisão permitiu verificar a rastreabilidade e a coerência entre os artefatos desenvolvidos.

---

## Considerações Finais

A segunda entrega consolida a passagem da etapa de levantamento de requisitos para a modelagem da solução.

Os diagramas e modelos apresentados foram elaborados para manter coerência com os requisitos funcionais, não funcionais e regras de negócio definidos na documentação inicial.

A modelagem comportamental demonstra como os diferentes perfis interagem com o sistema e como os processos principais são executados.

A modelagem estrutural apresenta as classes e entidades necessárias para representar o domínio do problema.

Por fim, os modelos conceitual e lógico, o dicionário de dados e a matriz de rastreabilidade demonstram como as funcionalidades previstas podem ser sustentadas por uma estrutura de dados relacional.

Com os ajustes e modelos apresentados, o projeto busca atender aos critérios de revisão, modelagem comportamental, modelagem de dados, rastreabilidade e legibilidade estabelecidos para a segunda entrega.

---

## Equipe

- João Vitor Barros Presner
- Guilhermy Martineli Brenny
- Matheus Borges
- Mateus Henrique Presner
