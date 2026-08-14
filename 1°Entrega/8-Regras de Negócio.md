| **Código** | **Regra de negócio**                                                                                                                                                         |
| ---------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **RN01**   | Cada usuário interno poderá possuir apenas um perfil de acesso ativo por vez.                                                                                                |
| **RN02**   | Apenas Gerente ou Administrador poderá aplicar descontos superiores a 10% sobre o valor da conta.                                                                            |
| **RN03**   | O estorno de uma venda finalizada somente poderá ser realizado por Gerente ou Administrador e exigirá o registro de uma justificativa.                                       |
| **RN04**   | Um pedido enviado para a cozinha não poderá ser cancelado livremente pelo cliente ou atendente; cancelamentos excepcionais dependerão das permissões definidas pelo sistema. |
| **RN05**   | Uma mesa ocupada ou aguardando pagamento não poderá receber um novo atendimento inicial até ser liberada.                                                                    |
| **RN06**   | Uma conta somente poderá ser encerrada quando todos os pedidos vinculados estiverem Entregues ou Cancelados e o valor total estiver integralmente pago.                      |
| **RN07**   | Uma conta poderá utilizar mais de uma modalidade de pagamento, desde que a soma dos pagamentos corresponda ao valor total devido.                                            |
| **RN08**   | O sistema não poderá permitir que a quantidade física de um item de estoque fique abaixo de zero.                                                                            |
| **RN09**   | Antes da confirmação de um pedido, o sistema deverá verificar se existem ingredientes suficientes para sua preparação.                                                       |
| **RN10**   | Quando um pedido for confirmado, os ingredientes necessários deverão ser reservados para evitar que sejam utilizados na confirmação de outro pedido.                         |
| **RN11**   | Quando um pedido atingir o status Pronto, os ingredientes correspondentes deverão ser baixados do estoque físico.                                                            |
| **RN12**   | Se um pedido for cancelado antes do início do preparo, os ingredientes reservados deverão ser liberados.                                                                     |
| **RN13**   | Todo produto que utilizar ingredientes controlados pelo estoque deverá possuir sua composição cadastrada com os respectivos ingredientes e quantidades.                      |
| **RN14**   | O acesso do cliente vinculado a uma mesa deverá permitir somente operações relacionadas àquela mesa enquanto ela estiver ativa.                                              |
