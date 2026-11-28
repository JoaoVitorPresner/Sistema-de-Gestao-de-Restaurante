# Sistema de Gestão de Restaurante
## Documentação técnica — segunda entrega do 2º bimestre

**Disciplina:** Fábrica de Software  
**Professora:** Sabine Cassol  
**Data de referência:** 06/11/2026  
**Responsável pela revisão técnica:** Guilherme20028  
**Equipe:** Guilherme20028, Matheusborgges, MateusPresner e JoaoVitorPresner  
**Repositório:** https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante  
**Branch:** `entrega-06-11-2026`  
**Commits de referência:** `c952fef` e `e2f8c3f`

> **Escopo:** atualização incremental da documentação técnica de 20/10/2026. Os documentos do primeiro bimestre continuam válidos como referência. Esta versão registra as funcionalidades implementadas, os testes realizados e as pendências identificadas até 06/11.

## 1. Contexto, problema e objetivos

O projeto propõe uma aplicação web para apoiar a gestão de restaurantes e lanchonetes, centralizando atividades como cardápio, mesas, pedidos, cozinha, estoque, vendas e pagamentos.

O problema identificado no primeiro bimestre envolve a fragmentação de informações e processos, que pode provocar erros de atendimento, atrasos, dificuldades de controle e falhas de comunicação.

O objetivo geral permanece desenvolver um sistema integrado de apoio às operações do estabelecimento.

Na segunda entrega do segundo bimestre, o desenvolvimento concentrou-se na evolução do **RF05 — Cardápio**, especialmente nas operações de edição e desativação de produtos.

A implementação atual representa uma evolução funcional do projeto, mas não corresponde à conclusão de todos os módulos previstos.

## 2. Documentos do primeiro bimestre considerados

Foram mantidos como referência:

- **1ª entrega — Visão e requisitos:** problema, objetivos, público-alvo, escopo, requisitos funcionais e não funcionais e regras de negócio.
- **2ª entrega — Modelagem:** casos de uso, atividades, modelos conceitual e lógico do banco de dados, dicionário de dados e matriz de rastreabilidade.
- **3ª entrega — Planejamento e arquitetura:** tecnologias previstas, diagrama de arquitetura, protótipos, MVP e rastreabilidade.

Também foram considerados os registros do segundo bimestre:

- `2-Bimestre/Entrega-20-10/documentacao/Documentacao-Tecnica-Entrega-20-10.md`
- `2-Bimestre/Entrega-06-11/documentacao/Testes-e-Evidencias-06-11.md`
- `README.md`

## 3. Revisão dos requisitos e evolução do RF05

O **RF05 — Cardápio** estabelece que o sistema deve permitir cadastro, consulta, alteração e exclusão de produtos e categorias.

Na entrega de 20/10, foram demonstrados cadastro, consulta e persistência de registros.

Na entrega de 06/11, foram implementadas e testadas novas operações relacionadas aos produtos.

### 3.1 Situação das operações

| Operação | Situação em 06/11 |
|---|---|
| Cadastrar categoria | Demonstrada na primeira entrega |
| Consultar categoria | Demonstrada na primeira entrega |
| Cadastrar produto | Demonstrada na primeira entrega |
| Consultar produto | Demonstrada na primeira entrega |
| Persistir registros no MySQL | Demonstrada |
| Editar produto | Implementada e aprovada no CT06 |
| Desativar produto | Implementada e aprovada no CT07 |
| Editar categoria | Não comprovada nesta entrega |
| Excluir ou desativar categoria | Não comprovada nesta entrega |
| Excluir fisicamente produto | Não implementada como comportamento demonstrado |

### 3.2 Edição de produtos

Foi implementada a possibilidade de modificar os dados de um produto existente.

A interface apresenta uma opção de edição, permitindo alterar as informações e enviar a atualização ao backend.

A API utiliza o método HTTP `PUT`, identificando o produto pelo seu ID.

O teste manual CT06 confirmou que as alterações foram aplicadas e exibidas na interface.

### 3.3 Desativação lógica de produtos

A operação de exclusão de produto foi implementada como **desativação lógica (soft delete)**.

Nesse comportamento, o produto não é removido fisicamente do banco de dados. Seu atributo `ativo` passa a receber o valor `false`.

A interface identifica o produto como inativo, preservando seu registro.

O teste CT07 confirmou a desativação e a permanência desse estado após atualização da página.

**Limitação:** o comportamento demonstrado não corresponde à exclusão física. A equipe ainda deve confirmar se a desativação lógica atende integralmente ao requisito original.

### 3.4 Relação com outros requisitos

O **RF07** prevê associar ingredientes aos produtos e definir quantidades de preparação.

A **RN13** estabelece a necessidade de composição para produtos que utilizam ingredientes controlados pelo estoque.

Essas integrações permanecem no escopo global, mas não foram comprovadas na implementação de 06/11.

## 4. Decisões técnicas confirmadas

| Componente | Tecnologia | Situação |
|---|---|---|
| Interface | HTML, CSS e JavaScript | Implementada para o recorte RF05 |
| Backend | Java 17 e Spring Boot 3.3.5 | Executado |
| Persistência | MySQL | Funcionamento demonstrado |
| Mapeamento objeto-relacional | Spring Data JPA / Hibernate | Utilizado |
| Dependências e compilação | Maven | Build concluído |
| Testes automatizados | JUnit, Spring Boot Test, Mockito e H2 | Dois testes aprovados |
| Controle de versão | Git e GitHub | Branch de 06/11 sincronizada |
| Protótipos | Figma | Referência do primeiro bimestre |
| Hospedagem | A definir | Execução local |

O Java 17 continua sendo a versão recomendada para o projeto.

## 5. Arquitetura da implementação

```text
[Usuário / Navegador]
          |
          | HTTP
          v
[Interface HTML / CSS / JavaScript]
          |
          | Requisições à API REST
          v
[Spring Boot 3.3.5 / Java 17]
          |
          |-- ApiController.java
          |
          |-- Categoria.java
          |-- Produto.java
          |
          |-- CategoriaRepository.java
          |-- ProdutoRepository.java
          |
          | Spring Data JPA / Hibernate
          v
[MySQL — banco restaurante]
```

### 5.1 Fluxo de edição

1. O usuário seleciona a opção de editar um produto.
2. A interface apresenta os dados do produto.
3. O usuário modifica as informações.
4. A interface envia uma requisição HTTP `PUT`.
5. O backend processa a alteração.
6. O repositório JPA persiste os novos valores no MySQL.
7. A interface apresenta o produto atualizado.

### 5.2 Fluxo de desativação

1. O usuário seleciona a opção de desativar o produto.
2. A interface envia uma requisição HTTP `DELETE`.
3. O backend identifica o produto.
4. O atributo `ativo` é alterado para `false`.
5. O registro permanece no banco de dados.
6. A interface apresenta o estado inativo.

## 6. Organização das pastas e arquivos

```text
2-Bimestre/
├── Entrega-20-10/
│   ├── implementacao/
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/br/com/restaurante/
│   │       │   │   ├── RestauranteApplication.java
│   │       │   │   ├── ApiController.java
│   │       │   │   ├── Categoria.java
│   │       │   │   ├── CategoriaRepository.java
│   │       │   │   ├── Produto.java
│   │       │   │   └── ProdutoRepository.java
│   │       │   └── resources/
│   │       │       ├── application.properties
│   │       │       └── static/index.html
│   │       └── test/
│   └── documentacao/
└── Entrega-06-11/
    ├── documentacao/
    │   ├── Testes-e-Evidencias-06-11.md
    │   ├── Documentacao-Tecnica-Entrega-06-11.md
    │   └── Backlog-e-Planejamento-06-11.md
    └── evidencias/
        ├── EV06-Edicao-Produto.png
        ├── EV06-Produto-Atualizado.png
        ├── EV07-Produto-Desativado.png
        └── EV07-Persistencia-Desativacao.png
```

Os dois novos documentos de planejamento e documentação técnica ainda precisam ser adicionados ao repositório.

## 7. Configuração do ambiente e execução

**Pré-requisitos:**

- Java JDK 17.
- Maven.
- MySQL instalado e em funcionamento.
- Código-fonte do repositório.

A implementação permanece no diretório:

`2-Bimestre/Entrega-20-10/implementacao/`

Para compilar e executar os testes:

```powershell
mvn clean package
```

Para configurar a senha conforme a variável prevista no `application.properties`:

```powershell
$env:DB_PASSWORD = Read-Host "Digite a senha do MySQL"
```

Para executar a aplicação:

```powershell
java -jar target\restaurante-0.1.0.jar
```

A aplicação utiliza por padrão a porta `8080`.

As credenciais reais do banco não devem ser publicadas no GitHub.

## 8. Endpoints REST do RF05

| Método | Endpoint | Finalidade |
|---|---|---|
| GET | `/api/categorias` | Consultar categorias |
| POST | `/api/categorias` | Cadastrar categoria |
| GET | `/api/produtos` | Consultar produtos |
| GET | `/api/produtos/{id}` | Consultar produto específico |
| POST | `/api/produtos` | Cadastrar produto |
| PUT | `/api/produtos/{id}` | Atualizar produto |
| DELETE | `/api/produtos/{id}` | Desativar logicamente produto |

O método `DELETE` não realiza exclusão física do produto.

## 9. Testes e evidências técnicas

### 9.1 Testes automatizados

Foi executado o comando:

```powershell
mvn clean package
```

Resultado registrado:

```text
Tests run: 2, Failures: 0, Errors: 0
BUILD SUCCESS
```

Os testes automatizados conhecidos são:

- `ApiControllerTest.cadastraCategoriaEConsulta`
- `ApiControllerTest.rejeitaProdutoSemPreco`

### 9.2 Testes manuais

| Caso | Objetivo | Resultado |
|---|---|---|
| CT06 | Editar produto e verificar atualização | Aprovado |
| CT07 | Desativar produto e verificar persistência | Aprovado para desativação lógica |

**Evidências do CT06:**

- `EV06-Edicao-Produto.png`
- `EV06-Produto-Atualizado.png`

**Evidências do CT07:**

- `EV07-Produto-Desativado.png`
- `EV07-Persistencia-Desativacao.png`

Os resultados detalhados estão em:

`2-Bimestre/Entrega-06-11/documentacao/Testes-e-Evidencias-06-11.md`

## 10. Registro de alterações em relação à entrega anterior

| Data | Item | Alteração |
|---|---|---|
| 20/10 | RF05 | Implementação inicial de cadastro, consulta e persistência |
| 06/11 | RF05 | Implementação da edição de produtos |
| 06/11 | RF05 | Implementação da desativação lógica |
| 06/11 | Testes | Registro dos casos CT06 e CT07 |
| 06/11 | Documentação | Atualização do README e inclusão de evidências |
| 06/11 | GitHub | Envio dos commits `c952fef` e `e2f8c3f` |

A branch `entrega-06-11-2026` foi enviada ao repositório remoto e sua sincronização foi confirmada.

A integração à branch `main` não foi confirmada.

## 11. Rastreabilidade e pendências

| Requisito | Implementação / verificação | Situação |
|---|---|---|
| RF05 — Cadastro de categorias | API, interface e testes anteriores | Demonstrado |
| RF05 — Cadastro de produtos | API, interface e testes anteriores | Demonstrado |
| RF05 — Edição de produtos | API, interface e CT06 | Aprovado |
| RF05 — Desativação de produtos | API, interface e CT07 | Aprovado como soft delete |
| RF05 — Alteração de categorias | Não verificada | Pendente |
| RF05 — Exclusão de categorias | Não verificada | Pendente |
| RF05 — Exclusão física de produtos | Não corresponde ao comportamento atual | Pendente de decisão |
| RF07 / RN13 | Integração de ingredientes e estoque | Não comprovada |

**Próximos passos:**

1. Confirmar a aceitação da desativação lógica como comportamento de exclusão.
2. Revisar as operações pendentes de categorias.
3. Ampliar os testes de validação e tratamento de erros.
4. Conferir a consistência entre requisitos, modelos e implementação.
5. Planejar a evolução dos demais requisitos até 27/11.
6. Preparar a demonstração final com participação da equipe.

## 12. Conclusão

A segunda entrega do segundo bimestre demonstra evolução funcional do Sistema de Gestão de Restaurante.

O RF05 passou a oferecer, além das operações básicas já demonstradas, edição e desativação lógica de produtos.

A implementação foi acompanhada por testes automatizados aprovados, testes manuais documentados e evidências visuais.

Entretanto, **o RF05 não deve ser considerado integralmente concluído**, pois ainda existem operações pendentes de verificação e decisões de negócio a confirmar.

O desenvolvimento continuará até a entrega final prevista para 27/11/2026.

---

**Revisão antes do envio:** Guilherme20028 deve validar a documentação técnica; MateusPresner deve conferir os registros de testes; Matheusborgges deve alinhar as pendências ao backlog; JoaoVitorPresner deve integrar os documentos ao repositório.