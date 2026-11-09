# Frontend — Gestão Financeira Pessoal

Interface das Etapas 1 e 2 do projeto acadêmico, desenvolvida em **React, TypeScript, Vite e Tailwind CSS**.

## Executar localmente

Pré-requisitos: Node.js compatível com o Vite e backend Spring Boot em execução na porta `8080`.

No terminal, dentro da pasta `frontend`:

```powershell
npm ci
npm run dev
```

Abra `http://localhost:5173`. O frontend consulta o backend em `http://localhost:8080`, no mesmo computador.

## O que é possível testar

- Etapa 1: cadastro, login/logout, receitas, despesas, categorias, filtros, edição/exclusão e indicadores.
- Etapa 2: orçamento por categoria, alertas, regra 50/30/20 e relatórios mensais com histórico salvo.

Os dados são persistidos pelo backend no MySQL; não são criados diretamente no frontend.

## Conferência técnica

```powershell
npm run build
npm run lint
```

O roteiro manual de homologação está em `../docs/roteiro-homologacao-frontend-etapas1e2.md`. Para instalação completa, consulte o `../README.md` e `../database/README.md`.

A hospedagem e o acesso por outro dispositivo ainda precisam de configuração de URLs e CORS; não fazem parte desta execução local.
