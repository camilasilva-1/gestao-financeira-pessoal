# Banco de dados — MySQL 8.0+

Este diretório reúne os arquivos SQL oficiais das Etapas 1 e 2 da branch `fix/correcoes-etapa-2`.

## Qual arquivo executar?

| Situação | Arquivo |
|---|---|
| Computador novo, banco ainda inexistente | `01_instalacao_mysql8.sql` |
| Banco existente com dados | Backup + `01_instalacao_mysql8.sql` (cria somente tabelas faltantes) + `02_atualizacao_banco_existente.sql` (migração conservadora; revisão prévia obrigatória) |
| Conferir dados gerados pelo site | `03_conferir_persistencia.sql` (somente SELECT; substituir `@email_qa`) |

Nunca executar o antigo script com `id_categoria NOT NULL`, pois receitas sem categoria são permitidas, nem scripts que apagam ou recarregam categorias.

**Atenção ao banco existente:** SQL DDL não é transacional. Antes de rodar migração, faça backup e compare `SHOW CREATE TABLE transacao;`. A migração recria a FK de categoria ao modificar sua nulabilidade, preservando as regras de integridade. Caso o banco tenha estrutura personalizada não prevista, pare e faça migração específica; não assuma que uma atualização genérica é infalível.

## Como configurar em outra máquina

Instale MySQL 8+, Java 21 e Node compatível com o Vite. No MySQL, crie as tabelas usando o arquivo de instalação, sem importar dados pessoais de outras máquinas.

O backend já lê as variáveis de ambiente:
- `DB_URL` — por exemplo `jdbc:mysql://localhost:3306/gestao_financeira`
- `DB_USERNAME` — usuário MySQL local da pessoa
- `DB_PASSWORD` — senha MySQL local da pessoa

Não versionar credenciais. No terminal PowerShell **daquela máquina**, por exemplo:
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/gestao_financeira"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<senha local>"
cd backend
.\mvnw.cmd spring-boot:run
```

Em segundo terminal:
```powershell
cd frontend
npm ci
npm run dev
```

O sistema atualmente referencia `http://localhost:8080` e `http://localhost:5173`; pressupõe frontend e backend executando **no mesmo computador**. Para hospedar ou acessar de outro dispositivo, será necessário configurar URLs e CORS; isso não está coberto por esta instalação local.

## Verificações

Usar `docs/roteiro-homologacao-frontend-etapas1e2.md` e `03_conferir_persistencia.sql`. Os scripts foram elaborados conforme entidades JPA; **não foram executados em uma segunda máquina neste atendimento**, logo a portabilidade deve ser comprovada pelos colegas.
