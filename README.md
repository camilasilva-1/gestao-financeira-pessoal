# Gestão Financeira Pessoal — ByteBank Finance (projeto acadêmico)

Sistema de controle e planejamento financeiro pessoal. **Este repositório não é um serviço bancário oficial.**

## Organização da branch de correções

```text
gestao-financeira-pessoal/
├── backend/      API Java 21 + Spring Boot + testes
├── frontend/     React + TypeScript + Vite
├── database/     SQL de instalação e conferência
├── docs/         Takeoff/validações e roteiro de homologação
├── qa/           Auditorias automatizadas locais
└── README.md
```

**Importante:** esta é a raiz do repositório Git. Em alguns computadores a pasta foi extraída de ZIP e existe outra pasta externa `versao-atualizada`; não é preciso duplicar backend/frontend nem criar mais uma cópia. Para colegas, prefira `git clone` diretamente da branch acordada.

## Instalação local em outras máquinas

Requisitos: MySQL 8+, Java 21 e Node.js compatível com a versão do Vite. Não inclua arquivos de senha, dump de contas pessoais ou `node_modules` no Git.

1. Criar o schema e as tabelas pelo `database/01_instalacao_mysql8.sql` no MySQL. As 12 categorias padrão são inseridas sem duplicação.
2. Para uma base **existente**, faça backup e leia `database/README.md`. O script de instalação não corrige estruturas antigas: atualizações devem ser avaliadas separadamente, sem excluir dados.
3. Definir na própria máquina as variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. Exemplo PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/gestao_financeira"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<senha do MySQL local>"
cd backend
.\mvnw.cmd spring-boot:run
```

4. Em outro terminal, na raiz do repositório:

```powershell
cd frontend
npm ci
npm run dev
```

Acessar `http://localhost:5173`. O frontend e backend estão configurados para rodar no **mesmo PC** (5173 e 8080); acesso remoto/hospedagem exige configuração específica de URLs e CORS.

## Homologação antes da `main`

- Roteiro completo de testes manuais: `docs/roteiro-homologacao-frontend-etapas1e2.md`
- Conferência das gravações no DBeaver: `database/03_conferir_persistencia.sql`
- Validação técnica das etapas: `docs/validacao-etapa1-takeoff.md` e `docs/validacao-etapa2-takeoff.md`
- Auditoria automatizada: `node qa/auditoria-etapa2.cjs`; testes unitários: `cd backend && .\mvnw.cmd test`; frontend: `npm run build`.

As verificações anteriores foram executadas na branch de correções. A portabilidade para outras máquinas precisa ser comprovada antes de afirmar que todas as instalações funcionam. A `main` não deve ser integrada sem autorização da equipe.
