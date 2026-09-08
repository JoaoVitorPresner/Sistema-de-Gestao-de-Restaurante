# 1. Revisão da Entrega 1

Nesta etapa, foi realizada a revisão da documentação desenvolvida na primeira entrega do projeto **Sistema de Gestão de Restaurante**.

A revisão teve como objetivo melhorar a clareza e a consistência da documentação, corrigindo problemas de ortografia, concordância, pontuação e padronização de termos. A numeração dos requisitos funcionais, requisitos não funcionais e regras de negócio foi preservada para manter a rastreabilidade entre as entregas.

Também foi considerada a entidade **Conta**, utilizada para representar o agrupamento financeiro dos pedidos vinculados a uma mesa e os pagamentos realizados.

---

## 1.1 Identificação do Projeto

**Tema:** Sistema para Gestão de Restaurante ou Lanchonete

### Equipe

- João Vitor Barros Presner
- Guilhermy Martineli Brenny
- Matheus Borges
- Mateus Henrique Presner

### Descrição

O Sistema de Gestão de Restaurante será uma aplicação web destinada a auxiliar restaurantes no gerenciamento de suas principais atividades. O sistema permitirá o controle de usuários, clientes, produtos, categorias, mesas, pedidos, estoque, vendas e pagamentos.

Também possibilitará o acompanhamento dos pedidos entre o atendimento e a cozinha, além da consulta de informações e relatórios básicos. O acesso às funcionalidades será controlado de acordo com o perfil de cada usuário, contribuindo para uma gestão mais organizada e eficiente.

---

## 1.2 Contexto e Problema

### Situação que motivou a criação do sistema

Restaurantes precisam administrar diversas atividades simultaneamente, como atendimento aos clientes, registro de pedidos, organização das mesas, preparação dos alimentos, controle de estoque, vendas e pagamentos.

Minha equipe notou que, quando essas atividades são realizadas manualmente ou por meio de ferramentas separadas, ocorrem erros no registro dos pedidos, falhas na comunicação entre atendimento e cozinha, atrasos no preparo e dificuldades no controle de estoque e vendas.

A partir disso, surge a necessidade de centralizar essas informações em um único sistema, permitindo integrar as atividades do estabelecimento e facilitar seu gerenciamento.

### Quem enfrenta o problema e como o processo ocorre atualmente

Os principais afetados são os funcionários responsáveis pelo atendimento, cozinha, estoque e gerenciamento, além dos clientes.

Sem um sistema centralizado, o atendente registra o pedido e o encaminha à cozinha, enquanto os cozinheiros precisam acompanhar manualmente os pedidos recebidos, em preparo e prontos. O responsável pelo estoque também precisa controlar entradas, saídas e disponibilidade dos ingredientes, enquanto os responsáveis pela gestão acompanham mesas, vendas e pagamentos por diferentes métodos ou ferramentas.

Devido a esta falta de integração, aumenta a ocorrência de erros, retrabalho, atrasos e perda de informações, fazendo com que o cliente também seja afetado por problemas na comunicação ou pela indisponibilidade de determinados produtos.

### Justificativa

O desenvolvimento do sistema se justifica pela necessidade de centralizar as principais informações e atividades do restaurante, com a integração entre pedidos, cozinha e estoque, facilitando o acompanhamento dessas operações, reduzindo erros de comunicação e auxiliando no controle dos ingredientes utilizados na preparação dos produtos.

Além disso, a utilização de diferentes perfis de acesso permitirá que cada funcionário utilize somente as funcionalidades necessárias às suas atividades, contribuindo para a organização, o gerenciamento e a segurança das informações do estabelecimento.

---

## 1.3 Objetivos

### Objetivo Geral

Desenvolver um sistema web para gerenciar e otimizar as principais operações de um restaurante, incluindo pedidos, mesas, produtos, estoque, vendas, pagamentos e usuários, buscando melhorar a organização e o controle das atividades do estabelecimento.

### Objetivos Específicos

- Permitir o cadastro e gerenciamento de usuários, clientes, produtos e categorias do cardápio.
- Organizar o registro e acompanhamento dos pedidos desde o recebimento até a entrega.
- Disponibilizar recursos para controle de mesas, estoque, vendas e pagamentos.
- Facilitar a comunicação entre atendimento, cozinha e estoque por meio da centralização das informações.
- Desenvolver uma interface intuitiva e responsiva para utilização em diferentes dispositivos.

---

## 1.4 Público-Alvo e Perfis de Usuário

| Perfil | Necessidades | Responsabilidades | Permissões |
|---|---|---|---|
| **Administrador** | Gerenciar integralmente o sistema. | Gerenciar usuários, acessos e informações administrativas. | Acesso às funcionalidades administrativas e de gerenciamento do sistema. |
| **Gerente** | Supervisionar a operação e acompanhar resultados. | Supervisionar funcionários, vendas, estoque e relatórios. | Acessar funções gerenciais e autorizar operações que exigem maior nível de permissão. |
| **Atendente** | Realizar o atendimento e registrar pedidos. | Registrar pedidos, administrar mesas e acompanhar pedidos. | Consultar cardápio, mesas e pedidos; registrar operações permitidas ao seu perfil. |
| **Cozinheiro/Chef** | Organizar os pedidos destinados à cozinha. | Preparar os pedidos e atualizar seus status. | Visualizar pedidos da cozinha e atualizar seus status durante o preparo. |
| **Estoquista** | Controlar os itens armazenados. | Registrar entradas e saídas e acompanhar níveis de estoque. | Consultar e realizar operações relacionadas ao estoque permitidas pelo sistema. |
| **Cliente** | Consultar o cardápio e realizar pedidos. | Selecionar produtos e acompanhar seus próprios pedidos. | Acessar o cardápio e realizar pedidos por meio da identificação da mesa. |

> **Observação:** Na primeira versão do sistema, o cliente não terá uma conta pessoal. Seu acesso será realizado por meio da identificação da mesa e ficará limitado às funcionalidades destinadas ao atendimento presencial. Futuramente, poderão ser implementadas funcionalidades de acesso externo, incluindo um aplicativo mobile nativo e um sistema próprio de delivery.

---

## 1.5 Escopo

### Funcionalidades incluídas na primeira versão

- Gerenciamento de usuários e perfis de acesso.
- Gerenciamento de clientes.
- Gerenciamento de produtos e categorias do cardápio.
- Gerenciamento de ingredientes e itens de estoque.
- Associação de ingredientes aos produtos.
- Controle de mesas.
- Registro e gerenciamento de pedidos.
- Acesso do cliente ao cardápio por meio da mesa.
- Acompanhamento dos pedidos pela equipe.
- Interface para acompanhamento dos pedidos na cozinha.
- Controle básico de estoque e movimentações.
- Verificação e reserva de ingredientes para pedidos.
- Controle de vendas e pagamentos.
- Registro de pagamentos em dinheiro, cartão e Pix.
- Aplicação de descontos conforme as permissões.
- Emissão de comprovante de venda não fiscal.
- Relatórios básicos de vendas.
- Registro de alterações relevantes do sistema.

### Itens fora do escopo

- Aplicativo mobile nativo.
- Sistema próprio de delivery.
- Integração com plataformas de delivery.
- Integração com bancos ou gateways de pagamento.
- Emissão de documentos fiscais eletrônicos.
- Programa de fidelidade.
- Inteligência artificial para previsão de vendas.
- Integrações com sistemas externos de contabilidade ou gestão empresarial.

### Restrições

#### Prazo

O sistema será desenvolvido de forma incremental durante o cronograma estabelecido.

#### Tecnologia

Para o desenvolvimento do Sistema de Gestão de Restaurante serão utilizadas as seguintes tecnologias:

- **HTML5:** utilizado para estruturar as páginas e os elementos da interface da aplicação web.
- **CSS3:** utilizado para estilização e organização visual das páginas.
- **JavaScript:** utilizado para implementar interações e comportamentos dinâmicos na interface.
- **Bootstrap:** utilizado para auxiliar na criação de uma interface responsiva, permitindo a utilização do sistema em computadores, tablets e dispositivos móveis.
- **Java:** linguagem utilizada para o desenvolvimento do back-end e implementação das regras de negócio do sistema.
- **Spring Boot:** framework utilizado para desenvolver a aplicação web e organizar os serviços responsáveis pelas funcionalidades do sistema.
- **Spring Data JPA / Hibernate:** utilizados para realizar o mapeamento e a persistência dos dados entre as classes da aplicação e o banco de dados relacional.
- **Spring Security:** utilizado para implementar autenticação, controle de acesso e permissões de acordo com os diferentes perfis de usuário.
- **API REST:** utilizada para realizar a comunicação entre a interface da aplicação e o back-end.
- **MySQL:** Sistema Gerenciador de Banco de Dados (SGBD) relacional utilizado para armazenar usuários, clientes, produtos, ingredientes, mesas, contas, pedidos, pagamentos, movimentações de estoque e demais informações do sistema.

A aplicação será executada como um **sistema web**, acessível por navegadores modernos, como Google Chrome, Mozilla Firefox e Safari. A interface será responsiva para permitir sua utilização em diferentes tamanhos de tela, incluindo os dispositivos utilizados pela equipe de atendimento e cozinha.

#### Acesso

- Usuários internos deverão realizar autenticação.
- As funcionalidades serão controladas de acordo com o perfil.
- O cliente terá acesso somente às funcionalidades destinadas ao atendimento presencial.

#### Integrações

A primeira versão não possuirá integração com plataformas externas de delivery, sistemas bancários, gateways de pagamento ou emissores fiscais.

---

## 1.6 Requisitos Funcionais

| Código | Categoria | Prioridade | Requisito funcional |
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

---

## 1.7 Requisitos Não Funcionais

| Código | Categoria | Requisito não funcional |
|---|---|---|
| **RNF01** | Segurança | As senhas dos usuários não devem ser armazenadas em texto puro, devendo utilizar mecanismo seguro de hash. |
| **RNF02** | Segurança | O sistema deve encerrar automaticamente a sessão de usuários internos após 15 minutos de inatividade. |
| **RNF03** | Segurança | O sistema deve impedir o acesso de usuários a funcionalidades não autorizadas para seu perfil. |
| **RNF04** | Segurança | O sistema deve restringir o acesso aos dados dos clientes conforme as permissões do usuário. |
| **RNF05** | Usabilidade | As principais telas devem apresentar informações e ações de forma clara e intuitiva para os respectivos perfis de usuário. |
| **RNF06** | Usabilidade | O sistema deve apresentar mensagens de erro e confirmação em linguagem clara, orientando o usuário quando necessário. |
| **RNF07** | Desempenho | As principais operações do sistema devem apresentar resposta em até 3 segundos em condições normais de rede. |
| **RNF08** | Compatibilidade | A aplicação deve funcionar corretamente nos principais navegadores modernos, como Chrome, Firefox e Safari. |
| **RNF09** | Responsividade | A interface deve adaptar-se a computadores, tablets e smartphones, considerando telas a partir de 360 pixels de largura. |
| **RNF10** | Disponibilidade | Os dados do sistema devem possuir mecanismo de backup periódico para permitir sua recuperação em caso de falha. |
| **RNF11** | Usabilidade | A interface da cozinha deve ser adequada para utilização em tablets e monitores, facilitando a visualização dos pedidos. |

---

## 1.8 Regras de Negócio

| Código | Regra de negócio |
|---|---|
| **RN01** | Cada usuário interno poderá possuir apenas um perfil de acesso ativo por vez. |
| **RN02** | Apenas Gerente ou Administrador poderá aplicar descontos superiores a 10% sobre o valor da conta. |
| **RN03** | O estorno de uma venda finalizada somente poderá ser realizado por Gerente ou Administrador e exigirá o registro de uma justificativa. |
| **RN04** | Um pedido enviado para a cozinha não poderá ser cancelado livremente pelo cliente ou atendente; cancelamentos excepcionais dependerão das permissões definidas pelo sistema. |
| **RN05** | Uma mesa ocupada ou aguardando pagamento não poderá receber um novo atendimento inicial até ser liberada. |
| **RN06** | Uma conta somente poderá ser encerrada quando todos os pedidos vinculados estiverem Entregues ou Cancelados e o valor total estiver integralmente pago. |
| **RN07** | Uma conta poderá utilizar mais de uma modalidade de pagamento, desde que a soma dos pagamentos corresponda ao valor total devido. |
| **RN08** | O sistema não poderá permitir que a quantidade física de um item de estoque fique abaixo de zero. |
| **RN09** | Antes da confirmação de um pedido, o sistema deverá verificar se existem ingredientes suficientes para sua preparação. |
| **RN10** | Quando um pedido for confirmado, os ingredientes necessários deverão ser reservados para evitar que sejam utilizados na confirmação de outro pedido. |
| **RN11** | Quando um pedido atingir o status Pronto, os ingredientes correspondentes deverão ser baixados do estoque físico. |
| **RN12** | Se um pedido for cancelado antes do início do preparo, os ingredientes reservados deverão ser liberados. |
| **RN13** | Todo produto que utilizar ingredientes controlados pelo estoque deverá possuir sua composição cadastrada com os respectivos ingredientes e quantidades. |
| **RN14** | O acesso do cliente vinculado a uma mesa deverá permitir somente operações relacionadas àquela mesa enquanto ela estiver ativa. |

---

## Resultado da Revisão

A revisão da primeira entrega preservou a estrutura e a numeração dos requisitos definidos anteriormente, realizando correções de redação e padronização da documentação.

As informações revisadas servem como base para os artefatos de modelagem desenvolvidos na segunda entrega, permitindo manter a consistência entre requisitos, casos de uso, processos, classes e estruturas do banco de dados.
