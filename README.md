# Gestão Financeira Pessoal

Projeto acadêmico desenvolvido para auxiliar no controle e acompanhamento das finanças pessoais, com registro de receitas e despesas, categorização de gastos, orçamentos, metas, relatórios e indicadores financeiros.

## Tecnologias

### Backend
- Java
- Spring Boot
- Maven
- MySQL

### Frontend
- React
- TypeScript
- Vite
- Tailwind CSS

## Estrutura do repositório

```text
gestao-financeira-pessoal/
├── backend/   # API, regras de negócio e acesso a dados
├── frontend/  # Interface da aplicação
├── README.md
└── .gitignore
```

## Como executar

### Backend

Entre na pasta `backend` e execute o projeto com Maven:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

> A configuração do banco de dados deve estar definida em `backend/src/main/resources/application.properties`.

### Frontend

Entre na pasta `frontend`:

```bash
npm install
npm run dev
```

## Objetivo do sistema

O sistema busca centralizar informações financeiras do usuário e apoiar o acompanhamento de receitas, despesas, categorias, orçamentos, metas e relatórios, de acordo com os requisitos definidos na documentação do projeto.

## Status

Projeto acadêmico em desenvolvimento.
