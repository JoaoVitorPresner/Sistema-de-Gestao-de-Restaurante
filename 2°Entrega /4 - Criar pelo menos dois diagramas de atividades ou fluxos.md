# 4. Diagramas de Atividades

Nesta etapa foram desenvolvidos dois diagramas de atividades para representar o fluxo das principais operações do **Sistema de Gestão de Restaurante**.

Os processos escolhidos foram:

1. **Realizar pedido**
2. **Processar pedido na cozinha**

Os diagramas permitem visualizar a sequência das atividades, decisões e alterações realizadas pelo sistema durante a execução desses processos.

---

## 4.1 Diagrama de Atividades — Realizar Pedido

O primeiro diagrama representa o processo de registro de um pedido, desde a identificação da mesa e seleção dos produtos até a disponibilização do pedido para a cozinha.

Antes da confirmação, o sistema verifica a disponibilidade dos ingredientes necessários. Caso não exista quantidade suficiente, o pedido não poderá ser confirmado.

Quando os ingredientes estiverem disponíveis, o pedido será registrado e os ingredientes necessários serão reservados.

### Fluxo do processo

1. O sistema identifica a mesa ou o atendente seleciona a mesa.
2. Os produtos desejados são selecionados.
3. O sistema verifica a disponibilidade dos ingredientes.
4. O sistema verifica se existem ingredientes suficientes.
5. Caso os ingredientes sejam insuficientes, o sistema informa a indisponibilidade e encerra o processo.
6. Caso existam ingredientes suficientes, o pedido é confirmado.
7. O sistema registra o pedido com status **Recebido**.
8. O sistema reserva os ingredientes necessários.
9. O pedido é disponibilizado para a cozinha.
10. O processo é encerrado.

### Representação simplificada do fluxo

```text
Início
  │
  ▼
Identificar/Selecionar mesa
  │
  ▼
Selecionar produtos
  │
  ▼
Verificar disponibilidade dos ingredientes
  │
  ▼
┌───────────────────────────┐
│ Ingredientes suficientes? │
└─────────────┬─────────────┘
              │
        ┌─────┴─────┐
        │           │
      Não          Sim
        │           │
        ▼           ▼
Informar         Confirmar
indisponibilidade pedido
        │           │
        ▼           ▼
       Fim       Registrar pedido
                 com status Recebido
                     │
                     ▼
               Reservar ingredientes
                     │
                     ▼
               Disponibilizar pedido
                  para a cozinha
                     │
                     ▼
                    Fim
