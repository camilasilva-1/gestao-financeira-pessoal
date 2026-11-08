# Gestão Financeira Pessoal

Projeto acadêmico de um sistema para acompanhamento das finanças pessoais, com cadastro de receitas e despesas, categorias, orçamentos, metas financeiras, relatórios e indicadores.

> **Estado do projeto:** em desenvolvimento. A presença das telas e endpoints no código não significa que todos os fluxos estejam integrados ou validados.

## Tecnologias

- **Frontend:** React 19, TypeScript, Vite e Tailwind CSS.
- **Backend:** Java 21, Spring Boot 3.3.4, Spring Web, Spring Data JPA e Maven.
- **Banco de dados:** MySQL.

## Estrutura

```text
backend/   API REST, regras de negócio, persistência e configurações
frontend/  Interface web, componentes e visualizações
```

## Executar localmente

### Pré-requisitos

- Java 21
- MySQL
- Node.js e npm compatíveis com as versões do Vite do projeto

### Backend

1. Crie um banco de dados MySQL chamado `gestao_financeira`.
2. Revise as configurações em `backend/src/main/resources/application.properties`. **Não utilize as credenciais de exemplo em ambientes reais.** A externalização das credenciais ainda está pendente.
3. No diretório `backend`, execute `./mvnw spring-boot:run` (Linux/macOS) ou `mvnw.cmd spring-boot:run` (Windows).

A configuração atual define a porta `8080`.

### Frontend

No diretório `frontend`:

```bash
npm ci
npm run dev
```

Use o endereço local indicado pelo Vite no terminal.

## Organização funcional

O repositório contém componentes de login, cadastro, recuperação de senha, dashboard, transações, orçamentos, metas e relatórios, além de camadas backend de controllers, services, repositories e models.

**Importante:** os dados iniciais do frontend incluem exemplos definidos no código. A comunicação efetiva com a API, a autenticação, a persistência e os testes precisam ser verificados antes de declarar o sistema concluído.

## Próximas verificações

- Conferir segurança de credenciais e autenticação.
- Testar os endpoints e regras de negócio do backend.
- Verificar a integração real entre frontend e backend.
- Executar testes, lint e build.
- Revisar funcionalidades, documentação e branches.

## Aviso

Projeto acadêmico; não é um sistema bancário oficial nem possui, por si só, integração com contas do Bradesco.
