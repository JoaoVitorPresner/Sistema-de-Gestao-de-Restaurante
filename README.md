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
├── 2-Bimestre/
│   ├── Entrega-20-10/
│   │   ├── implementacao/
│   │   │   └── Aplicação Java com Spring Boot
│   │   ├── documentacao/
│   │   │   └── Documentação técnica, backlog e testes
│   │   └── evidencias/
│   │       └── Capturas de tela dos testes
│   │
│   └── Entrega-06-11/
│       ├── documentacao/
│       │   └── Documentação da segunda entrega
│       └── evidencias/
│           └── Evidências da segunda entrega
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

### Documentação

1. [📄 Atualizar a Gestão do Trabalho](./3%C2%B0Entrega/1-Atualizar%20a%20gest%C3%A3o%20do%20trabalho.md)
2. [📄 Definir Arquitetura e Tecnologias Previstas](./3%C2%B0Entrega/2-Definir%20arquitetura%20e%20tecnologias%20previstas.md)
3. [📄 Elaborar o Mapa de Navegação e os Protótipos](./3%C2%B0Entrega/3-Elaborar%20o%20mapa%20de%20navega%C3%A7%C3%A3o%20e%20os%20prot%C3%B3tipos.md)
4. [📄 Definir o Produto Mínimo Viável (MVP)](./3%C2%B0Entrega/4-Definir%20o%20Produto%20M%C3%ADnimo%20Vi%C3%A1vel%20%28MVP%29.md)
5. [📄 Verificar a Rastreabilidade](./3%C2%B0Entrega/5-Verificar%20a%20rastreabilidade.md)

### Documento completo

[📑 Visualizar PDF da 3ª Entrega](./3%C2%B0Entrega/6-3%C2%B0entrega%20Sistema%20de%20Gest%C3%A3o%20de%20Restaurante.pdf)

---
---

# 2º Bimestre — Primeira Entrega (20/10/2026)

## Implementação inicial

Nesta etapa, a equipe iniciou a implementação do Sistema de Gestão de Restaurante, priorizando o **RF05 — Gerenciamento do Cardápio**.

A entrega apresenta uma aplicação web executável, acompanhada de documentação técnica, planejamento, testes e evidências.

### Funcionalidades implementadas

- Cadastro de categorias do cardápio.
- Cadastro de produtos associados a categorias.
- Consulta de categorias e produtos cadastrados.
- Persistência dos dados no MySQL.
- Validação de campos obrigatórios e valores de preço na interface.

### Funcionalidades pendentes

As operações de edição e exclusão de produtos ainda não foram implementadas. Essas limitações estão documentadas nos casos de teste CT06 e CT07.

A implementação de 20/10 representa apenas uma etapa inicial do desenvolvimento, não a conclusão do MVP.

## Tecnologias utilizadas

| Componente | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Backend | Spring Boot 3.3.5 |
| Gerenciamento de dependências | Maven |
| Banco de dados | MySQL |
| Banco de testes | H2 em memória |
| Frontend | HTML, CSS e JavaScript |

## Como executar a implementação

### Pré-requisitos

- Java 17.
- Maven.
- MySQL instalado e em execução.
- Banco de dados e conexão configurados para a aplicação.

### Compilar

Na raiz do repositório, acessar a pasta da implementação:

```powershell
cd "2-Bimestre/Entrega-20-10/implementacao"
```

Executar:

```powershell
mvn clean package -DskipTests
```

### Iniciar o servidor

```powershell
java -jar target\restaurante-0.1.0.jar
```

Após a inicialização, acessar:

http://localhost:8080

Para encerrar o servidor, pressionar `Ctrl + C` no terminal.

### Executar os testes automatizados

Na pasta da implementação:

```powershell
mvn clean test
```

A execução registrada apresentou dois testes automatizados aprovados, sem falhas ou erros.

## Testes e evidências

Foram registrados oito casos de teste:

| Resultado | Quantidade |
|---|---:|
| Aprovados | 6 |
| Bloqueados | 2 |
| Total | 8 |

Os casos CT01 a CT05 e CT08 foram aprovados nos escopos avaliados.

Os casos CT06 e CT07 permaneceram bloqueados porque as operações de edição e exclusão ainda não estão disponíveis.

As verificações do CT08 foram realizadas na interface web, sem teste direto da API.

## Documentação da entrega

- [Documentação técnica](./2-Bimestre/Entrega-20-10/documentacao/Documentacao-Tecnica-Entrega-20-10.md)
- [Backlog e planejamento](./2-Bimestre/Entrega-20-10/documentacao/Backlog-e-Planejamento-20-10.md)
- [Testes e evidências](./2-Bimestre/Entrega-20-10/documentacao/Testes-e-Evidencias-20-10.md)
- [Código-fonte](./2-Bimestre/Entrega-20-10/implementacao/)
- [Capturas de tela dos testes](./2-Bimestre/Entrega-20-10/evidencias/)

## Identificação da entrega

- **Data:** 20/10/2026.
- **Branch:** `entrega-20-10-2026`.
- **Commit de referência dos testes:** `2f96830`.
- **Situação:** implementação inicial executável, com testes e pendências documentadas.

---

# 2º Bimestre — Segunda Entrega (06/11/2026)

## Evolução da implementação

Nesta etapa, a equipe deu continuidade ao desenvolvimento do **Sistema de Gestão de Restaurante**, ampliando as funcionalidades do **RF05 — Gerenciamento do Cardápio**.

A implementação foi desenvolvida a partir da versão apresentada em 20/10/2026, mantendo a arquitetura Java com Spring Boot, a persistência em MySQL e a interface web desenvolvida com HTML, CSS e JavaScript.

### Funcionalidades implementadas

Além das operações disponíveis na entrega anterior, foram acrescentadas:

- **Edição de produtos:** permite modificar informações de produtos cadastrados, como nome, descrição, preço e categoria.
- **Desativação lógica de produtos:** permite desativar produtos sem remover seus registros do banco de dados.
- **Identificação de produtos inativos:** a interface identifica os produtos desativados e impede novas operações de edição e desativação nesses itens.
- **Integração entre frontend e backend:** a interface utiliza requisições HTTP para atualizar e desativar produtos.
- **Persistência das alterações:** as modificações são armazenadas no MySQL e permanecem disponíveis após a atualização da página.

### Endpoints da API REST

| Método | Endpoint | Finalidade |
|---|---|---|
| GET | `/api/categorias` | Consultar categorias |
| POST | `/api/categorias` | Cadastrar categoria |
| GET | `/api/produtos` | Consultar produtos |
| POST | `/api/produtos` | Cadastrar produto |
| GET | `/api/produtos/{id}` | Consultar produto por ID |
| PUT | `/api/produtos/{id}` | Atualizar produto |
| DELETE | `/api/produtos/{id}` | Desativar produto logicamente |

A operação `DELETE` não remove fisicamente o registro do banco de dados. Ela altera o atributo `ativo` do produto para `false`.

### Tecnologias utilizadas

- Java 17;
- Spring Boot 3.3.5;
- Maven;
- MySQL;
- H2 em memória para testes automatizados;
- HTML, CSS e JavaScript;
- Git e GitHub.

### Compilação e execução

A partir da raiz do repositório, acessar:

```powershell
cd "2-Bimestre/Entrega-20-10/implementacao"
```

Executar a compilação e os testes automatizados:

```powershell
mvn clean package
```

Para iniciar a aplicação, configurar a senha do MySQL no ambiente, quando necessária:

```powershell
$env:SPRING_DATASOURCE_PASSWORD = Read-Host "Digite a senha do MySQL"
```

Em seguida, executar:

```powershell
java -jar target\restaurante-0.1.0.jar
```

Acessar a aplicação pelo navegador:

`http://localhost:8080`

As configurações de conexão também podem ser fornecidas pelas variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD`, conforme o arquivo `application.properties`.

Não devem ser publicadas senhas ou outras credenciais no repositório.

### Testes realizados

Durante a evolução do RF05, foram realizadas as seguintes verificações:

- Compilação da aplicação utilizando Maven, concluída com sucesso.
- Execução de dois testes automatizados, sem falhas ou erros.
- Cadastro e consulta de produtos pela interface web.
- Edição do preço de um produto existente.
- Verificação da persistência do novo preço após atualizar a página.
- Cadastro de um produto para teste de desativação.
- Desativação lógica do produto pela interface.
- Verificação da identificação do produto como inativo após atualizar a página.

Essas verificações confirmam o funcionamento dos fluxos avaliados, mas não representam a conclusão de todos os requisitos do sistema.

### Limitações atuais

A implementação desta entrega continua concentrada no RF05. Outros módulos previstos no projeto, como autenticação, gerenciamento de mesas, pedidos, cozinha, estoque e pagamentos, permanecem fora do escopo implementado nesta versão.

A desativação lógica de produtos também não deve ser confundida com exclusão física dos registros.

### Organização dos arquivos

O código-fonte atualizado permanece na pasta:

`2-Bimestre/Entrega-20-10/implementacao/`

A documentação e as evidências específicas desta entrega serão organizadas em:

- `2-Bimestre/Entrega-06-11/documentacao/`
- `2-Bimestre/Entrega-06-11/evidencias/`

### Identificação da entrega

- **Data:** 06/11/2026.
- **Branch:** `entrega-06-11-2026`.
- **Commit de referência da evolução do RF05:** `c952fef`.
- **Situação:** evolução funcional do RF05 implementada, compilada, testada nos fluxos descritos e enviada ao GitHub.

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

Este repositório reúne a documentação, os diagramas, os modelos de banco de dados, os protótipos e o código-fonte desenvolvido durante a evolução do projeto Sistema de Gestão de Restaurante.
