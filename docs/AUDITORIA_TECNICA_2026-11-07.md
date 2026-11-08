# Auditoria técnica — Gestão Financeira Pessoal
Data: 2026-11-07 | Base analisada: `main`

> Auditoria estática do código e comparação de branches. Não equivale a executar Maven, npm, MySQL ou testes de ponta a ponta. Não foram excluídas branches nem mescladas funcionalidades.

## 1. Estrutura do repositório
**Bom:** pastas `backend/` e `frontend/` separadas, arquivos de build declarativos (`pom.xml`, `package.json`, `package-lock.json`). A pasta `node_modules/` versionada foi removida e o `.gitignore` e README principal foram atualizados na análise anterior.
**Ajustar:** `frontend/README.md` ainda é o modelo genérico do Vite; `backend/classpath.txt` parece artefato local (validar antes de remover). Não há pipelines de CI na árvore principal.
**Justificativa:** manter apenas código e documentação reproduzíveis.

## 2. Backend
Organização:
- `controller/`: recebe endpoints HTTP e delega ações.
- `service/`: executa regras financeiras.
- `repository/`: consulta/persiste registros via Spring Data JPA.
- `model/`: entidades mapeadas para tabelas do MySQL.
- `dto/`: objetos usados como resultado de cálculos e respostas.
- `enums/`: valores fechados como RECEITA/DESPESA.
- `exception/`: respostas de erro.
- `resources/`: propriedades do servidor e da conexão.
- `src/test/`: testes unitários.

**Bom:** separação por camadas, uso de BigDecimal em transações e orçamentos, relacionamento entre entidades e quatro testes unitários de TransacaoService.
**Problemas e riscos:**
- BF-001 (alta): `application.properties` contém usuário/senha MySQL definidos em texto; externalizar com variáveis de ambiente, revisar credenciais antigas e evitar segredos em futuros commits.
- BF-002 (crítica): endpoints listam usuários/transações/orçamentos sem autenticação ou autorização no código analisado; bloquear acesso cruzado por usuário antes de qualquer implantação.
- BF-003 (alta): `OrcamentoService.calcularGasto` filtra categoria/tipo/mês, mas não usuário; contamina cálculos entre contas.
- BF-004 (alta): `MetaFinanceiraService.calcularMeta` soma todas as RECEITAS como valor guardado e pode dividir por zero para meta inválida.
- BF-005 (média): `InsightService.gerarInsights` divide o aumento de gastos pela soma dos períodos, não pelo valor anterior; na virada do ano consulta dezembro no ano errado.
- BF-006 (média): `Regra503020Service` calcula sobra como poupança e desconsidera categorias de despesa; consulta sem período combina histórico inteiro.
- BF-007 (média): `GlobalExceptionHandler` trata todo RuntimeException como HTTP 400, inclusive falhas internas e itens inexistentes.
- BF-008 (média): métodos gerais em `TransacaoController` e `UsuarioController` expõem consultas sem escopo de usuário; filtros também não impõem usuário.
- BF-009 (média): `TransacaoRepository.evolucaoMensal` agrupa só por MONTH sem YEAR; pode misturar anos.
- BF-010 (média): meta usa `Double` em dinheiro, diferente da padronização `BigDecimal` nas transações.
- BF-011 (média): falta verificação uniforme de descrição, data, tipo, limites e identificadores relacionados.
- BE-001 (baixa, registro **corrigido**): o frontend declara `adicionarCategoria` mas não a invoca na tela de transações analisada. O backend já restringe a criação às 12 categorias. Manter apenas as 12 e remover a função não utilizada, sem implementar categorias personalizadas.

## 3. Frontend
**Bom:** telas em componentes separados (Login, Cadastro, EsqueciSenha, Dashboard, Transacoes, OrcamentosEMetas, Relatorios, Header, ModalEditarTransacao), interfaces TypeScript, layout responsivo e filtros visuais.
**Problemas:**
- FE-001 (crítica): login é simulado com setTimeout, sem autenticação; cadastro apenas muda de tela; recuperação de senha exibe sucesso sem envio.
- FE-002 (alta): transações, orçamentos e metas são mantidos em estado React, sem persistência após recarregar.
- FE-003 (alta): dados de demonstração embutidos em `App.tsx` e `OrcamentosEMetas.tsx` podem ser apresentados como reais.
- FE-004 (média): orçamento do frontend soma despesas da categoria de todos os períodos em vez do mês atual, apesar do requisito mensal.
- FE-005 (média): validações no frontend não cobrem todos os valores inválidos (ex.: edição de transação e limites numéricos).
- FE-006 (média): `OrcamentosEMetas.tsx` limita barra a 100%; deve exibir percentual real excedido em texto/alerta.
- FE-007 (baixa): harmonizar identidade visual/marca e remover resíduos do template React/Vite.

## 4. Integração
- INT-001 (crítica): nenhum `fetch`, `axios` ou outro cliente HTTP foi encontrado nos componentes principais analisados; estado local não é API.
- INT-002 (alta): modelos de frontend e backend diferem: `id` vs `idTransacao`, `data` vs `dataTransacao`, `establishment` vs `estabelecimento`, categoria string vs entidade.
- INT-003 (média): `CategoriaController` só permite CORS de localhost:3000, enquanto Vite normalmente serve em 5173; verificar origens e política centralizada.
- INT-004 (alta): não existe fluxo integrado de sessão, identificação do usuário, tratamento de erro e atualização após gravação.

## 5. Funcionalidades por etapa
| Área | Código/tela | Validação operacional |
|---|---|---|
| Usuários | Endpoints CRUD e telas de cadastro/login | Não integrado; autenticação ausente |
| Categorias | 12 padronizadas, sugestão e troca de categoria no backend | Necessita teste |
| Transações | CRUD e filtros no backend; CRUD local no front | Não integrado |
| Orçamentos | CRUD e cálculos | Cálculo por usuário incorreto no backend e mês no frontend |
| Metas | Serviço e formulário local | Sem integração; regra de acumulado incorreta |
| Relatórios | Endpoints e relatório visual local | Não integrado; agrupamentos/períodos a validar |
| Insights | Serviços e endpoints | Não há visualização integrada demonstrada; fórmulas a corrigir |
| Regra 50/30/20 | Serviço e endpoint | Fórmula de poupança incompleta |
| Integração bancária | Não consta na main | Examinar branch de demonstração sem declarar integração real |

## 6. Testes e validações
- Há quatro testes unitários de `TransacaoService` no código analisado. A existência não garante que passaram.
- Não foi possível executar Maven, npm, MySQL e testes E2E neste ambiente; o GitHub não exibe workflow CI na árvore analisada.
- Prioridade: compilar `mvnw test`, `npm ci && npm run build && npm run lint`; testes de integração com banco temporário; autenticação/autorização com dois usuários; CRUD e filtros; meses/anos; valores zero/negativos; cálculo de orçamentos, metas, relatórios e regra 50/30/20; fluxo browser→API→MySQL.
- Critérios de aceite: nenhum dado financeiro cruza usuários; dados sobrevivem a recarga; erros HTTP consistentes; valores coincidem em frontend, API e banco.

## 7. Limpeza e branches
Branch `main`: base da auditoria. Nenhuma branch foi apagada ou mesclada.
- `chore/limpeza-target-git`, `feature/filtros-consulta-testes-transacao`, `integracao-orcamento-mensal`: comparação indica 0 commits à frente da main; candidatas a encerramento após confirmar PRs/histórico.
- `chore/limpeza-repositorio`: 3 à frente/3 atrás; revisar commits, não mesclar `node_modules` de volta.
- `feat/demonstracao-bancaria`: 1 à frente/3 atrás; revisar arquivos de integração **simulada**, validar requisitos e testes antes de qualquer merge.
- `feat/tratamento-erros`: 3 à frente/34 atrás, estrutura antiga na raiz.
- `feat/tratamento-erros-atualizado`: 5 à frente/29 atrás, estrutura antiga na raiz.
- `feature/orcamento-mensal`: 1 à frente/31 atrás, estrutura antiga na raiz.
- `feature/regra-50-30-20`: 1 à frente/11 atrás, inclui artefatos `backend/target/`; não mesclar cegamente.
- `feat/dashboard-graficos` e `feat/frontend-base`: GitHub informa **sem ancestral comum** com `main`; requer comparação manual.
**Regra de segurança:** nunca excluir ou mesclar por contagem isolada de commits. Conferir mudanças exclusivas, PRs, divergências e impacto antes da decisão final.

## Próxima execução recomendada
1. Prioridade zero: autenticação/autorização e isolamento de usuário; retirar credenciais fixas.
2. Corrigir cálculo de orçamento e metas e cobrir com testes.
3. Projetar contrato de API para integrar frontend e backend.
4. Alinhar as 12 categorias sem criar categorias extras.
5. Corrigir relatórios, insights e 50/30/20 com casos de teste.
6. Rodar build/lint/testes e E2E em ambiente com Node, Java e MySQL.
7. Só então revisar merges/remoções de branches.

## Limitações
Conclusões sobre execução são **pendentes**, não aprovação ou reprovação. Nenhum dado pessoal ou bancário foi utilizado para simulações.
