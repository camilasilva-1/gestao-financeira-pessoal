# Backend — Gestão Financeira Pessoal

API em **Java 21, Spring Boot e MySQL**, utilizada pelo frontend do projeto acadêmico.

## Estrutura do código

- `controller/`: recebe as requisições HTTP.
- `service/`: aplica as regras de negócio e os cálculos.
- `repository/`: consulta e persiste registros por JPA.
- `model/`: representa tabelas e relacionamentos.
- `dto/`: dados enviados nas respostas da API.
- `config/`: segurança de sessão e autorização.
- `src/test/`: testes automatizados.

## Rodar localmente (Windows / PowerShell)

No MySQL, execute o script de instalação `../database/01_instalacao_mysql8.sql` em uma **base nova**.

Na pasta `backend`, configure as credenciais do **seu** MySQL:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/gestao_financeira"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<senha local do MySQL>"
.\mvnw.cmd spring-boot:run
```

Por padrão, a API responde em `http://localhost:8080`. A interface do site está em `http://localhost:5173`, executada separadamente na pasta `frontend`.

As rotas por usuário são protegidas por sessão; não basta informar o ID de outro usuário para acessar os dados dele.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes de serviço utilizam mocks, e o teste de integração do histórico utiliza um banco H2 temporário, sem gravar no MySQL pessoal.

As regras validadas das Etapas 1 e 2 incluem: receitas sem categoria, despesas com categoria, orçamento mensal isolado por usuário, alertas por faixas, cálculo 50/30/20 e persistência explícita de relatórios mensais.

Veja `../README.md` e `../database/README.md`. A instalação em outra máquina ainda precisa ser validada pela equipe.
