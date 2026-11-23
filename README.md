# ByteBank Finance

O **ByteBank Finance** é um projeto acadêmico de gestão financeira pessoal. Ele permite registrar receitas e despesas, acompanhar o saldo, organizar orçamentos e metas, consultar relatórios e visualizar indicadores e recomendações financeiras.

O sistema está em fase final de desenvolvimento e pode ser executado localmente.

## Tecnologias

- **Frontend:** React 19, TypeScript, Vite e Tailwind CSS.
- **Backend:** Java 21, Spring Boot 3.3.4 e Maven.
- **Banco de dados:** MySQL 8.0+.

## Estrutura do projeto

```text
backend/    API, regras de negócio e testes
frontend/   Interface do sistema
database/   Scripts SQL de instalação e conferência
```

## Pré-requisitos

Instale no computador:

- **Git**
- **Java 21**
- **MySQL 8.0 ou superior** (servidor em execução)
- **Node.js e npm** em versões compatíveis com o Vite 8

O DBeaver é opcional e pode facilitar a execução do script SQL.

## Como executar localmente

Os exemplos abaixo usam **Windows / PowerShell**. Abra os terminais na pasta principal do projeto.

### 1. Baixar o projeto

```powershell
git clone https://github.com/camilasilva-1/gestao-financeira-pessoal.git
cd gestao-financeira-pessoal
```

### 2. Preparar o MySQL

No DBeaver ou em outro cliente MySQL, conecte-se ao seu servidor e execute o arquivo:

```text
database/01_instalacao_mysql8.sql
```

O script cria o banco `gestao_financeira` e as tabelas necessárias para uma instalação nova.

**Atenção:** se você já tiver um banco com dados, faça um backup e consulte `database/README.md` antes de executar scripts. Não apague o banco existente para instalar o projeto.

### 3. Iniciar o backend

Em um terminal PowerShell:

```powershell
cd backend
$env:DB_URL = "jdbc:mysql://localhost:3306/gestao_financeira"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "SUA_SENHA_MYSQL"
.\mvnw.cmd spring-boot:run
```

Substitua `root` e `SUA_SENHA_MYSQL` pelas credenciais do **seu** MySQL. Essas variáveis valem para o terminal atual; não é necessário alterar nem publicar senhas no repositório.

A API será iniciada em `http://localhost:8080`. Deixe esse terminal aberto.

### 4. Iniciar o frontend

Abra **outro terminal** na pasta principal do projeto:

```powershell
cd frontend
npm ci
npm run dev
```

Acesse **http://localhost:5173** no navegador e crie uma conta para começar a usar o ByteBank Finance.

O frontend precisa do backend e do MySQL em execução. A configuração atual é para uso local, no mesmo computador.

## Conferir a instalação

Para executar os testes do backend, dentro da pasta `backend`:

```powershell
.\mvnw.cmd test
```

Para conferir a compilação do frontend, dentro da pasta `frontend`:

```powershell
npm run build
```

Mais detalhes sobre as partes do projeto estão nos arquivos `backend/README.md`, `frontend/README.md` e `database/README.md`.

> **Observação:** projeto acadêmico de demonstração, não vinculado a um serviço bancário oficial. A recuperação de senha por e-mail ainda não está disponível.
