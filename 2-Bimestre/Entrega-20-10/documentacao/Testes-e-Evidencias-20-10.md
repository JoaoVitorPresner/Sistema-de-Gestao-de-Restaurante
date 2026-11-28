# Registro de Testes e Evidências — Entrega de 20/10/2026

**Projeto:** Sistema de Gestão de Restaurante  
**Disciplina:** Fábrica de Software — 2º bimestre de 2026  
**Responsável pela organização dos registros:** MateusPresner  
**Requisito priorizado:** RF05 — Cardápio (produtos e categorias)  
**Repositório:** https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante  
**Branch de trabalho:** `entrega-20-10-2026`  
**Data de elaboração deste registro:** 20/10/2026  
**Situação:** registro atualizado após a execução dos CT01 a CT08; seis casos aprovados e dois bloqueados. Permanecem pendentes a confirmação dos testadores e a conferência final das evidências no GitHub.


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

| ID | Requisito | Verificação | Situação | Evidência |
|---|---|---|---|---|
| CT01 | RF05 | Execução dos testes automatizados via Maven | **APROVADO** — 2 testes, 0 falhas, 0 erros, 0 ignorados | [EV01-Testes-Automatizados.png](../evidencias/EV01-Testes-Automatizados.png) |
| CT02 | RF05 | Cadastro da categoria `Sobremesas` | **APROVADO** — categoria cadastrada e apresentada na listagem | `../evidencias/EV02-Cadastro-Categoria.png` |
| CT03 | RF05 | Cadastro do produto `Pudim de Leite` | **APROVADO** — produto cadastrado e apresentado na listagem | `../evidencias/EV03-Cadastro-Produto.png` |
| CT04 | RF05 | Consulta de categorias e produtos | **APROVADO** — registros apresentados corretamente | `../evidencias/EV04-Consulta-Cardapio.png` |
| CT05 | RF05 | Persistência após reinicialização da aplicação | **APROVADO** — registros permaneceram disponíveis após reiniciar | `../evidencias/EV05-Antes-Reinicio.png` e `../evidencias/EV05-Persistencia-Apos-Reinicio.png` |
| CT06 | RF05 | Alteração de produto existente | **BLOQUEADO** — funcionalidade de edição não implementada | Inspeção da interface e do `ApiController.java`; sem captura anexada |
| CT07 | RF05 | Exclusão de produto existente | **BLOQUEADO** — funcionalidade de exclusão não implementada | Inspeção da interface e do `ApiController.java`; sem captura anexada |
| CT08 | RF05 | Validação de campos obrigatórios e preço negativo | **APROVADO — INTERFACE** — quatro verificações aprovadas | Quatro capturas `EV08-Validacao-*.png` |

**Resultado consolidado:** 8 casos de teste registrados, sendo **6 aprovados e 2 bloqueados**.

Os casos CT06 e CT07 não foram aprovados nem reprovados, pois as operações necessárias ainda não estão implementadas.

O CT08 comprova as validações realizadas pelo navegador. A validação direta dos endpoints da API não foi executada.

Os identificadores CT01–CT08 organizam este documento. Caso a equipe possua identificadores anteriores para os mesmos testes, deverá alinhar a nomenclatura na documentação.

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
- **Evidência anexada:** [EV02-Cadastro-Categoria.png](../evidencias/EV02-Cadastro-Categoria.png).
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
- **Evidência anexada:** [EV03-Cadastro-Produto.png](../evidencias/EV03-Cadastro-Produto.png)
.
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
- **Evidência anexada:** [EV04-Consulta-Cardapio.png](../evidencias/EV04-Consulta-Cardapio.png).
- **Limitação:** este teste confirma a consulta dos registros existentes. A persistência após reinicialização é verificada separadamente no CT05.

### CT05 — Persistência após reinicialização

- **Requisito:** RF05 — Cardápio.
- **Pré-condições:** aplicação Spring Boot disponível; conexão com MySQL estabelecida; categorias `Bebidas` e `Sobremesas` e produtos `Suco de Laranja` e `Pudim de Leite` previamente cadastrados.
- **Passos executados:**
  1. Iniciar a aplicação com `java -jar target\restaurante-0.1.0.jar`.
  2. Acessar `http://localhost:8080`.
  3. Consultar as categorias e os produtos cadastrados.
  4. Confirmar a presença dos quatro registros e capturar a evidência anterior à reinicialização.
  5. Encerrar a aplicação utilizando `Ctrl + C` no PowerShell.
  6. Reiniciar a aplicação utilizando o mesmo comando.
  7. Aguardar a inicialização do servidor e atualizar a página no navegador.
  8. Consultar novamente os registros, sem realizar novos cadastros.
  9. Capturar a evidência posterior à reinicialização.
- **Dados de entrada:** categorias `Bebidas` e `Sobremesas`; produtos `Suco de Laranja` e `Pudim de Leite`, previamente armazenados no MySQL.
- **Resultado esperado:** os quatro registros devem permanecer armazenados e disponíveis para consulta após encerrar e reiniciar a aplicação.
- **Resultado observado:** após a reinicialização, as duas categorias e os dois produtos continuaram disponíveis na interface, sem necessidade de novos cadastros, conforme execução manual confirmada.
- **Situação:** **APROVADO**.
- **Data da execução:** 20/10/2026.
- **Horário da execução:** não registrado.
- **Testador:** a confirmar.
- **Ambiente:** Java Temurin 17.0.20.1; Spring Boot 3.3.5; MySQL; interface HTML, CSS e JavaScript; servidor local na porta 8080.
- **Commit exato testado:** `0ab2af6`.
- **Evidências anexadas:**
  - [EV05-Antes-Reinicio.png](../evidencias/EV05-Antes-Reinicio.png)
  - [EV05-Persistencia-Apos-Reinicio.png](../evidencias/EV05-Persistencia-Apos-Reinicio.png)
- **Limitação:** o teste confirma a permanência dos registros após reiniciar a aplicação, mas não verifica recuperação de dados após falhas do banco de dados ou outras situações excepcionais.

### CT06 — Alteração de registro (bloqueado)

- **Requisito:** RF05 — Cardápio.
- **Pré-condições:** aplicação Spring Boot iniciada; conexão com MySQL disponível; produto `Pudim de Leite` previamente cadastrado; interface acessível em `http://localhost:8080`.
- **Passos de verificação realizados:**
  1. Iniciar a aplicação e acessar a interface pelo navegador.
  2. Localizar o produto `Pudim de Leite` na listagem.
  3. Procurar uma opção de edição ou alteração do produto.
  4. Constatar a ausência de uma opção de edição na interface.
  5. Inspecionar o arquivo `ApiController.java` para verificar a existência de endpoints de atualização.
  6. Confirmar que o controlador não possui métodos com `@PutMapping` ou `@PatchMapping`.
- **Entrada proposta:** produto `Pudim de Leite`; alteração de preço para R$ 14,90, condicionada à verificação do preço original.
- **Resultado esperado:** permitir a alteração de um campo editável de um produto existente, salvar a modificação no banco de dados e apresentar o novo valor na consulta.
- **Resultado observado:** a interface não apresenta uma opção de edição de produtos. A inspeção do controlador confirmou que não existem endpoints de atualização implementados. Por esse motivo, não foi possível executar a operação de alteração nem verificar sua persistência.
- **Situação:** **BLOQUEADO — FUNCIONALIDADE NÃO IMPLEMENTADA**.
- **Data da verificação:** 20/10/2026.
- **Horário da verificação:** não registrado.
- **Testador:** a confirmar.
- **Ambiente:** Java Temurin 17.0.20.1; Spring Boot 3.3.5; MySQL; interface HTML, CSS e JavaScript.
- **Commit da versão verificada:** `ecc652f`.
- **Evidência:** inspeção do arquivo `src/main/java/br/com/restaurante/ApiController.java` e verificação manual da interface; captura de tela não anexada.
- **Pendência identificada:** implementar a funcionalidade de alteração de produtos, incluindo endpoint de atualização, opção de edição na interface e persistência das modificações no MySQL.
- **Encaminhamento:** registrar a implementação no backlog, com responsável, prioridade e prazo definidos pela equipe; executar novamente o CT06 após a implementação.
- **Limitação:** este registro documenta o impedimento encontrado, não constituindo aprovação ou reprovação de uma operação de atualização efetivamente executada.

### CT07 — Exclusão de registro (bloqueado)

- **Requisito:** RF05 — Cardápio.
- **Pré-condições:** aplicação Spring Boot iniciada; conexão com MySQL disponível; produto `Pudim de Leite` previamente cadastrado; interface acessível em `http://localhost:8080`.
- **Passos de verificação realizados:**
  1. Acessar a interface do sistema.
  2. Localizar o produto `Pudim de Leite` na listagem.
  3. Verificar a existência de uma opção para excluir ou remover o produto.
  4. Constatar a ausência dessa funcionalidade na interface.
  5. Inspecionar o arquivo `ApiController.java` para verificar a existência de endpoints de exclusão.
  6. Confirmar que o controlador não possui métodos com `@DeleteMapping`.
- **Entrada proposta:** exclusão de um produto fictício previamente cadastrado, mediante confirmação do usuário.
- **Resultado esperado:** permitir excluir um produto existente, remover seu registro do banco de dados e garantir que ele não apareça mais na consulta.
- **Resultado observado:** a interface não apresenta opção de exclusão de produtos. A inspeção do controlador confirmou que não existem endpoints de exclusão implementados. Portanto, não foi possível executar a operação nem verificar a remoção do registro.
- **Situação:** **BLOQUEADO — FUNCIONALIDADE NÃO IMPLEMENTADA**.
- **Data da verificação:** 20/10/2026.
- **Horário da verificação:** não registrado.
- **Testador:** a confirmar.
- **Ambiente:** Java Temurin 17.0.20.1; Spring Boot 3.3.5; MySQL; interface HTML, CSS e JavaScript.
- **Commit da versão verificada:** `67ca690`.
- **Evidência:** inspeção do arquivo `src/main/java/br/com/restaurante/ApiController.java` e verificação manual da interface; captura de tela não anexada.
- **Pendência identificada:** implementar a funcionalidade de exclusão de produtos, incluindo endpoint de exclusão, opção de remoção na interface e tratamento das regras de negócio relacionadas.
- **Encaminhamento:** registrar a implementação no backlog, com responsável, prioridade e prazo definidos pela equipe; executar novamente o CT07 após a implementação.
- **Limitação:** este registro documenta o impedimento encontrado, não constituindo aprovação ou reprovação de uma operação de exclusão efetivamente executada.

### CT08 — Validação de campos obrigatórios e entradas inválidas

- **Requisito:** RF05 — Cardápio.
- **Pré-condições:** aplicação Spring Boot iniciada; conexão com MySQL disponível; interface acessível em `http://localhost:8080`; categoria `Sobremesas` previamente cadastrada.
- **Data da execução:** 20/10/2026.
- **Testador:** a confirmar.
- **Ambiente:** Java Temurin 17.0.20.1; Spring Boot 3.3.5; MySQL; interface HTML, CSS e JavaScript.

**Verificação 1 — Nome obrigatório**

- **Entrada:** nome vazio; categoria `Sobremesas`; preço R$ 10,00; descrição `Produto fictício para teste`.
- **Resultado esperado:** impedir o cadastro de produto sem nome.
- **Resultado observado:** o navegador exibiu a mensagem `Preencha este campo` e impediu o envio do formulário.
- **Situação:** APROVADO.
- **Evidência:** [EV08-Validacao-Nome-Obrigatorio.png](../evidencias/EV08-Validacao-Nome-Obrigatorio.png).

**Verificação 2 — Preço negativo**

- **Entrada:** nome `Produto Teste Preço`; categoria `Sobremesas`; preço `-10`; descrição `Teste de preço negativo`.
- **Resultado esperado:** impedir o cadastro de produto com preço negativo.
- **Resultado observado:** o navegador impediu o envio do formulário e apresentou uma mensagem de validação.
- **Situação:** APROVADO.
- **Evidência:** [EV08-Validacao-Preco-Negativo.png](../evidencias/EV08-Validacao-Preco-Negativo.png)
.

**Verificação 3 — Preço obrigatório**

- **Entrada:** nome `Produto Teste Sem Preço`; categoria `Sobremesas`; preço vazio; descrição `Teste de preço obrigatório`.
- **Resultado esperado:** impedir o cadastro de produto sem preço.
- **Resultado observado:** o navegador exibiu a mensagem `Preencha este campo` e impediu o envio do formulário.
- **Situação:** APROVADO.
- **Evidência:** [EV08-Validacao-Preco-Obrigatorio.png](../evidencias/EV08-Validacao-Preco-Obrigatorio.png)
.

**Verificação 4 — Categoria obrigatória**

- **Entrada:** nome `Produto Teste Sem Categoria`; categoria não selecionada; preço R$ 10,00; descrição `Teste de categoria obrigatória`.
- **Resultado esperado:** impedir o cadastro de produto sem categoria.
- **Resultado observado:** o navegador impediu o envio do formulário e solicitou a seleção de uma categoria.
- **Situação:** APROVADO.
- **Evidência:** [EV08-Validacao-Categoria-Obrigatoria.png](../evidencias/EV08-Validacao-Categoria-Obrigatoria.png).

**Resultado consolidado:** quatro verificações executadas e aprovadas na interface.

- **Situação do CT08:** **APROVADO — VALIDAÇÕES DA INTERFACE**.
- **Commit da versão testada:** `fa85052`.
- **Limitação:** as verificações foram realizadas pela interface web. A validação diretamente nos endpoints da API não foi executada e deverá ser verificada separadamente, se necessária ao escopo.
- **Encaminhamento:** manter as evidências no repositório e atualizar a rastreabilidade do requisito RF05.

## 5. Organização das evidências

As evidências dos testes foram organizadas na pasta:

`2-Bimestre/Entrega-20-10/evidencias/`

Os registros detalhados estão documentados em:

`2-Bimestre/Entrega-20-10/documentacao/Testes-e-Evidencias-20-10.md`

### Evidências registradas

| Teste | Arquivo ou registro |
|---|---|
| CT01 | `EV01-Testes-Automatizados.png` |
| CT02 | `EV02-Cadastro-Categoria.png` |
| CT03 | `EV03-Cadastro-Produto.png` |
| CT04 | `EV04-Consulta-Cardapio.png` |
| CT05 | `EV05-Antes-Reinicio.png` |
| CT05 | `EV05-Persistencia-Apos-Reinicio.png` |
| CT06 | Inspeção da interface e do código; captura não anexada |
| CT07 | Inspeção da interface e do código; captura não anexada |
| CT08 | `EV08-Validacao-Nome-Obrigatorio.png` |
| CT08 | `EV08-Validacao-Preco-Negativo.png` |
| CT08 | `EV08-Validacao-Preco-Obrigatorio.png` |
| CT08 | `EV08-Validacao-Categoria-Obrigatoria.png` |

As capturas foram registradas durante as verificações manuais informadas pela equipe. A presença de todos os arquivos no repositório remoto deverá ser confirmada antes da entrega.

### Execução dos testes automatizados

Os testes automatizados foram executados com Java 17, utilizando o comando:

```powershell
mvn clean test
```

O resultado registrado no CT01 foi:

- Tests run: 2
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

O banco utilizado nesses testes foi H2 em memória. A persistência real no MySQL foi verificada separadamente no CT05.

### Identificação e rastreabilidade

Cada caso de teste contém informações sobre requisito, pré-condições, passos, entradas, resultados esperados e observados, situação, data e commit da versão verificada.

Os horários não confirmados e a identificação dos testadores permanecem explicitamente indicados como pendentes, evitando o registro de informações presumidas.

---

## 6. Defeitos, limitações e riscos conhecidos

| Item | Situação identificada | Ação necessária |
|---|---|---|
| Incompatibilidade anterior com JDK 26 durante testes automatizados | Contornada utilizando Java Temurin 17.0.20.1; CT01 aprovado | Manter Java 17 documentado e validar a execução em outro ambiente |
| Alteração de produtos — CT06 | **BLOQUEADO:** interface sem opção de edição e controlador sem endpoint de atualização | Implementar edição, registrar no backlog e executar novamente o CT06 |
| Exclusão de produtos — CT07 | **BLOQUEADO:** interface sem opção de exclusão e controlador sem endpoint de remoção | Implementar exclusão, definir regras de negócio e executar novamente o CT07 |
| Validação de entradas — CT08 | Quatro verificações aprovadas na interface | Verificar separadamente a validação direta da API, conforme prioridade do projeto |
| Cobertura dos testes automatizados | Dois testes aprovados; cobertura integral do RF05 não demonstrada | Ampliar os testes conforme novas funcionalidades forem implementadas |
| Identificação dos testadores | Ainda não confirmada nos registros detalhados | Preencher com os responsáveis reais antes da entrega |
| Evidências no GitHub | Capturas informadas como salvas; conferir presença na branch remota | Verificar os arquivos e seus caminhos no repositório |
| Execução em outro computador | Não documentada neste registro | Validar o procedimento do README em ambiente de outro integrante |

As funcionalidades de alteração e exclusão não foram removidas do escopo do RF05. Permanecem como pendências de implementação.

---

## 7. Rastreabilidade inicial

| Requisito | Implementação ou situação | Testes relacionados | Resultado em 20/10/2026 |
|---|---|---|---|
| RF05 — Cadastro de categorias | Cadastro disponível na aplicação | CT02 | **APROVADO** |
| RF05 — Cadastro de produtos | Cadastro disponível na aplicação | CT03 | **APROVADO** |
| RF05 — Consulta de cardápio | Listagem de categorias e produtos disponível | CT04 | **APROVADO** |
| RF05 — Persistência | Armazenamento em MySQL | CT05 | **APROVADO** |
| RF05 — Testes automatizados | Dois testes existentes executados com Maven e H2 | CT01 | **APROVADO**, dentro da cobertura existente |
| RF05 — Alteração de registros | Funcionalidade não implementada | CT06 | **BLOQUEADO** |
| RF05 — Exclusão de registros | Funcionalidade não implementada | CT07 | **BLOQUEADO** |
| RF05 — Validação de entradas | Validações verificadas na interface web | CT08 | **APROVADO NA INTERFACE**; API não verificada diretamente |

**Localização da implementação:**

`2-Bimestre/Entrega-20-10/implementacao/`

Os resultados demonstram que o recorte inicial do RF05 permite cadastrar, consultar e manter registros após reinicialização, mas ainda não contempla todas as operações previstas para o cardápio.

---

## 8. Próximos passos até 06/11/2026

1. Confirmar que todas as capturas dos testes CT01 a CT05 e CT08 estão presentes no GitHub e acessíveis pelos caminhos documentados.
2. Registrar no backlog a implementação da alteração de produtos, relacionada ao CT06, com prioridade, responsável e prazo.
3. Registrar no backlog a implementação da exclusão de produtos, relacionada ao CT07, incluindo o tratamento das regras de negócio necessárias.
4. Implementar e testar as funcionalidades pendentes conforme as prioridades definidas pela equipe.
5. Ampliar as verificações de validação para os endpoints da API, quando aplicável, sem confundir a validação do navegador com a validação do servidor.
6. Reexecutar os testes afetados pelas futuras alterações e registrar novos resultados, evidências e commits.
7. Validar a execução da aplicação em outro computador, seguindo as instruções do README.
8. Completar a identificação dos testadores e conferir a documentação, o backlog e a rastreabilidade antes da próxima entrega.

---

## 9. Critério para concluir esta atividade documental

A documentação registra os oito casos de teste previstos para a entrega de 20/10/2026, com os seguintes resultados:

- **6 casos aprovados:** CT01, CT02, CT03, CT04, CT05 e CT08.
- **2 casos bloqueados:** CT06 e CT07.

Os testes aprovados possuem resultados observados registrados. Os testes bloqueados apresentam justificativas técnicas e encaminhamentos para implementação futura.

**Pendências de conferência antes da entrega:**

- Confirmar os responsáveis reais pelas execuções.
- Verificar a presença das capturas no GitHub.
- Confirmar a integração da documentação na branch de entrega.
- Conferir se as funcionalidades pendentes estão representadas no backlog.
- Confirmar que o commit da versão entregue está identificado.

A conclusão desta etapa documental não representa a conclusão integral do RF05 nem do Sistema de Gestão de Restaurante. O documento registra o estado da implementação e dos testes em 20/10/2026, preservando a distinção entre funcionalidades aprovadas, bloqueadas e verificações ainda não realizadas.

---

**Referência de orientação:** *Manual da primeira entrega do segundo bimestre*, disciplina Fábrica de Software, professora Sabine Cassol, entrega de 20/10/2026.