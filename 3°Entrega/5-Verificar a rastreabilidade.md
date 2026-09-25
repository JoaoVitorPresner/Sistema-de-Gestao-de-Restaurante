A tabela abaixo relaciona os requisitos selecionados para o MVP com as funcionalidades, entidades, telas e sua participação no MVP.

| Requisito | Caso de uso / funcionalidade | Entidades | Tela | No MVP? |
|---|---|---|---|---|
| RF02 | Autenticar usuário | Usuario | Login | Sim |
| RF03 | Controlar acesso | Usuario, Perfil | Tela inicial | Sim |
| RF05 | Consultar cardápio | Produto, Categoria | Cardápio | Sim |
| RF08 | Registrar pedido | Pedido, ItemPedido, Mesa, Conta | Novo pedido | Sim |
| RF09 | Realizar pedido pela mesa | Cliente, Mesa, Pedido | Cardápio da mesa | Sim |
| RF10 | Acompanhar pedido | Pedido | Acompanhamento | Sim |
| RF11 | Gerenciar pedidos da cozinha | Pedido, ItemPedido | Cozinha | Sim |
| RF12 | Verificar disponibilidade | Ingrediente, ProdutoIngrediente | Novo pedido | Sim |
| RF13 | Reservar ingredientes | ReservaIngrediente, Ingrediente, Pedido | Novo pedido | Sim |
| RF14 | Baixar ingredientes | Ingrediente, MovimentacaoEstoque | Estoque | Sim |
| RF17 | Fechar venda | Conta, Pedido | Fechamento | Sim |
| RF18 | Registrar pagamento | Pagamento, Conta | Pagamento | Sim |

Os demais requisitos continuam fazendo parte do sistema, porém não são necessários para o fluxo mínimo escolhido para o MVP.

Dessa forma, os artefatos do projeto ficam relacionados entre si:

**Requisitos → Funcionalidades → Entidades → Telas → MVP**

O fluxo principal começa na autenticação, passa pelo atendimento e pedido, utiliza a verificação e reserva dos ingredientes, segue para a cozinha e termina com o pagamento e encerramento da conta.

---

## Voltar Para:

[**Sistema de Gestão de Restaurante**](https://github.com/JoaoVitorPresner/Sistema-de-Gestao-de-Restaurante)
