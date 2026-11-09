-- CORRECAO PONTUAL RN009 - erro HTTP 500 / MySQL 1406 (Data too long).
-- Preserva TODOS os registros existentes; nao usa DELETE, DROP ou TRUNCATE.
-- Executar com o backend parado e apos backup, em banco gestao_financeira.
-- A coluna vai comportar o JSON completo do relatorio mensal.
USE gestao_financeira;

-- 1. Confirmar a estrutura atual ANTES da alteracao:
SELECT column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'historico_relatorio_mensal'
  AND column_name = 'relatorio_json';

-- 2. Corrigir a coluna: nenhuma linha existente e apagada.
ALTER TABLE historico_relatorio_mensal
  MODIFY COLUMN relatorio_json LONGTEXT NOT NULL;

-- 3. Confirmar a estrutura corrigida:
SELECT column_name, column_type, is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name = 'historico_relatorio_mensal'
  AND column_name = 'relatorio_json';
