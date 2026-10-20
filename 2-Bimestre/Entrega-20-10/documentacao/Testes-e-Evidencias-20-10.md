# Registro de Testes e Evidências — Entrega de 20/10/2026

**Projeto:** Sistema de Gestão de Restaurante  
**Disciplina:** Fábrica de Software — 2º bimestre de 2026  
**Responsável pela organização dos registros:** MateusPresner  
**Requisito priorizado:** RF05 — Cardápio (produtos e categorias)  
**Repositório:** https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante  
**Branch de trabalho:** `entrega-20-10-2026`  
**Data de elaboração deste registro:** 20/10/2026  
**Situação:** registro inicial; evidências visuais e dados de execução ainda precisam ser complementados.

> **Critério de honestidade dos registros:** somente resultados efetivamente informados como executados estão marcados como aprovados. Não foram presumidos horários, datas específicas de execução, capturas de tela, conteúdo dos dois métodos de teste ou a versão exata usada em cada teste. Essas informações devem ser conferidas pela equipe antes da entrega.

## 1. Objetivo e escopo

Documentar a verificação inicial da implementação do RF05 — Cardápio, incluindo execução dos testes automatizados, cadastro e consulta de categorias e produtos, além da persistência no MySQL. O recorte demonstrável da entrega de 20/10 **não equivale à conclusão integral do RF05**.

## 2. Ambiente de testes conhecido

| Componente | Informação conhecida | Observação |
|---|---|---|
| Linguagem | Java 17 (Temurin) | Utilizada na execução bem-sucedida dos testes automatizados |
| Framework | Spring Boot 3.3.5 | Aplicação Java |
| Gerenciador de dependências | Maven | Comando de testes: `mvn clean test` |
| Banco dos testes automatizados | H2 em memória | Não confundir com a persistência real no MySQL |
| Banco da aplicação | MySQL | Utilizado na verificação manual de persistência |
| Interface | HTML, CSS e JavaScript | Disponível pela aplicação local |
| Endereço local | `http://localhost:8080` | Requer aplicação e banco configurados |
| Commit da implementação inicial | `d45857a` | Identifica o envio inicial do código; **não comprova**, sozinho, o commit exato de cada execução |
| Commit mais recente informado antes deste documento | `6c09499` | Inclui documentação e backlog; não atribuir retroativamente aos testes |

**Configuração:** a aplicação utiliza as variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD`, sem registrar credenciais neste documento.

## 3. Resumo dos testes e situações

| ID | Requisito | Verificação | Resultado informado | Evidência |
|---|---|---|---|---|
| CT01 | RF05 | Execução dos testes automatizados via Maven | **Aprovado**: 2 executados, 0 falhas, 0 erros, 0 ignorados | Saída de terminal conhecida; arquivo/captura pendente |
| CT02 | RF05 | Cadastrar categoria de exemplo | **Aprovado**, conforme execução manual relatada | Captura pendente |
| CT03 | RF05 | Cadastrar produto associado ao cardápio | **Aprovado**, conforme execução manual relatada | Captura pendente |
| CT04 | RF05 | Consultar os registros cadastrados | **Aprovado**, conforme execução manual relatada | Captura pendente |
| CT05 | RF05 | Verificar persistência após reiniciar a aplicação | **Aprovado**, conforme execução manual relatada | Captura pendente |
| CT06 | RF05 | Alterar categoria ou produto existente | **Não executado / não confirmado** | Pendente |
| CT07 | RF05 | Excluir categoria ou produto existente | **Não executado / não confirmado** | Pendente |
| CT08 | RF05 | Validar campos obrigatórios e entradas inválidas | **Não executado / não confirmado** | Pendente |

> Os identificadores CT01–CT08 organizam **este documento**. Se a equipe já possui identificadores de casos de teste no primeiro bimestre, alinhar a nomenclatura antes da versão final.

## 4. Registros detalhados

### CT01 — Execução dos testes automatizados

- **Requisito relacionado:** RF05 — Cardápio (testes automatizados da aplicação; a correspondência específica dos dois métodos com cada comportamento do RF05 deve ser conferida no código).
- **Responsável pelo registro:** MateusPresner. **Executor do teste:** não identificado no registro do terminal; confirmar com a equipe.
- **Pré-condições:** projeto Maven disponível na pasta `2-Bimestre/Entrega-20-10/implementacao`; Java Temurin 17.0.20.1 configurado; dependências Maven acessíveis.
- **Ambiente de execução:** Windows, Java Temurin 17.0.20.1, Spring Boot 3.3.5, Maven (versão exata não registrada) e banco H2 em memória para os testes automatizados.
- **Passos executados:**
  1. Acessar a pasta `2-Bimestre/Entrega-20-10/implementacao` pelo PowerShell.
  2. Executar `java -version` e confirmar a versão 17.0.20.1.
  3. Executar `mvn clean test`.
  4. Conferir o resumo dos testes e o resultado final da compilação.
- **Entrada:** código-fonte e configurações de teste do projeto, sem dados digitados manualmente.
- **Resultado esperado:** execução dos testes automatizados sem falhas ou erros, encerrando com `BUILD SUCCESS`.
- **Resultado observado:** execução de `br.com.restaurante.ApiControllerTest` com `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`.
- **Situação:** **APROVADO**.
- **Data e horário da execução:** 20/10/2026, conclusão às 17:29:45 (UTC−03:00), conforme saída do Maven.
- **Branch e commit da versão testada:** `entrega-20-10-2026`, commit `e413d38`, informado por `git rev-parse --short HEAD` após a execução; não houve alteração de código relatada entre os comandos.
- **Evidência:** [`EV01-Testes-Automatizados.png`](../evidencias/EV01-Testes-Automatizados.png), captura salva em `2-Bimestre/Entrega-20-10/evidencias/` em 20/10/2026 às 17:36, conforme listagem do PowerShell. **Confirmar o envio da imagem ao GitHub.**
- **Limitação:** a execução automatizada utilizou H2 em memória, não MySQL. Este resultado comprova a aprovação dos dois testes existentes, mas não a cobertura completa do RF05 nem a persistência real no MySQL, que é verificada separadamente no CT05.

### CT02 — Cadastro de categoria

- **Requisito:** RF05 — Cardápio.
- **Pré-condições:** aplicação Spring Boot iniciada com Java 17; conexão com MySQL estabelecida; interface acessível em `http://localhost:8080`.
- **Passos executados:**
  1. Iniciar a aplicação com `java -jar target\restaurante-0.1.0.jar`.
  2. Acessar a interface pelo navegador.
  3. Localizar o formulário de cadastro de categorias.
  4. Informar o nome `Sobremesas`.
  5. Salvar o cadastro.
  6. Verificar se a categoria aparece na listagem.
- **Dados de entrada:** nome da categoria `Sobremesas`.
- **Resultado esperado:** categoria cadastrada com sucesso e disponível para consulta na interface.
- **Resultado observado:** a categoria `Sobremesas` foi cadastrada e apareceu corretamente na listagem, conforme confirmação da execução manual.
- **Situação:** **APROVADO**.
- **Data da execução:** 20/10/2026.
- **Horário da execução:** não registrado; aplicação iniciada às 17h54min54s.
- **Testador:** a confirmar.
- **Ambiente:** Java Temurin 17.0.20.1; Spring Boot 3.3.5; MySQL; interface HTML, CSS e JavaScript; servidor local na porta 8080.
- **Commit exato testado:** `6e0eb6b`.
- **Evidência anexada:** `../evidencias/EV02-Cadastro-Categoria.png`.
- **Limitação:** este teste confirma o cadastro e a visualização da categoria na interface. A persistência após reinicialização é verificada separadamente no CT05.

### CT03 — Cadastro de produto

- **Requisito:** RF05 — Cardápio.
- **Pré-condições:** aplicação Spring Boot iniciada; conexão com MySQL disponível; categoria `Sobremesas` previamente cadastrada; interface acessível em `http://localhost:8080`.
- **Passos executados:**
  1. Iniciar a aplicação com `java -jar target\restaurante-0.1.0.jar`.
  2. Acessar a interface pelo navegador.
  3. Localizar o formulário de cadastro de produtos.
  4. Preencher os dados do produto `Pudim de Leite`.
  5. Selecionar a categoria `Sobremesas`.
  6. Salvar o cadastro.
  7. Verificar se o produto aparece na listagem.
- **Dados de entrada:** produto `Pudim de Leite`; categoria `Sobremesas`; preço sugerido de R$ 12,90 e descrição sugerida `Pudim de leite condensado` (confirmar os valores efetivamente utilizados).
- **Resultado esperado:** produto cadastrado com sucesso, associado à categoria selecionada e disponível para consulta.
- **Resultado observado:** o produto `Pudim de Leite` apareceu corretamente na listagem após o cadastro, conforme confirmação da execução manual.
- **Situação:** **APROVADO**.
- **Data da execução:** 20/10/2026.
- **Horário da execução:** não registrado.
- **Testador:** a confirmar.
- **Ambiente:** Java Temurin 17.0.20.1; Spring Boot 3.3.5; MySQL; interface HTML, CSS e JavaScript.
- **Commit exato testado:** `dc41aa1`.
- **Evidência anexada:** `../evidencias/EV03-Cadastro-Produto.png`.
- **Limitação:** este teste verifica o cadastro e a visualização inicial do produto. A persistência após reinicialização é verificada separadamente no CT05.

### CT04 — Consulta de registros

- **Requisito:** RF05 — Cardápio.
- **Pré-condições:** aplicação Spring Boot iniciada; conexão com MySQL estabelecida; categorias e produtos previamente cadastrados; interface acessível em `http://localhost:8080`.
- **Passos executados:**
  1. Iniciar a aplicação com `java -jar target\restaurante-0.1.0.jar`.
  2. Acessar a interface pelo navegador.
  3. Consultar a listagem de categorias cadastradas.
  4. Verificar a presença das categorias `Bebidas` e `Sobremesas`.
  5. Consultar a listagem de produtos cadastrados.
  6. Verificar a presença dos produtos `Suco de Laranja` e `Pudim de Leite`.
- **Dados de entrada:** registros previamente cadastrados no banco de dados MySQL.
- **Resultado esperado:** categorias e produtos cadastrados devem aparecer corretamente nas respectivas listagens, permitindo a consulta dos registros.
- **Resultado observado:** as categorias `Bebidas` e `Sobremesas` e os produtos `Suco de Laranja` e `Pudim de Leite` foram apresentados corretamente na interface, conforme execução manual confirmada.
- **Situação:** **APROVADO**.
- **Data da execução:** 20/10/2026.
- **Horário da execução:** não registrado.
- **Testador:** a confirmar.
- **Ambiente:** Java Temurin 17.0.20.1; Spring Boot 3.3.5; MySQL; interface HTML, CSS e JavaScript.
- **Commit exato testado:** `a2d801b`.
- **Evidência anexada:** `../evidencias/EV04-Consulta-Cardapio.png`.
- **Limitação:** este teste confirma a consulta dos registros existentes. A persistência após reinicialização é verificada separadamente no CT05.

### CT05 — Persistência após reinicialização

- **Requisito:** RF05.
- **Pré-condições:** registros previamente cadastrados no MySQL.
- **Passos relatados:** encerrar e reiniciar a aplicação; consultar novamente os registros.
- **Entrada:** registros cadastrados nos testes anteriores.
- **Resultado esperado:** registros permanecem disponíveis após reiniciar a aplicação.
- **Resultado observado informado:** categoria e produto continuaram armazenados no MySQL após reinicialização.
- **Situação:** **APROVADO (relato de execução manual)**.
- **Data/hora exata e commit testado:** **a confirmar**.
- **Evidência a anexar:** `evidencias/CT05-persistencia-apos-reinicio.png`.

### CT06 — Alteração de registro (pendente)

- **Requisito:** RF05.
- **Pré-condições:** possuir categoria ou produto existente.
- **Passos propostos:** localizar um registro; alterar um campo permitido; salvar; consultar novamente.
- **Entrada proposta:** alterar a descrição ou outro campo editável de um produto fictício.
- **Resultado esperado:** novo valor persistido e apresentado corretamente.
- **Resultado observado:** **não há execução confirmada**.
- **Situação:** **NÃO EXECUTADO / NÃO CONFIRMADO**.
- **Data, commit e evidência:** pendentes.

### CT07 — Exclusão de registro (pendente)

- **Requisito:** RF05.
- **Pré-condições:** possuir um registro fictício criado especificamente para o teste; verificar possíveis vínculos.
- **Passos propostos:** selecionar registro; solicitar exclusão; confirmar; consultar novamente.
- **Entrada proposta:** registro descartável de teste.
- **Resultado esperado:** comportamento conforme as regras do sistema, inclusive restrições por vínculos quando aplicáveis.
- **Resultado observado:** **não há execução confirmada**.
- **Situação:** **NÃO EXECUTADO / NÃO CONFIRMADO**.
- **Data, commit e evidência:** pendentes.

### CT08 — Validações de entrada (pendente)

- **Requisito:** RF05 e regras de negócio pertinentes, a confirmar na documentação original.
- **Pré-condições:** formulário de cadastro acessível.
- **Passos propostos:** tentar salvar campos obrigatórios vazios e outros valores inválidos previstos nas regras.
- **Entrada:** casos inválidos a definir com base nas regras de negócio.
- **Resultado esperado:** entradas inválidas são rejeitadas com resposta compreensível, sem gravar dados inconsistentes.
- **Resultado observado:** **não há execução confirmada**.
- **Situação:** **NÃO EXECUTADO / NÃO CONFIRMADO**.
- **Data, commit e evidência:** pendentes.

## 5. Organização das evidências

Salvar capturas e registros reais na pasta `2-Bimestre/Entrega-20-10/documentacao/evidencias/` com os nomes sugeridos nos testes. Não criar imagens fictícias para simular resultados. Antes de capturar telas, utilizar somente dados de demonstração e ocultar credenciais, tokens e informações pessoais.

### Como registrar a saída do Maven

Na pasta `implementacao`, com Java 17 ativo:

```powershell
mvn clean test 2>&1 | Tee-Object -FilePath "..\documentacao\evidencias\CT01-maven-testes.txt"
```

**Atenção:** o caminho acima pressupõe que o terminal está em `2-Bimestre/Entrega-20-10/implementacao` e que a pasta `evidencias` já existe. Esta será **uma nova execução**, com nova data e commit a registrar; só marque como aprovada após verificar o resultado real. Alternativamente, salvar captura do resumo do terminal.

### Registro complementar obrigatório para cada execução

| Campo | Preencher após executar |
|---|---|
| Data e horário | `DD/MM/AAAA HH:MM` |
| Testador | Usuário GitHub de quem executou |
| Versão / commit | Saída de `git rev-parse --short HEAD` |
| Ambiente | Java, Maven, MySQL/H2 conforme o teste |
| Resultado observado | O que efetivamente aconteceu |
| Evidência | Caminho do arquivo anexado |
| Defeito, se houver | Descrição, passos para reproduzir e responsável pela correção |

## 6. Defeitos, limitações e riscos conhecidos

| Item | Situação | Ação |
|---|---|---|
| Incompatibilidade anterior com JDK 26 durante os testes (Mockito/Byte Buddy) | Contornada ao executar com Java 17; testes passaram | Documentar Java 17 no README e validar em outra máquina |
| Alteração e exclusão do RF05 | Não verificadas | Executar CT06 e CT07 e registrar resultados reais |
| Validações de entrada | Não verificadas | Confrontar regras originais e executar CT08 |
| Evidências visuais | Não anexadas neste registro inicial | Capturar telas e logs reais |
| Datas e commits exatos dos testes já relatados | Não registrados | Completar com informações verificáveis; não inventar |

## 7. Rastreabilidade inicial

| Requisito | Implementação | Testes | Situação em 20/10 |
|---|---|---|---|
| RF05 — Cardápio | Código em `2-Bimestre/Entrega-20-10/implementacao/` | CT01 a CT05 | Cadastro, consulta e persistência relatados como funcionais; evidências a anexar |
| RF05 — Alteração/exclusão | Conferir endpoints e interface no código | CT06 e CT07 | Ainda não verificado |
| RF05 — Validações | Conferir regras de negócio e código | CT08 | Ainda não verificado |

## 8. Próximos passos até 06/11/2026

1. Anexar capturas de tela ou logs de CT01 a CT05, com data, testador e commit.
2. Executar CT06, CT07 e CT08 e atualizar o status de acordo com os resultados.
3. Caso haja defeitos, registrar passos de reprodução, comportamento esperado e observado, correção e reteste.
4. Repetir a execução em ambiente de outro integrante, seguindo as instruções do README.
5. Integrar os registros ao backlog, ao README e à documentação técnica, mantendo a rastreabilidade atualizada.

## 9. Critério para concluir esta atividade documental

A atividade de testes e evidências estará pronta para revisão final quando os registros tiverem **ID, requisito, pré-condições, passos, entrada, resultado esperado, resultado observado, status, data, versão/commit e evidência correspondente**, com pendências explicitadas e sem testes não executados marcados como aprovados.

---

**Referência de orientação:** *Manual da primeira entrega do segundo bimestre*, disciplina Fábrica de Software, professora Sabine Cassol, entrega de 20/10/2026, especialmente os passos sobre testes, evidências, rastreabilidade e organização da entrega.
