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

## Conferência documental adicional após 26/26 (08/11/2026)

Auditoria de leitura `qa/auditoria-etapa2.cjs`: **26/26 aprovações**, mais 34 testes Java e build frontend aprovados. Esse resultado confirma os casos exercitados; NÃO encerra requisitos fora da cobertura.

### Confirmado no código
- Faixas e percentuais na interface em `OrcamentosEMetas.tsx`, associados ao gasto mensal retornado pela API; testes unitários das faixas aprovados. A auditoria real só exercitou percentuais baixos (0% e ~13%), portanto falta validar visualmente faixas Atenção/Risco/Estourado com massa isolada.
- Regra 50/30/20 tem DTO/serviço/endpoint backend, com teste unitário; valores ideais da renda mensal por transações foram comparados na auditoria. Não foi encontrada exibição da regra 50/30/20 no `App.tsx`, `Relatorios.tsx` ou `OrcamentosEMetas.tsx`.
- Relatórios mensais e ranking têm endpoints, e os totais foram conferidos na auditoria; existe interface para comparação mês atual/anterior e ranking.
- Cadastro de salário é opcional no frontend; login recebe e guarda valor do backend; auditoria confirmou valor de R$ 2.000 naquele usuário.

### Lacunas que impedem declarar conformidade literal de 100% sem novas evidências
1. **RN009** (histórico dos relatórios mensais gerados): não foi identificada persistência ou listagem de histórico de relatórios no fluxo consultado; conferir todo o repositório antes de propor implementação.
2. **Regra 50/30/20 visível ao usuário**: endpoint existe, mas falta verificar como o usuário acompanha a recomendação; avaliação de necessidade de exibição simples conforme seção 3.7.
3. **Base da renda usada na regra 50/30/20**: backend usa receitas do mês, enquanto seção 3.7 exemplifica salário líquido. Verificar se salário + receitas devem compor a base (sem dupla contagem) antes de alterar fórmula.
4. **Relatório de distribuição (seção 3.6)**: conferir apresentação completa das principais categorias e evolução em relação ao mês anterior; API e interface não equivalem automaticamente a todas as informações exigidas.
5. **Faixas do card 21**: já corrigidas na branch e testes de limites Java aprovados, mas só Normal foi efetivamente exercitado pela auditoria com dados do usuário. Não finalizar o card de terceiro sem revisão.
6. **RNF020**: geração de relatório mensal em menos de 10 segundos ainda não foi cronometrada especificamente.
7. **Faixas em porcentagens não inteiras**: o Takeoff tem lacuna em exatamente 90%; foi adotado Risco a partir de 90% para não haver ausência de aviso. Pedir alinhamento da equipe apenas se necessário.

Nenhuma mudança em `main` ou no Trello durante esta conferência. **Não marcar 100% por 26/26**, pois a auditoria não abrange integralmente a documentação.

## Implementações finais preparadas na branch (09/11/2026; NÃO testadas após este conjunto de mudanças)

- **Regra 50/30/20 visível:** seção simples `Planejamento 50/30/20` na interface de relatórios, alimentada pelo endpoint existente. A base explicitamente apresentada é a **soma das receitas registradas no mês**; o vínculo conceitual com salário informado separadamente continua merecendo conferência com o Takeoff. Não há alteração do cálculo para evitar dupla contagem de salário com receitas.
- **RN009 — histórico de relatórios gerados:** entidade/tabela nova `historico_relatorio_mensal`, repositório isolado por usuário, POST de arquivamento do mês completo sob clique explícito e GET de consulta ao histórico. O relatório completo é preservado em `relatorio_json`, junto dos totais do período. Não altera transações nem orçamentos existentes. O Hibernate em `ddl-auto=update` pode **criar essa nova tabela no MySQL local** ao iniciar o backend atualizado; fazer backup antes de aplicar mudanças em ambiente compartilhado. Todos os testes de cadastro/arquivamento automatizados devem usar mocks ou banco temporário, não a base pessoal.
- **Auditoria consolidada:** `qa/auditoria-etapa2.cjs` mantém os 26 cenários anteriores e acrescenta checagem visual da regra, interface e consulta de histórico, controle de acesso, limite de 10 s para geração mensal, e 4 faixas visuais de orçamento com respostas GET simuladas (nada gravado no MySQL).
- **Testes Java adicionais:** `HistoricoRelatorioMensalServiceTest` (3 testes); teste de limites `AlertasOrcamentoFaixasTest` já existente.

### Rodada final a executar (na máquina local com banco preservado)
```powershell
cd "C:\Projetos\gestao financeira pessoal\gestao-financeira-pessoal-fix-correcoes-etapa-2\versao-atualizada"
git pull
cd backend
.\mvnw.cmd test
cd ..\frontend
npm run build
```
Reiniciar os servidores backend/frontend **atualizados** antes de rodar:
```powershell
cd "C:\Projetos\gestao financeira pessoal\gestao-financeira-pessoal-fix-correcoes-etapa-2\versao-atualizada"
node qa/auditoria-etapa2.cjs
```

**Não declarar Etapa 2 concluída somente por essas alterações.** Falhas devem ser corrigidas antes de atualizar o Trello. A `main` permanece intocada. Os requisitos de relatório e a correspondência entre renda e salário serão analisados pelos resultados da auditoria e revisão documental.
