# Backend — Gestão Financeira Pessoal

Este backend é parte do projeto acadêmico descrito no **Takeoff — Bradesco**. Usa Java 21, Spring Boot e MySQL.

## Entendendo as pastas

- **controller/** recebe as requisições HTTP, por exemplo `GET /transacoes`.
- **service/** valida dados e realiza os cálculos financeiros.
- **repository/** consulta e salva dados no banco usando JPA.
- **model/** representa as tabelas: usuário, transação, categoria, orçamento e meta.
- **dto/** define os resultados dos cálculos e relatórios que vão para o frontend.
- **enums/** lista valores fixos, como `RECEITA` e `DESPESA`.
- **exception/** trata erros e define os códigos de resposta.
- **src/main/resources/** guarda a configuração da aplicação.
- **src/test/** contém testes com JUnit e Mockito.

## Executar no computador

1. Instale Java 21 e MySQL.
2. Crie no MySQL o banco `gestao_financeira`.
3. Configure o usuário e a senha do banco pelas variáveis `DB_USERNAME` e `DB_PASSWORD`. A variável `DB_URL` é opcional; por padrão o sistema usa `jdbc:mysql://localhost:3306/gestao_financeira`.
4. Abra um terminal na pasta `backend` e execute:
   - Windows: `mvnw.cmd spring-boot:run`
   - Linux/macOS: `./mvnw spring-boot:run`
5. Se iniciar corretamente, acesse `http://localhost:8080/ping`.

Exemplo para definir variáveis no PowerShell **antes de iniciar** (substitua com seus dados locais):

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "SENHA_DO_SEU_MYSQL"
.\mvnw.cmd spring-boot:run
```

## Rodar testes

Na pasta `backend`, execute `mvnw.cmd test` no Windows ou `./mvnw test` no Linux/macOS.

Os testes unitários usam dependências simuladas (Mockito); por isso não exigem o MySQL para os testes de serviço já criados. Para conferir a API e o banco reais, é necessário iniciar o MySQL e testar os endpoints.

## Decisões do Takeoff

- Manter **somente as 12 categorias padrão**.
- Transações devem ter valor maior que zero, descrição, data, tipo, usuário e categoria.
- O orçamento deve contabilizar despesas do **mês atual e do próprio usuário**.
- O valor acumulado da meta é o valor efetivamente informado como guardado, não a soma de salários recebidos.
- A regra **50/30/20** usa a renda e os gastos do mês; a sobra é apenas uma **estimativa** do valor disponível para guardar.
- O frontend ainda precisa ser integrado ao backend. **Login, autenticação e autorização não estão concluídos.**

## Estado desta branch

`fix/correcoes-backend` contém correções pontuais do backend. **A compilação, a execução com MySQL e os testes automatizados ainda precisam ser executados e confirmados no computador da equipe ou em CI.**

Não utilizar o projeto com informações financeiras reais antes de validar autenticação, isolamento entre usuários e persistência.
