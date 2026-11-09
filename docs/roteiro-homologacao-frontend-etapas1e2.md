# Homologação manual — Etapas 1 e 2 (antes do merge)

**Referência:** Takeoff, cards Trello Etapas 1 e 2 e código da branch `fix/correcoes-etapa-2`.
**Regra:** testar **em uma conta QA recém-criada**, jamais mexer nos registros financeiros pessoais. Não mover o merge para `main` antes da revisão.

## Preparação
1. Anotar o estado atual do MySQL ou fazer backup antes de executar scripts de atualização. Nunca executar comandos `DROP DATABASE`, `TRUNCATE` ou `DELETE FROM categoria`.
2. Subir MySQL, backend `8080` e frontend `5173`, usando código atualizado da branch.
3. No site, criar conta exclusivamente de testes, com email que você reconhecerá no DBeaver, por exemplo `qa.etapas12@example.com` (se já existir, use outro). Salário líquido de teste: **R$ 2.000**. Não compartilhar a senha.
4. No DBeaver, abrir `database/03_conferir_persistencia.sql` e alterar a linha `SET @email_qa` para o email criado. **Somente SELECT**.

## Etapa 1 — cadastro, acesso e transações
- [ ] **E1.1 Cadastro**: criar conta com nome, email, senha válida e salário R$ 2.000; validar mensagem e fazer login. DBeaver: consulta 1 retorna **1 linha**, salário `2000.00`. Repetir tentativa com mesmo email deve ser rejeitada sem criar duplicata.
- [ ] **E1.2 Salário opcional**: no cadastro, verificar que a interface permite deixar salário vazio. Se quiser comprovar no banco, criar outra conta de testes sem salário; consulta 1 deve mostrar `NULL` para ela.
- [ ] **E1.3 Login e logout**: login com senha correta funciona; senha errada é rejeitada; logout volta à tela de login. Acesso a dados financeiros após logout deve exigir nova autenticação.
- [ ] **E1.4 Receita sem categoria**: cadastrar receita `QA Receita variável`, valor **R$ 3.000**, data do mês atual, categoria `Sem categoria`, recorrência desmarcada. Aparece no histórico; DBeaver consulta 2 mostra `RECEITA`, `3000.00`, `id_categoria` ausente (categoria exibida como NULL).
- [ ] **E1.5 Despesa categorizada**: cadastrar despesa `QA Mercado`, **R$ 400**, categoria **Alimentação**, data do mês atual, estabelecimento `Mercado QA`, recorrência desmarcada. DBeaver consulta 2 mostra categoria/valor/estabelecimento.
- [ ] **E1.6 Recorrência**: cadastrar despesa `QA Assinatura`, **R$ 100**, categoria **Assinaturas**, data do mês atual, marcar recorrente. No banco `recorrencia=1`. Dar **F5**: o valor continua marcado/visível.
- [ ] **E1.7 Validação**: tentar despesa sem categoria, valor zero, negativo e descrição vazia. Não devem resultar em novas transações no banco.
- [ ] **E1.8 Edição**: editar `QA Mercado` de R$ 400 para **R$ 450** e trocar categoria para **Compras**. F5 e consulta 2 precisam mostrar o novo valor e a nova categoria, sem criar linha extra.
- [ ] **E1.9 Filtros**: testar busca por descrição, tipo, categoria, data inicial/final, estabelecimento e recorrência. Cada filtro deve mostrar apenas transações correspondentes. Clicar em `Limpar filtros` restaura todos os registros.
- [ ] **E1.10 Excluir**: excluir somente `QA Assinatura`, confirmar e dar F5. Consulta 2 não deve mais mostrar essa linha. Não excluir transações reais.
- [ ] **E1.11 Dashboard e histórico**: total de receitas, despesas e saldo respondem às transações do usuário. Conferir depois do F5, sem transações de outra conta.
- [ ] **E1.12 Isolamento**: usar segunda conta QA; não deve visualizar as transações ou os orçamentos da primeira. A auditoria automatizada já comprovou 401/403, mas observar também a interface.

## Etapa 2 — orçamento, alertas, relatórios e renda
- [ ] **E2.1 Criar orçamento**: cadastrar limite **R$ 500** para **Compras**, onde ficou `QA Mercado` de R$ 450. Deve mostrar **90%**, saldo **R$ 50** e alerta **Risco**; DBeaver consultas 3 e 4 confirmam limite e gasto mensal.
- [ ] **E2.2 Atualização do orçamento**: cadastrar outra despesa `QA Compra extra`, **R$ 100**, categoria Compras, mesmo mês. Orçamento deve ir para **R$ 550**, **110%**, saldo **-R$ 50**, status **Estourado**. Conferir após F5 e DBeaver.
- [ ] **E2.3 Alterar/remover limite**: alterar orçamento da categoria Compras para R$ 1.000 (se a interface oferecer essa opção, deverá refletir no banco); depois remover somente o orçamento de testes e confirmar pela consulta 3. Não é para apagar despesas.
- [ ] **E2.4 Alertas nas 4 faixas**: os testes automatizados já validaram Normal (70%), Atenção (71%), Risco (90%) e Estourado (101%) com dados simulados. No site, conferir se textos/cores/porcentagem são legíveis.
- [ ] **E2.5 Regra 50/30/20**: com receita QA de R$ 3.000 no mês, a tela deve mostrar base de **R$ 3.000** (não R$ 5.000 por somar salário) e recomendações **R$ 1.500**, **R$ 900**, **R$ 600**. Dica: se outras receitas foram lançadas, calcular usando a soma do mês, não apenas o exemplo.
- [ ] **E2.6 Relatórios**: conferir entradas, saídas, saldo do mês atual, mês anterior e ranking por categoria; conferir conta/saldo com DBeaver, e que a virada do mês/ano não desloca datas.
- [ ] **E2.7 Histórico RN009**: clicar **uma única vez** em `Salvar relatório deste mês` na conta QA; conferir mensagem de sucesso e que o histórico recebe 1 novo item. Dar F5 ou novo login, item deve persistir. Consulta 5 mostra uma linha com `json_valido=1` e totais corretos.
- [ ] **E2.8 Sem dados**: na segunda conta QA vazia, dashboards/relatórios não devem apresentar `NaN`, `Infinity` ou travar; mensagem de ausência de dados deve ser clara.

## Visual e robustez
- [ ] Testar desktop, celular (375 px) e tablet (768 px), sem corte lateral; já houve auditoria automatizada de overflow, mas conferir se os botões são utilizáveis.
- [ ] Abrir DevTools (F12), guia Console: não devem aparecer erros relevantes em vermelho ao cadastrar, editar, salvar e consultar.
- [ ] No navegador, atualizar com F5 e fazer logout/login para confirmar persistência do MySQL.

## Aprovação e documentação
- Registrar **Passou/Falhou**, observação e prints somente dos casos que falharam.
- SQL de instalação para colegas: `database/01_instalacao_mysql8.sql` em uma instância MySQL 8 nova, sem dados de exemplo/senhas.
- SQL de atualização para uma base existente: `database/02_atualizacao_banco_existente.sql`; fazer backup e **conferir estrutura** antes de rodar.
- Consultas de verificação: `database/03_conferir_persistencia.sql`.
- **Etapa 3 fora deste aceite**: metas financeiras, score, investimento, recomendações avançadas e integração bancária.
- A execução dos roteiros manuais e a instalação em **outra máquina** ainda são pendentes: build e testes aprovados em uma máquina não demonstram portabilidade em todas.
