O MVP será uma versão reduzida do sistema capaz de realizar o fluxo principal de atendimento do restaurante, desde o acesso ao sistema até o fechamento da conta.

| Funcionalidade | Classificação | Faz parte do MVP? | Justificativa |
|---|---|---|---|
| Autenticação | Indispensável | Sim | Necessária para iniciar o uso do sistema pelos usuários internos. |
| Controle de acesso por perfil | Indispensável | Sim | Necessário para limitar as funcionalidades conforme o perfil. |
| Controle de mesas | Indispensável | Sim | Os pedidos são vinculados às mesas. |
| Cardápio | Indispensável | Sim | Necessário para selecionar os produtos. |
| Registro de pedidos | Indispensável | Sim | Representa uma das operações centrais do restaurante. |
| Acesso do cliente pelo número da mesa | Indispensável | Sim | Permite que o cliente consulte o cardápio e realize pedidos. |
| Verificação de ingredientes | Indispensável | Sim | Evita confirmar pedidos sem ingredientes disponíveis. |
| Reserva de ingredientes | Indispensável | Sim | Garante a disponibilidade dos ingredientes após a confirmação. |
| Interface da cozinha | Indispensável | Sim | Permite que a cozinha visualize e atualize os pedidos. |
| Atualização dos status | Indispensável | Sim | Permite acompanhar o pedido durante o preparo e entrega. |
| Baixa dos ingredientes | Indispensável | Sim | Mantém o estoque atualizado quando o pedido fica pronto. |
| Fechamento da conta | Indispensável | Sim | Permite finalizar o atendimento. |
| Registro de pagamento | Indispensável | Sim | Necessário para concluir a venda. |
| Cadastro de clientes | Desejável | Não | Pode ser desenvolvido após o fluxo principal. |
| Controle completo de estoque | Desejável | Não | Pode ser ampliado em uma versão posterior. |
| Descontos e estornos | Desejável | Não | Não são necessários para validar o fluxo principal. |
| Relatórios básicos | Desejável | Não | Podem ser adicionados após a validação do fluxo principal. |
| Histórico de alterações | Desejável | Não | Pode ser ampliado posteriormente. |
| Aplicativo mobile nativo | Futura | Não | Está fora da primeira versão definida no projeto. |
| Delivery próprio | Futura | Não | Está fora da primeira versão. |
| Integração com plataformas de delivery | Futura | Não | Está fora do escopo atual. |
| Integração com bancos/gateways | Futura | Não | Está fora do escopo atual. |
| Emissão de documentos fiscais eletrônicos | Futura | Não | Está fora do escopo atual. |

A primeira entrega já define como fora do projeto, neste momento, o aplicativo mobile nativo, delivery próprio, integrações com plataformas de delivery, bancos/gateways e emissão de documentos fiscais eletrônicos.

## 8.1 Fluxo completo do MVP

O fluxo escolhido para representar o MVP será:

**Autenticar usuário → Selecionar mesa → Consultar cardápio → Selecionar produtos → Verificar disponibilidade dos ingredientes → Confirmar pedido → Reservar ingredientes → Enviar para cozinha → Preparar pedido → Marcar como pronto → Entregar pedido → Registrar pagamento → Encerrar conta.**

### Requisitos envolvidos

RF02, RF03, RF05, RF08, RF09, RF10, RF11, RF12, RF13, RF14, RF17 e RF18.

### Principais entidades envolvidas

- Usuario
- Perfil
- Mesa
- Conta
- Produto
- Categoria
- Ingrediente
- ProdutoIngrediente
- Pedido
- ItemPedido
- ReservaIngrediente
- Pagamento

Esse fluxo utiliza as principais operações previstas para pedidos, disponibilidade e reserva de ingredientes, acompanhamento da cozinha e pagamento.

---

## Voltar Para:

[**Sistema de Gestão de Restaurante**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante)
