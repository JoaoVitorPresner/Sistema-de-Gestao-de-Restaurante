# Sistema de Gestão de Restaurante
## Documentação técnica — primeira entrega do 2º bimestre

**Disciplina:** Fábrica de Software  
**Professora:** Sabine Cassol  
**Data de referência:** 20/10/2026  
**Responsável pela revisão técnica:** Guilherme20028  
**Equipe:** Guilherme20028, Matheusborgges, MateusPresner e JoaoVitorPresner  
**Repositório:** https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante  
**Branch de trabalho:** `entrega-20-10-2026`  
**Commit inicial da implementação:** `d45857a`

> **Escopo deste registro:** atualização incremental da documentação técnica. Os documentos das entregas do primeiro bimestre permanecem como referência; este arquivo registra decisões confirmadas, o recorte implementado, evidências disponíveis e pontos ainda não verificados.

## 1. Contexto, problema e objetivos

O projeto propõe uma aplicação web para apoiar a gestão de restaurantes e lanchonetes, centralizando atividades como cardápio, mesas, pedidos, cozinha, estoque, vendas e pagamentos. O problema identificado no primeiro bimestre é a fragmentação de informações e processos, que pode provocar erros de atendimento, atrasos, dificuldades de controle e falhas de comunicação. O objetivo geral permanece desenvolver um sistema integrado de apoio às operações do estabelecimento.

**Decisão da revisão:** manter o problema, o objetivo geral e o escopo global definidos no primeiro bimestre. A implementação de 20/10 representa uma etapa do projeto, não uma redefinição do produto final.

## 2. Documentos do primeiro bimestre considerados

- **1ª entrega — Visão e requisitos:** problema, objetivos, público-alvo, escopo, requisitos funcionais e não funcionais e regras de negócio.
- **2ª entrega — Modelagem:** casos de uso, atividades, modelos conceitual e lógico do banco de dados, dicionário de dados e matriz de rastreabilidade.
- **3ª entrega — Planejamento e arquitetura:** tecnologias previstas, diagrama de arquitetura, protótipos, MVP e rastreabilidade.

Referências diretas:
- [Requisitos funcionais](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/1%C2%B0Entrega/6-Requisitos%20Funcionais.md)
- [Regras de negócio](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/1%C2%B0Entrega/8-Regras%20de%20Neg%C3%B3cio.md)
- [Arquitetura e tecnologias previstas](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/3%C2%B0Entrega/2-Definir%20arquitetura%20e%20tecnologias%20previstas.md)
- [Rastreabilidade da terceira entrega](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante/blob/main/3%C2%B0Entrega/5-Verificar%20a%20rastreabilidade.md)
- [README do projeto](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante)

## 3. Revisão dos requisitos e recorte escolhido

O **RF05 — Cardápio** estabelece que o sistema deve permitir **cadastro, consulta, alteração e exclusão de produtos e categorias do cardápio**, com prioridade **alta** na documentação original. Esse requisito foi escolhido como recorte demonstrável da primeira entrega do segundo bimestre por permitir uma interação real com o usuário e armazenamento persistente, sem depender da conclusão do fluxo completo de pedidos e pagamentos.

### 3.1 Situação das operações do RF05

| Operação | Situação em 20/10 | Base da informação |
|---|---|---|
| Cadastrar categoria | Demonstrada manualmente | Categoria fictícia “Bebidas” |
| Consultar categoria | Demonstrada manualmente | Categoria exibida na interface |
| Cadastrar produto | Demonstrada manualmente | Produto fictício “Suco de Laranja” |
| Consultar produto | Demonstrada manualmente | Produto exibido na interface |
| Persistir registros | Demonstrada manualmente | Registros mantidos após reinicialização do servidor |
| Alterar categoria ou produto | **Não verificada nesta revisão** | Exige teste e registro próprios |
| Excluir categoria ou produto | **Não verificada nesta revisão** | Exige teste e registro próprios |

**Importante:** o RF05 não deve ser marcado como totalmente validado enquanto todas as operações previstas não forem verificadas. A documentação do primeiro bimestre não deve ser reduzida para coincidir com o recorte inicial.

### 3.2 Relação com outros requisitos e regras

O cardápio constitui uma base para os fluxos posteriores de pedidos e para a associação entre produtos e ingredientes. O **RF07** prevê associar ingredientes aos produtos e definir quantidades de preparação; a **RN13** estabelece a necessidade de composição para produtos que utilizem ingredientes controlados pelo estoque. Essas integrações pertencem ao escopo global e **não foram comprovadas** na implementação inicial de 20/10.

Não há justificativa para alterar os identificadores RF05, RF07 ou RN13. A equipe deverá apenas complementar sua rastreabilidade com o estado real de implementação e teste.

## 4. Decisões técnicas confirmadas

| Componente | Tecnologia / recurso | Função e justificativa | Estado |
|---|---|---|---|
| Interface | HTML, CSS e JavaScript | Permitir acesso por navegador e interação com o cardápio | Implementação inicial disponível em `static/index.html` |
| Backend | Java 17, Spring Boot 3.3.5 | Organizar endpoints HTTP, validações e acesso aos dados | Executado e testado |
| Persistência | MySQL | Armazenar produtos e categorias em banco relacional | Conexão e persistência demonstradas |
| Mapeamento objeto-relacional | Spring Data JPA / Hibernate | Mapear entidades e repositórios de dados | Presente na implementação |
| Gerenciamento de dependências | Maven | Compilar, executar testes e empacotar o projeto | Comandos executados com sucesso para testes |
| Testes automatizados | JUnit / Spring Boot Test, Mockito e H2 | Verificar comportamentos em ambiente isolado de teste | Dois testes aprovados com Java 17 |
| Controle de versão | Git e GitHub | Histórico, colaboração e integração por Pull Request | Branch enviada; integração à `main` pendente |
| Protótipos | Figma | Referência para telas planejadas | Documentado no primeiro bimestre; aderência detalhada do recorte deve ser revisada |
| Hospedagem | A definir | Implantação fora do ambiente local | Ainda não definida |

O **Java 26** foi utilizado inicialmente no ambiente local, mas os testes automatizados apresentaram problema de inicialização do Mockito/Byte Buddy. A execução dos testes foi repetida com **Java 17**, resultando em dois testes aprovados. Por isso, Java 17 é a referência de execução recomendada para esta entrega.

## 5. Arquitetura da implementação inicial

```text
[Usuário / navegador]
         |
         | HTTP — interface e requisições
         v
[Aplicação Java 17 / Spring Boot 3.3.5]
         |-- Interface estática: src/main/resources/static/index.html
         |-- Controlador HTTP: ApiController.java
         |-- Entidades: Categoria.java / Produto.java
         |-- Repositórios JPA: CategoriaRepository.java / ProdutoRepository.java
         |
         | JDBC / JPA / Hibernate
         v
[MySQL — banco restaurante]
```

**Fluxo:** o usuário acessa a aplicação pelo navegador; a interface envia operações ao backend; o backend valida/processa a requisição e utiliza os repositórios para gravar ou consultar os registros no MySQL; o resultado retorna à interface.

O diagrama descreve apenas componentes identificados na implementação inicial. Autenticação, cozinha, pagamentos e estoque permanecem no planejamento global, sem serem apresentados como componentes já operacionais neste recorte.

## 6. Organização das pastas e arquivos

```text
2-Bimestre/
└── Entrega-20-10/
    ├── implementacao/
    │   ├── pom.xml
    │   └── src/
    │       ├── main/
    │       │   ├── java/br/com/restaurante/
    │       │   │   ├── RestauranteApplication.java
    │       │   │   ├── ApiController.java
    │       │   │   ├── Categoria.java
    │       │   │   ├── CategoriaRepository.java
    │       │   │   ├── Produto.java
    │       │   │   └── ProdutoRepository.java
    │       │   └── resources/
    │       │       ├── application.properties
    │       │       └── static/index.html
    │       └── test/
    │           ├── java/br/com/restaurante/ApiControllerTest.java
    │           └── resources/application.properties
    └── documentacao/
        └── Documentacao-Tecnica-Entrega-20-10.md
```

A pasta `documentacao` é o destino proposto para este documento; deve ser criada no repositório antes de sua inclusão. A pasta `target/` é gerada pelo Maven e não deve ser versionada.

## 7. Configuração de ambiente e execução

**Pré-requisitos confirmados no ambiente utilizado:** JDK 17, Maven, MySQL e banco `restaurante`. As credenciais devem ser configuradas localmente, sem publicação no GitHub.

Variáveis utilizadas pela aplicação:

| Variável | Finalidade |
|---|---|
| `DB_URL` | URL JDBC de conexão com o MySQL |
| `DB_USER` | Usuário do banco de dados |
| `DB_PASSWORD` | Senha do banco de dados |
| `PORT` | Porta HTTP da aplicação (padrão: 8080) |

O `application.properties` contém valores-padrão de conexão local, mas não inclui senha real. A equipe deve documentar as configurações no README sem divulgar credenciais.

Comandos, executados a partir da pasta `implementacao`:

```powershell
mvn clean test
mvn clean package
java -jar .\target\restaurante-0.1.0.jar
```

O comando `mvn clean test` foi executado com sucesso em 20/10/2026. O empacotamento com testes (`mvn clean package`) deve ter seu resultado registrado separadamente quando executado.

## 8. Testes e evidências técnicas disponíveis

| Identificação provisória | Verificação | Resultado observado | Situação |
|---|---|---|---|
| TM-RF05-01 | Cadastrar categoria “Bebidas” | Cadastro realizado e registro exibido | Aprovado em teste manual |
| TM-RF05-02 | Cadastrar produto “Suco de Laranja” | Cadastro realizado e registro exibido | Aprovado em teste manual |
| TM-RF05-03 | Reiniciar aplicação e consultar registros | Categoria e produto continuaram disponíveis | Aprovado em teste manual |
| TA-RF05-01 | `ApiControllerTest.cadastraCategoriaEConsulta` | Teste automatizado aprovado | Aprovado |
| TA-RF05-02 | `ApiControllerTest.rejeitaProdutoSemPreco` | Teste automatizado aprovado | Aprovado |

**Registro do teste automatizado:** 20/10/2026, Java 17.0.20.1, Spring Boot 3.3.5, banco H2 em memória para testes, `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`.

**Versão de referência:** commit `d45857a` da branch `entrega-20-10-2026`. A correspondência exata entre o diretório local testado e o commit deve ser confirmada pelo responsável por testes antes de registrar esse commit como versão formal de execução.

**Responsável pelo registro formal das evidências:** MateusPresner. Os identificadores acima são propostas e podem ser ajustados ao padrão de casos de teste da equipe. Capturas de tela e logs devem ser vinculados aos respectivos casos. A verificação de entradas inválidas foi comprovada no teste automatizado de produto sem preço, não como teste manual da interface.

## 9. Registro de alterações em relação ao primeiro bimestre

| Data | Item | Alteração / decisão | Motivo | Impacto |
|---|---|---|---|---|
| 20/10/2026 | RF05 | Selecionado como recorte inicial demonstrável | Começar por uma funcionalidade de alta prioridade e dependências reduzidas | Criação de interface, entidades, repositórios, controlador e testes |
| 20/10/2026 | Arquitetura | Tecnologias planejadas confirmadas em uma implementação local | Demonstrar funcionamento real, não apenas protótipo | Navegador, Spring Boot e MySQL conectados |
| 20/10/2026 | Ambiente | Java 17 adotado para execução dos testes | Compatibilidade com ferramentas de teste utilizadas | Dois testes automatizados aprovados |
| 20/10/2026 | Repositório | Criada branch `entrega-20-10-2026`, commit `d45857a` e Pull Request | Integrar a evolução ao projeto existente | Código disponível em branch; merge ainda pendente |
| 20/10/2026 | Documentação | Complemento técnico do segundo bimestre | Registrar decisões, estado real e pendências | Novo documento sem substituir os artefatos anteriores |

**Requisitos globais:** não foram eliminados nem renumerados. **Mudanças de escopo:** nenhuma mudança formal de escopo foi confirmada; houve apenas priorização temporal do RF05.

## 10. Rastreabilidade do recorte

| Requisito | Elementos de implementação | Verificação | Estado em 20/10 |
|---|---|---|---|
| RF05 — Cadastro de categorias | `Categoria.java`, `CategoriaRepository.java`, `ApiController.java`, `index.html` | TM-RF05-01; TA-RF05-01 | Cadastro e consulta demonstrados |
| RF05 — Cadastro de produtos | `Produto.java`, `ProdutoRepository.java`, `ApiController.java`, `index.html` | TM-RF05-02; TA-RF05-02 | Cadastro e consulta demonstrados; rejeição de produto sem preço verificada automaticamente |
| RF05 — Persistência | Repositórios JPA e configuração MySQL | TM-RF05-03 | Persistência observada após reinicialização |
| RF05 — Alteração e exclusão | A verificar nos endpoints e na interface | Casos ainda não registrados | Não validado nesta revisão |
| RF07 / RN13 — Composição e ingredientes | Planejamento original | Não testado no recorte | Pendente para etapas posteriores |

## 11. Pendências e próximos passos

**Para finalizar a entrega de 20/10:**

- Confirmar se a interface e a API oferecem alteração e exclusão e registrar o resultado real; caso contrário, identificar como funcionalidades em desenvolvimento.
- Anexar evidências dos testes manuais e automatizados e indicar versão/commit efetivamente testado.
- Integrar este documento e os demais registros ao Pull Request existente.
- Atualizar o README com versões, comandos, variáveis de ambiente e funcionalidades disponíveis.
- Revisar com a equipe a coerência entre protótipos, modelo/dicionário de dados e entidades realmente implementadas, registrando divergências sem apagar os modelos originais.
- Verificar acesso ao repositório e integração final à branch principal.

**Até 06/11/2026:** ampliar a implementação de acordo com o backlog definido pela equipe, testar os fluxos integrados e atualizar a rastreabilidade. A priorização detalhada e a atribuição das próximas tarefas cabem ao responsável pelo backlog, Matheusborgges, em conjunto com a equipe.

## 12. Conclusão

A documentação do primeiro bimestre permanece válida como base do projeto. A entrega de 20/10/2026 demonstra a transição do planejamento para a implementação inicial, com uma aplicação executável, operações básicas de cardápio, persistência em MySQL e dois testes automatizados aprovados. A conclusão integral do RF05 e dos demais módulos não é declarada nesta etapa; as lacunas estão identificadas para continuidade do desenvolvimento.

---

**Revisão antes do envio:** Guilherme20028 deve validar este texto com os arquivos e protótipos do repositório; MateusPresner deve conferir os registros de testes; Matheusborgges deve alinhar os próximos passos ao backlog; JoaoVitorPresner deve integrar a documentação ao Pull Request.
