# Backlog e Planejamento — Sistema de Gestão de Restaurante

**Disciplina:** Fábrica de Software — 2º bimestre de 2026  
**Entrega de referência:** 06/11/2026  
**Responsável pela organização do backlog:** Matheusborgges  
**Repositório:** https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante  
**Branch:** `entrega-06-11-2026`  
**Versão:** proposta atualizada para revisão da equipe.

## 1. Objetivo e escopo

O objetivo deste documento é acompanhar a evolução do Sistema de Gestão de Restaurante durante o segundo bimestre, registrando tarefas, responsáveis, prioridades, prazos, resultados e pendências.

A entrega de 06/11 representa a continuidade da implementação iniciada em 20/10, mantendo o **RF05 — Cardápio** como recorte principal.

As principais evoluções foram a edição de produtos, a desativação lógica e a formalização dos testes.

A conclusão integral do MVP permanece como objetivo das próximas etapas, especialmente da entrega final de 27/11/2026.

## 2. Equipe e responsabilidades

| Integrante | Responsabilidade |
|---|---|
| Guilherme20028 | Documentação técnica, requisitos e arquitetura |
| Matheusborgges | Backlog, planejamento e acompanhamento |
| MateusPresner | Testes, evidências e registro de defeitos |
| JoaoVitorPresner | README, GitHub, integração e implementação inicial do RF05 |

As atribuições não representam confirmação automática de conclusão ou aprovação dos documentos.

## 3. Critérios de priorização

**Alta:** necessária para demonstrar, executar, testar ou documentar adequadamente a entrega.

**Média:** consolida o RF05 e prepara as próximas funcionalidades.

**A definir:** depende de revisão dos requisitos e decisão da equipe.

**Estados utilizados:**

- **Concluída:** execução comprovada por código, teste ou documento.
- **Parcialmente concluída:** parte do critério foi atendida.
- **A fazer:** não existe execução confirmada.
- **Pendente de validação:** requer revisão ou aprovação da equipe.

## 4. Histórico das tarefas da primeira entrega

| ID | Tarefa | Situação conhecida até 06/11 |
|---|---|---|
| T01 | Estrutura inicial de categorias e produtos | Concluída |
| T02 | Cadastro, consulta e persistência | Concluída para o recorte demonstrado |
| T03 | Testes automatizados iniciais | Dois testes aprovados |
| T04 | Documentação técnica | Documento de 20/10 disponível; revisão de 06/11 pendente |
| T05 | Backlog e planejamento | Documento anterior disponível; atualização pendente de validação |
| T06 | Testes manuais e evidências | CT06 e CT07 documentados em 06/11 |
| T07 | Atualização do README | Concluída |
| T08 | Revisão do repositório e integração | Branch de 06/11 sincronizada; merge na `main` não confirmado |
| T09 | Roteiro de demonstração | Ensaio não confirmado |

O histórico original de 20/10 permanece preservado no documento da primeira entrega.

## 5. Acompanhamento das tarefas até 06/11

| ID | Tarefa | Responsável | Prioridade | Situação |
|---|---|---|---|---|
| T10 | Verificar e corrigir edição de categorias/produtos | JoaoVitorPresner | Alta | Parcialmente concluída |
| T11 | Verificar e corrigir exclusão de categorias/produtos | JoaoVitorPresner | Alta | Parcialmente concluída |
| T12 | Testar entradas inválidas e respostas de erro | MateusPresner | Média | Parcialmente verificada |
| T13 | Revisar integração entre interface, API e banco | MateusPresner / JoaoVitorPresner | Alta | Concluída para CT06 e CT07 |
| T14 | Priorizar requisitos restantes do MVP | Guilherme20028 / Matheusborgges | Média | A fazer |
| T15 | Atualizar documentação, backlog e README | Equipe conforme responsabilidades | Alta | Parcialmente concluída |
| T16 | Preparar demonstração de 06/11 | Equipe inteira | Alta | Pendente de confirmação |

### T10 — Edição de categorias e produtos

**Resultado:** a edição de produtos foi implementada e aprovada no teste CT06.

A alteração de categorias ainda não foi comprovada.

**Situação:** parcialmente concluída.

**Evidências:**

- `EV06-Edicao-Produto.png`
- `EV06-Produto-Atualizado.png`

### T11 — Exclusão de categorias e produtos

**Resultado:** foi implementada a desativação lógica de produtos, utilizando `ativo=false`.

O teste CT07 confirmou o comportamento e a permanência do estado inativo após atualizar a interface.

Não foi comprovada a exclusão física de produtos nem a exclusão de categorias.

**Situação:** parcialmente concluída.

**Evidências:**

- `EV07-Produto-Desativado.png`
- `EV07-Persistencia-Desativacao.png`

### T12 — Testes de entradas inválidas

O teste automatizado `rejeitaProdutoSemPreco` foi aprovado.

Entretanto, não há confirmação de uma bateria abrangente de testes manuais envolvendo entradas inválidas, mensagens de erro e regras de integridade.

**Situação:** parcialmente verificada.

### T13 — Integração entre interface, API e banco

Os fluxos de edição e desativação foram executados pela interface web, com atualização dos dados persistidos.

**Situação:** concluída para os fluxos CT06 e CT07, sem implicar validação de todos os fluxos do sistema.

### T14 — Revisão dos demais requisitos

Ainda é necessário revisar os requisitos restantes do primeiro bimestre, identificar dependências e definir prioridades de implementação.

**Situação:** a fazer, sem execução confirmada.

### T15 — Atualização da documentação

O README foi atualizado e enviado ao GitHub.

O documento de testes e as quatro evidências também foram incluídos na branch de 06/11.

A documentação técnica e o backlog atualizados ainda precisam ser revisados e integrados ao repositório.

**Situação:** parcialmente concluída.

### T16 — Demonstração

A aplicação possui funcionalidades demonstráveis de cadastro, consulta, edição e desativação de produtos.

Ainda é necessário confirmar a realização de um ensaio conjunto pela equipe.

**Situação:** pendente de confirmação.

## 6. Evidências da entrega

| Item | Evidência |
|---|---|
| Implementação inicial | Commit `d45857a` |
| Edição e desativação de produtos | Commit `c952fef` |
| README e evidências | Commit `e2f8c3f` |
| Testes automatizados | 2 testes, 0 falhas, 0 erros |
| Teste manual CT06 | Edição de produto aprovada |
| Teste manual CT07 | Desativação lógica aprovada |
| Controle de versão | Branch `entrega-06-11-2026` sincronizada |

O commit `e2f8c3f` registra o documento de testes e as quatro imagens.

## 7. Planejamento até a entrega final de 27/11

| Atividade | Responsável proposto | Critério de conclusão |
|---|---|---|
| Revisar o comportamento de exclusão do RF05 | Equipe | Decisão registrada |
| Verificar operações pendentes de categorias | Implementação a distribuir | Operações testadas e documentadas |
| Ampliar testes negativos | MateusPresner | Casos executados e resultados registrados |
| Priorizar requisitos restantes | Matheusborgges / Guilherme20028 | Backlog revisado |
| Atualizar documentação e rastreabilidade | Guilherme20028 | Documento validado |
| Conferir integração e Pull Request | JoaoVitorPresner | Situação registrada |
| Preparar apresentação final | Equipe inteira | Demonstração ensaiada |

Novas implementações de outros requisitos ainda dependem de distribuição pela equipe.

## 8. Dependências e riscos

| Risco | Consequência | Ação preventiva |
|---|---|---|
| Desativação lógica não atender à exclusão exigida | RF05 parcialmente aceito | Confirmar regra com a professora |
| Alteração/exclusão de categorias não verificadas | Funcionalidade incompleta | Inspecionar e testar |
| Testes negativos insuficientes | Defeitos não identificados | Ampliar cobertura |
| MySQL não configurado | Falha na demonstração | Testar ambiente antecipadamente |
| Versão Java incompatível | Problemas de execução | Utilizar Java 17 |
| Documentos não revisados | Inconsistência na entrega | Validar com responsáveis |
| Branch não integrada à `main` | Material pode não aparecer na principal | Conferir PR e acesso |
| Escopo amplo até 27/11 | Risco de atraso | Priorizar requisitos essenciais |

## 9. Rotina de acompanhamento

1. Cada integrante registra a situação das tarefas sob sua responsabilidade.
2. Matheusborgges consolida o backlog e revisa as prioridades.
3. Guilherme20028 verifica a consistência da documentação técnica.
4. MateusPresner registra resultados e evidências dos testes.
5. JoaoVitorPresner verifica a integração e o versionamento.
6. Problemas identificados devem ser documentados e corrigidos antes da validação final.

## 10. Checklist de aceite da entrega de 06/11

- [x] Projeto e repositório preservados.
- [x] RF05 ampliado com edição de produtos.
- [x] Desativação lógica de produtos implementada.
- [x] CT06 executado e aprovado.
- [x] CT07 executado e aprovado para soft delete.
- [x] Quatro evidências registradas.
- [x] Dois testes automatizados aprovados.
- [x] README atualizado.
- [x] Código e evidências enviados ao GitHub.
- [x] Branch de 06/11 sincronizada.
- [ ] Documentação técnica de 06/11 revisada e integrada.
- [ ] Backlog de 06/11 revisado e integrado.
- [ ] Validação dos registros de testes pelo responsável.
- [ ] Acesso da professora e situação do PR confirmados.
- [ ] Demonstração ensaiada pela equipe.
- [ ] Decisão sobre a aceitação da desativação lógica registrada.

## 11. Referências internas

- Documentos de requisitos e modelagem do primeiro bimestre.
- `2-Bimestre/Entrega-20-10/documentacao/Documentacao-Tecnica-Entrega-20-10.md`
- Backlog e planejamento da primeira entrega do segundo bimestre.
- `2-Bimestre/Entrega-20-10/implementacao/`
- `2-Bimestre/Entrega-06-11/documentacao/Testes-e-Evidencias-06-11.md`
- `2-Bimestre/Entrega-06-11/documentacao/Documentacao-Tecnica-Entrega-06-11.md`
- `README.md`

## 12. Conclusão

A entrega de 06/11 demonstra avanços na implementação e na validação do RF05.

Foram concluídas a edição de produtos e a desativação lógica, acompanhadas de testes manuais e evidências.

A documentação e o README também evoluíram, permitindo maior rastreabilidade entre código, testes e requisitos.

Entretanto, algumas operações do RF05 e outros requisitos do sistema permanecem pendentes de implementação ou verificação.

O planejamento até 27/11 deverá priorizar essas pendências e preparar a apresentação final.

---

**Nota de controle:** as situações descritas refletem as evidências conhecidas até 06/11. Matheusborgges deve validar as prioridades e os prazos; os demais responsáveis devem confirmar os resultados de suas respectivas áreas antes do envio definitivo.