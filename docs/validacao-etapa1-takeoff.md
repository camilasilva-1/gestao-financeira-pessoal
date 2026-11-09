# Etapa 1 — Conferência final do Takeoff

Referências: Takeoff (seção "Etapa 1: Controle Financeiro Básico", 3.1 a 3.3, 3.11, 4 e 5); Trello "Takeoff - bradesco"; branch `fix/correcoes-etapa-2`.

## Validado na revisão local de 08/11/2026
- Cadastro/login, inclusive cadastro sem salário; logout e sessão.
- CRUD de transações, associação a usuário, 12 categorias e correção da categoria Moradia.
- Receita sem categoria e estabelecimento após recarregar a página.
- Recorrência persistida após F5 e filtros de data/estabelecimento/recorrência.
- Dashboard, histórico, visualização de orçamentos existentes, controle de acesso anônimo (401) e entre contas (403).
- Backend: 19 testes aprovados antes das duas últimas correções; frontend: build aprovado antes das duas últimas correções.

## Alterações posteriores a revalidar
- Modal de edição: preservar receita sem categoria e permitir editar recorrência.
- Novos testes em `TransacaoServiceTest`: valor zero/negativo, descrição vazia, despesa sem categoria, edição/exclusão por outro usuário.

## Ainda não é possível declarar conformidade literal de 100%
- Rodar novamente `mvn test` e `npm run build` após as últimas alterações.
- Conferir requisitos não funcionais do Takeoff (responsividade, desempenho e console): ainda sem evidência formal de todos os limites.
- Revisar histórico/relacionamentos da seção 5.5 e os cenários não cobertos (ex.: usuário/categoria inexistentes e ordenação).
- Definir com a equipe a etapa de entrega da integração bancária automática, mencionada em 3.1 mas não indicada claramente na descrição da Etapa 1.
- Fazer revisão final do PR e integração à `main` apenas com autorização; o Trello só deve ser encerrado após evidências.

## Fora do recorte de Etapa 1
Orçamentos avançados, alertas, metas financeiras, insights e inteligência financeira, planejados em etapas posteriores.

Nenhum teste descrito neste documento apaga ou modifica dados de produção. Para testes de escrita, utilizar banco isolado; não executar suite destrutiva na base pessoal existente.
