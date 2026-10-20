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

- **Requisito relacionado:** RF05 — Cardápio (verificações automatizadas do projeto; associação exata de cada método ao requisito precisa ser conferida no código).
- **Pré-condições:** projeto Maven disponível e Java 17 configurado no terminal.
- **Passos executados:** abrir o terminal na pasta `implementacao`; executar `mvn clean test`; conferir o resumo.
- **Entrada:** código e configurações de teste do projeto; sem entrada manual adicional.
- **Resultado esperado:** testes executados sem falhas ou erros; comando finaliza com `BUILD SUCCESS`.
- **Resultado observado informado:** `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` e `BUILD SUCCESS`.
- **Situação:** **APROVADO**.
- **Data/hora exata da execução:** **a confirmar**.
- **Commit exato testado:** **a confirmar**.
- **Evidência a anexar:** `evidencias/CT01-maven-testes.png` ou `evidencias/CT01-maven-testes.txt`.
- **Limitação:** o H2 utilizado nos testes automatizados não substitui a verificação manual de persistência no MySQL.

### CT02 — Cadastro de categoria

- **Requisito:** RF05.
- **Pré-condições:** aplicação iniciada; conexão com MySQL disponível; interface acessível em `http://localhost:8080`.
- **Passos relatados:** acessar a interface; cadastrar uma categoria; verificar sua apresentação na aplicação.
- **Dados de entrada:** nome da categoria `Bebidas`.
- **Resultado esperado:** categoria cadastrada e disponível para consulta.
- **Resultado observado informado:** cadastro de `Bebidas` realizado com sucesso.
- **Situação:** **APROVADO (relato de execução manual)**.
- **Data/hora exata e commit testado:** **a confirmar**.
- **Evidência a anexar:** `evidencias/CT02-categoria-bebidas.png`.

### CT03 — Cadastro de produto

- **Requisito:** RF05.
- **Pré-condições:** aplicação e MySQL disponíveis; categoria de exemplo cadastrada.
- **Passos relatados:** acessar a interface; preencher os dados de um produto; salvar o cadastro.
- **Dados de entrada conhecidos:** nome `Suco de Laranja`; demais campos e valores usados **a confirmar**.
- **Resultado esperado:** produto gravado e disponível no cardápio.
- **Resultado observado informado:** cadastro de `Suco de Laranja` realizado com sucesso.
- **Situação:** **APROVADO (relato de execução manual)**.
- **Data/hora exata e commit testado:** **a confirmar**.
- **Evidência a anexar:** `evidencias/CT03-produto-suco.png`.

### CT04 — Consulta de registros

- **Requisito:** RF05.
- **Pré-condições:** categoria e produto cadastrados.
- **Passos relatados:** consultar os registros na aplicação.
- **Entrada:** registros `Bebidas` e `Suco de Laranja` previamente cadastrados.
- **Resultado esperado:** registros apresentados na consulta.
- **Resultado observado informado:** registros puderam ser consultados.
- **Situação:** **APROVADO (relato de execução manual)**.
- **Data/hora exata e commit testado:** **a confirmar**.
- **Evidência a anexar:** `evidencias/CT04-consulta-cardapio.png`.

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
