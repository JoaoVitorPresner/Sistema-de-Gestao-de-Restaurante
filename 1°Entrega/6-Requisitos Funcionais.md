# 6. Requisitos Funcionais

| **Código** | **Categoria** | **Prioridade** | **Requisito funcional** |
|---|---|---|---|
| **RF01** | Usuários | Alta | O sistema deve permitir o cadastro de usuários internos e a definição de seus respectivos perfis de acesso. |
| **RF02** | Autenticação | Alta | O sistema deve permitir que usuários internos realizem autenticação por login e senha. |
| **RF03** | Segurança | Alta | O sistema deve controlar o acesso às funcionalidades de acordo com o perfil do usuário autenticado. |
| **RF04** | Clientes | Média | O sistema deve permitir o cadastro, consulta, alteração e exclusão de clientes. |
| **RF05** | Cardápio | Alta | O sistema deve permitir o cadastro, consulta, alteração e exclusão de produtos e categorias do cardápio. |
| **RF06** | Estoque | Alta | O sistema deve permitir o cadastro e gerenciamento de ingredientes e itens controlados pelo estoque. |
| **RF07** | Estoque / Produtos | Alta | O sistema deve permitir associar ingredientes aos produtos e definir as quantidades necessárias para sua preparação. |
| **RF08** | Pedidos / Mesas | Alta | O sistema deve permitir o registro e gerenciamento de pedidos vinculados a uma mesa. |
| **RF09** | Pedidos / Cliente | Alta | O sistema deve permitir ao cliente acessar o cardápio e realizar pedidos utilizando a identificação da mesa. |
| **RF10** | Pedidos | Alta | O sistema deve permitir o acompanhamento dos pedidos e a atualização de seus status entre Recebido, Em preparo, Pronto, Entregue e Cancelado, quando aplicável. |
| **RF11** | Cozinha | Alta | O sistema deve disponibilizar uma interface para a equipe da cozinha visualizar e atualizar os pedidos. |
| **RF12** | Estoque / Pedidos | Alta | O sistema deve verificar a disponibilidade dos ingredientes antes da confirmação de um pedido. |
| **RF13** | Estoque / Pedidos | Alta | O sistema deve reservar os ingredientes necessários quando um pedido for confirmado e liberar a reserva quando o pedido for cancelado antes do preparo. |
| **RF14** | Estoque / Pedidos | Alta | O sistema deve realizar a baixa dos ingredientes utilizados quando o pedido atingir o status Pronto. |
| **RF15** | Estoque | Alta | O sistema deve permitir o registro e consulta das movimentações de entrada e saída do estoque. |
| **RF16** | Estoque | Média | O sistema deve informar quando a quantidade disponível de um item atingir ou ficar abaixo do limite mínimo definido. |
| **RF17** | Vendas | Alta | O sistema deve permitir o fechamento de vendas e calcular automaticamente o valor total da conta. |
| **RF18** | Pagamentos | Alta | O sistema deve permitir o registro de pagamentos em dinheiro, cartão e Pix, inclusive utilizando mais de uma modalidade na mesma conta. |
| **RF19** | Vendas / Permissões | Alta | O sistema deve permitir a aplicação de descontos e o estorno de vendas conforme as permissões dos usuários. |
| **RF20** | Vendas / Relatórios | Média | O sistema deve emitir comprovantes de venda não fiscais e gerar relatórios básicos de vendas por período. |
| **RF21** | Auditoria | Média | O sistema deve registrar o histórico de alterações relevantes realizadas no sistema, incluindo alterações de status dos pedidos e movimentações de estoque. |
