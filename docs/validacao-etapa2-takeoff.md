# Etapa 2 — Conferência Takeoff × branch de correções

**Escopo:** Planejamento e Acompanhamento (seções 3.3, 3.4, 3.6 e 3.7, regras 3.11 e requisitos não funcionais aplicáveis).

**Regra de entrega:** NÃO integrar esta branch à `main` antes da aprovação dos testes finais da Etapa 2 e revisão do PR pela equipe. Não atualizar cards do Trello sem as evidências.

## Conferência inicial do Trello (08/11/2026)
- 14 cards com rótulo Etapa 2; 11 em Concluído; 2 em Revisão/Testes; 1 Em andamento.
- Pendentes de verificação final: alertas preventivos por faixa de consumo; regra 50/30/20; cadastro de salário e receitas na interface.
- Cards de relatório, orçamento e ranking já estão marcados como concluídos, mas ainda exigem testes de regressão dos valores apresentados.

## Problemas concretos corrigidos na branch
1. `frontend/src/components/OrcamentosEMetas.tsx`: cálculo visual de orçamento usava o histórico inteiro e faixas 80/100%; agora lê o gasto mensal por categoria e usuário fornecido pelo backend e apresenta Normal, Atenção, Risco, Estourado conforme o Takeoff.
2. `backend/src/main/java/gestaofinanceira/project/service/InsightService.java`: alertas de orçamento agora incluem faixa Atenção acima de 70% e identificam Normal/Risco/Estourado.
3. `frontend/src/App.tsx`: orçamentos recarregam quando as transações na tela mudam.
4. `frontend/src/components/Relatorios.tsx`: datas ISO tratadas como locais para evitar dia/mês errado por fuso horário.

**Interpretacao de limite não expresso literalmente no Takeoff:** o texto especifica "até 70", "71 a 89", "acima de 90", "acima de 100" mas não atribui estado a exatamente 90%; para cobrir a lacuna o sistema usa **90% = Risco** e **acima de 100% = Estourado**. A equipe deve confirmar se essa escolha atende à expectativa.

## Testes acrescentados (ainda por executar na máquina local)
- `backend/src/test/java/gestaofinanceira/project/service/AlertasOrcamentoFaixasTest.java`: 7 cenários de fronteira de percentual.
- `backend/src/test/java/gestaofinanceira/project/service/OrcamentoMensalTakeoffTest.java`: gasto, saldo e percentual do mês filtrado por usuário e categoria.
- `backend/src/test/java/gestaofinanceira/project/service/Regra503020ServiceTest.java`: já existia um teste da regra 50/30/20 na branch.
- `qa/auditoria-etapa2.cjs`: auditoria somente leitura de APIs, comparação de totais, ranking, regra 50/30/20, salário e orçamento/alertas quando existem orçamentos.

## Como validar
1. `git pull` na pasta `versao-atualizada`.
2. No `backend`: `.\\mvnw.cmd test` (banco pessoal NÃO deve ser apagado nem recriado; testes de escrita JPA usam H2 isolado).
3. `npm run build` no `frontend`.
4. Reiniciar backend e frontend com os arquivos da branch atualizada.
5. Na raiz da `versao-atualizada`: `node qa/auditoria-etapa2.cjs`, entrar no Chromium e registrar a tabela.

## Condições para marcar 100%
- Testes Java e build frontend aprovados.
- Auditoria Etapa 2 sem falhas.
- Testar explicitamente faixas 70/71/89/90/100/101 e orçamento zero/sem orçamento via testes unitários ou ambiente isolado.
- Conferir no Takeoff se requisitos de relatórios completos e registro de histórico dos relatórios mensais (RN009) pertencem à entrega desta etapa; não marcar isto como validado por haver apenas endpoint ou tela.
- Confirmar integração visual da regra 50/30/20 se exigida pela entrega.
- Revisar evidências e a branch com equipe antes de qualquer merge na `main`.

**Importante:** este documento registra mudanças e verificações propostas. Não é um certificado de aprovação dos novos testes.
