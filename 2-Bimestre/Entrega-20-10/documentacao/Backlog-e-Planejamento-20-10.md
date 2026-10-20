# Backlog e Planejamento — Sistema de Gestão de Restaurante

**Disciplina:** Fábrica de Software — 2º bimestre de 2026  
**Entrega de referência:** 20/10/2026  
**Responsável pela organização do backlog:** Matheusborgges  
**Repositório:** https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante  
**Versão deste documento:** proposta para revisão da equipe em 20/10/2026.

## 1. Objetivo e escopo

Continuar o projeto e os artefatos do primeiro bimestre, com uma implementação inicial executável e demonstrável em 20/10, evolução e integração até 06/11 e conclusão/apresentação final em 27/11. O recorte priorizado para 20/10 é o **RF05 — Cardápio**, envolvendo produtos e categorias, interface web, API e persistência em MySQL.

**Atenção:** o recorte não equivale à conclusão de todo o MVP. Outros requisitos do primeiro bimestre permanecem válidos e devem ser reavaliados com seus identificadores originais antes de receberem tarefas detalhadas. Este documento não presume que operações ainda não verificadas foram aprovadas.

## 2. Equipe e responsabilidades combinadas

| Integrante (GitHub) | Responsabilidade principal |
|---|---|
| Guilherme20028 | Documentação técnica: requisitos, arquitetura, diagramas e mudanças desde o 1º bimestre |
| Matheusborgges | Backlog, priorização, prazos, acompanhamento e plano até 06/11 |
| MateusPresner | Casos de teste, resultados, evidências e defeitos |
| JoaoVitorPresner | README, revisão do repositório, Pull Request e integração; implementação inicial do RF05 já enviada |

As responsabilidades acima são áreas de trabalho, não confirmação automática de que todas as atividades estejam concluídas.

## 3. Critérios de priorização

- **Alta:** necessária para demonstrar o recorte de 20/10, reproduzir o ambiente, comprovar testes ou identificar a entrega.
- **Média:** consolida o RF05 e permite fluxos integrados para 06/11.
- **A definir:** funcionalidades de outros requisitos, cuja ordem depende de revisão do escopo do primeiro bimestre e de decisão da equipe.

**Situações:** `Concluída` = há evidência objetiva; `Em andamento` = iniciada, mas falta aceitação; `A fazer` = não há execução confirmada; `A verificar` = implementação ou resultado precisa ser inspecionado.

## 4. Backlog da entrega de 20/10

| ID | Tarefa / requisito | Responsável | Prioridade | Prazo | Situação em 20/10 | Critério verificável de conclusão |
|---|---|---|---|---|---|---|
| T01 | Implementar estrutura inicial de categorias e produtos — RF05 | JoaoVitorPresner | Alta | 20/10 | Concluída (código enviado) | Código-fonte e configuração presentes no repositório; commit identificado |
| T02 | Disponibilizar cadastro e consulta de categoria/produto com persistência — RF05 | JoaoVitorPresner | Alta | 20/10 | Concluída para o recorte testado | Criar categoria e produto, consultá-los e confirmar persistência após reiniciar a aplicação |
| T03 | Executar testes automatizados iniciais — RF05 | JoaoVitorPresner (execução já realizada); MateusPresner (registro) | Alta | 20/10 | Execução concluída; registro pendente | Relatório registra 2 testes, 0 falhas e 0 erros em Java 17, com comando e versão |
| T04 | Revisar requisitos, decisões técnicas e arquitetura — documentação | Guilherme20028 | Alta | 20/10 | Documento enviado; revisão final pendente | Documento técnico consistente com o código e aprovado pela equipe |
| T05 | Elaborar backlog com responsáveis, prioridades, prazos, situação e aceite — planejamento | Matheusborgges | Alta | 20/10 | Em andamento | Backlog revisado e versionado no repositório |
| T06 | Registrar casos de teste manuais e evidências reais — RF05 | MateusPresner | Alta | 20/10 | A fazer | Cada caso contém ID, condições, passos, entradas, esperado, observado, status, data, versão e evidência |
| T07 | Atualizar README com execução e configuração — ambiente | JoaoVitorPresner | Alta | 20/10 | A fazer | Outro integrante consegue seguir as instruções, sem senhas versionadas |
| T08 | Conferir Pull Request, arquivos e identificação da versão da entrega — integração | JoaoVitorPresner | Alta | 20/10 | Em andamento | PR revisado, documentação e código presentes; commit/tag final anotado; acesso da professora conferido |
| T09 | Preparar roteiro de demonstração e verificar execução em conjunto — apresentação | Equipe inteira (coordenação: Matheusborgges) | Alta | 20/10 | A fazer | Demonstração percorre cadastro, consulta, persistência, testes, pendências e próximos passos |

### Evidências já conhecidas em 20/10

- Implementação inicial do RF05 enviada no commit `d45857a` da branch `entrega-20-10-2026`.
- Documento técnico enviado no commit `e5cfcaa` da mesma branch.
- Testes automatizados executados localmente com Java 17: **2 executados, 0 falhas, 0 erros, 0 ignorados; BUILD SUCCESS**. A formalização das evidências é responsabilidade de MateusPresner.
- Cadastro de categoria **Bebidas** e produto **Suco de Laranja**, consulta e persistência após reiniciar a aplicação foram verificados manualmente. Ainda faltam registros formais desses casos.
- Branch remota sincronizada e árvore de trabalho limpa após o envio de `e5cfcaa`. **Isso não comprova que o Pull Request foi mesclado à `main`.**

## 5. Pendências e plano até 06/11

Os prazos abaixo são **marcos propostos pela equipe**, não datas intermediárias impostas pela professora. Devem ser confirmados na revisão do backlog.

| ID | Tarefa / requisito | Responsável proposto | Prioridade | Prazo proposto | Situação | Critério de aceitação |
|---|---|---|---|---|---|---|
| T10 | Verificar e, se necessário, corrigir edição de categorias/produtos — RF05 | JoaoVitorPresner (a confirmar disponibilidade) | Média | 30/10 | A verificar | Editar registro, consultar novo valor e confirmar persistência |
| T11 | Verificar e, se necessário, corrigir exclusão de categorias/produtos — RF05 | JoaoVitorPresner (a confirmar disponibilidade) | Média | 30/10 | A verificar | Exclusão conforme regras de integridade; comportamento observado registrado |
| T12 | Testar entradas inválidas, campos obrigatórios e respostas de erro — RF05 / regras aplicáveis | MateusPresner (testes); implementação a distribuir | Média | 03/11 | A fazer | Casos negativos executados e eventuais defeitos registrados |
| T13 | Revisar integração entre interface, API e banco e repetir testes — RF05 | MateusPresner (testes); JoaoVitorPresner (integração) | Média | 05/11 | A fazer | Fluxos integrados executados e evidenciados na versão da entrega |
| T14 | Revisar requisitos restantes e priorizar o próximo fluxo do MVP — identificadores originais do 1º bimestre | Guilherme20028 e Matheusborgges | Média | 27/10 | A fazer | Lista priorizada com IDs originais, dependências e critérios de aceite |
| T15 | Atualizar backlog, documentação e README conforme a evolução — gestão da entrega | Matheusborgges, Guilherme20028 e JoaoVitorPresner | Média | 06/11 | A fazer | Documentos coerentes com funcionalidades realmente executadas |
| T16 | Preparar demonstração de sistema parcialmente funcional — entrega 06/11 | Equipe inteira | Média | 06/11 | A fazer | Demonstração de fluxos integrados, regras de negócio e testes registrados |

**Não estão atribuídas aqui novas implementações de outros requisitos**, porque a divisão confirmada pela equipe diz respeito às áreas de documentação, planejamento, testes e integração. A equipe deve deliberar a distribuição das funcionalidades restantes.

## 6. Marco de conclusão — 27/11

- Finalizar o produto dentro do escopo validado com a professora.
- Executar testes finais e registrar defeitos/correções.
- Consolidar rastreabilidade entre requisitos, código e testes.
- Conferir instruções de instalação, reprodutibilidade e acesso ao repositório.
- Preparar apresentação final com participação dos quatro integrantes.

## 7. Dependências, riscos e decisões pendentes

| Dependência ou risco | Consequência | Ação preventiva |
|---|---|---|
| README ainda não atualizado | Dificuldade para reproduzir a execução | Priorizar T07 e testar as instruções em outro computador |
| Evidências manuais ainda não formalizadas | Resultado pode não ser verificável na avaliação | Executar T06 com data, versão e capturas reais |
| Java 26 causou falhas no ambiente de testes | Execução inconsistente entre máquinas | Padronizar Java 17 e documentar a versão |
| MySQL exige configuração local | Falha de conexão na demonstração | Conferir serviço, banco e variáveis de ambiente antes da apresentação |
| PR ainda não confirmado como mesclado | Material pode não estar na branch principal | Revisar e integrar somente após conferência dos arquivos |
| Alteração e exclusão ainda não verificadas | RF05 não pode ser declarado integralmente validado | Executar T10–T12 e registrar resultados |
| Escopo dos demais requisitos não priorizado aqui | Risco de atrasar a conclusão | Executar T14 com documentos do primeiro bimestre |

## 8. Rotina de acompanhamento e atualização

1. Cada responsável atualiza a situação da própria tarefa e anexa link para commit, arquivo ou teste.
2. Matheusborgges revisa prioridades, dependências e bloqueios com a equipe.
3. Testes reprovados geram registro de defeito, responsável e nova execução após correção.
4. JoaoVitorPresner verifica conflitos e integração antes de mesclar o Pull Request.
5. Após o retorno da professora em 20/10, revisar prazos e escopo para 06/11.

## 9. Checklist de aceite da entrega de 20/10

- [x] Projeto e repositório do primeiro bimestre mantidos.
- [x] Recorte RF05 iniciado e código enviado à branch da entrega.
- [x] Documento técnico enviado à branch da entrega (revisão final pendente).
- [x] Dois testes automatizados executados com sucesso localmente.
- [ ] Backlog revisado pela equipe e enviado ao GitHub.
- [ ] README atualizado com instalação e execução.
- [ ] Casos de teste e evidências formais incluídos.
- [ ] Versão final da entrega identificada e acesso da professora conferido.
- [ ] Pull Request revisado e integração à branch principal confirmada.
- [ ] Roteiro de demonstração ensaiado com dados fictícios.

## 10. Referências internas

- Documentos e requisitos do primeiro bimestre existentes no repositório.
- `2-Bimestre/Entrega-20-10/documentacao/Documentacao-Tecnica-Entrega-20-10.md`.
- `2-Bimestre/Entrega-20-10/implementacao/` — implementação inicial do RF05.
- Manual da primeira entrega do segundo bimestre, Fábrica de Software, professora Sabine Cassol, 20/10/2026.

> **Nota de controle:** as situações refletem somente as confirmações disponíveis até a preparação deste arquivo. A equipe deve ajustar datas propostas, responsáveis de implementação futura e estados conforme evidências reais, antes da entrega.
