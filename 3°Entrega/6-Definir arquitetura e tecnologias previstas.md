A primeira entrega estabelece que o sistema será desenvolvido como uma **aplicação web**, utilizando um **banco de dados relacional**, funcionando em navegadores modernos e possuindo interface responsiva.

As tecnologias específicas abaixo serão utilizadas como proposta para a implementação do sistema:

| Camada/elemento | Tecnologia escolhida | Justificativa |
|---|---|---|
| Interface / Frontend | HTML, CSS e JavaScript | Tecnologias adequadas para o desenvolvimento de uma aplicação web e para criação de uma interface responsiva. |
| Servidor / Backend | Java + Spring Boot | Permite implementar as regras de negócio, autenticação, controle de acesso e comunicação com o banco de dados. |
| Banco de dados | MySQL | Banco de dados relacional adequado para armazenar as informações do restaurante. |
| Controle de versão | Git e GitHub | Permite acompanhar a evolução do projeto e organizar o desenvolvimento da equipe. |
| Protótipos | Figma | Permite criar e visualizar os protótipos das telas antes da implementação. |
| Hospedagem | A definir | Será definida posteriormente conforme a evolução do projeto e as necessidades de implantação. |

## 6.1 Diagrama simples de arquitetura

```text
┌──────────────┐
│   USUÁRIO    │
└──────┬───────┘
       ↓
┌─────────────────────────┐
│ INTERFACE / FRONTEND    │
│ HTML + CSS + JavaScript │
└──────────┬──────────────┘
           ↓
┌─────────────────────────┐
│ BACKEND / SERVIDOR      │
│ Java + Spring Boot      │
└──────────┬──────────────┘
           ↓
┌─────────────────────────┐
│ BANCO DE DADOS          │
│ MySQL                   │
└─────────────────────────┘
