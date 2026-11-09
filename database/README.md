# Banco de dados — MySQL 8.0+

Esta pasta mantém apenas os arquivos necessários para instalar o banco em um computador novo e conferir os dados salvos pelo site.

| Arquivo | Finalidade |
|---|---|
| `01_instalacao_mysql8.sql` | Cria o banco, as tabelas das Etapas 1 e 2 e as 12 categorias padrão |
| `03_conferir_persistencia.sql` | Consulta os dados salvos por e-mail, sem alterá-los |
| `README.md` | Orientações de instalação |

## Instalação em computador novo

1. Instale MySQL 8+, Java 21 e Node.js compatível com o Vite.
2. Execute `01_instalacao_mysql8.sql` no DBeaver/MySQL.
3. Configure o backend com `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` locais.
4. Inicie o backend: `cd backend` e `.\mvnw.cmd spring-boot:run`.
5. Em outro terminal, inicie o frontend: `cd frontend`, `npm ci` e `npm run dev`.
6. Acesse `http://localhost:5173`.

O script principal já cria `transacao.id_categoria` como opcional para receitas sem categoria e `historico_relatorio_mensal.relatorio_json` como `LONGTEXT`.

**Banco já existente:** faça backup e confira a estrutura antes de qualquer alteração. `CREATE TABLE IF NOT EXISTS` não atualiza colunas antigas. Scripts pontuais de correção foram retirados da branch para evitar confusão: caso um colega tenha banco antigo incompatível, a atualização deve ser avaliada separadamente, sem apagar registros.

Para conferir a persistência, altere `@email_qa` no arquivo `03_conferir_persistencia.sql`. O script é somente leitura.

O sistema local usa `localhost:5173` no frontend e `localhost:8080` no backend, ambos no mesmo computador. A instalação em outro PC ainda precisa ser validada pela equipe.
