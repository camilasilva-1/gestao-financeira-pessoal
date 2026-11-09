-- VERIFICACAO DE PERSISTENCIA NO MYSQL (APENAS CONSULTAS)
-- No DBeaver, altere somente @email_qa para o email da sua conta de testes.
-- Nao imprime senha, nao altera linhas nem tabelas.
USE gestao_financeira;
SET @email_qa = 'qa.etapas12@example.com';

-- 1. Usuario e salario
SELECT id_usuario, nome, email, salario_liquido
FROM usuario WHERE email = @email_qa;

-- 2. Transacoes, inclusive receitas sem categoria e recorrencia
SELECT t.id_transacao,t.descricao,t.valor,t.data_transacao,t.tipo,
       c.nome AS categoria,t.estabelecimento,t.recorrencia
FROM transacao t
JOIN usuario u ON u.id_usuario=t.id_usuario
LEFT JOIN categoria c ON c.id_categoria=t.id_categoria
WHERE u.email=@email_qa
ORDER BY t.data_transacao DESC,t.id_transacao DESC;

-- 3. Limites de orcamento cadastrados
SELECT o.id_orcamento,c.nome AS categoria,o.limite_valor
FROM orcamento o
JOIN usuario u ON u.id_usuario=o.id_usuario
JOIN categoria c ON c.id_categoria=o.id_categoria
WHERE u.email=@email_qa
ORDER BY o.id_orcamento DESC;

-- 4. Gastos do mes atual por categoria
SELECT c.nome AS categoria,COALESCE(SUM(t.valor),0) AS gasto_mes_atual
FROM transacao t
JOIN usuario u ON u.id_usuario=t.id_usuario
JOIN categoria c ON c.id_categoria=t.id_categoria
WHERE u.email=@email_qa
  AND t.tipo='DESPESA'
  AND t.data_transacao >= DATE_FORMAT(CURDATE(),'%Y-%m-01')
  AND t.data_transacao < DATE_FORMAT(CURDATE() + INTERVAL 1 MONTH,'%Y-%m-01')
GROUP BY c.id_categoria,c.nome
ORDER BY gasto_mes_atual DESC;

-- 5. Historico salvo de relatorios (RN009)
SELECT h.id_historico,h.data_inicio,h.data_fim,h.gerado_em,
       h.total_receitas,h.total_despesas,h.saldo_final,
       JSON_VALID(h.relatorio_json) AS json_valido
FROM historico_relatorio_mensal h
JOIN usuario u ON u.id_usuario=h.id_usuario
WHERE u.email=@email_qa
ORDER BY h.gerado_em DESC;

-- 6. As 12 categorias obrigatorias
SELECT nome FROM categoria ORDER BY id_categoria;

-- 7. Conferencia de tabelas e estrutura (sem alteracoes)
SELECT table_name FROM information_schema.tables
WHERE table_schema='gestao_financeira' ORDER BY table_name;
SELECT table_name,column_name,column_type,is_nullable
FROM information_schema.columns
WHERE table_schema='gestao_financeira'
  AND table_name IN ('usuario','categoria','transacao','orcamento','historico_relatorio_mensal')
ORDER BY table_name,ordinal_position;
