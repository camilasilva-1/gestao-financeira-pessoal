-- Etapa 1: permitir receitas sem categoria.
-- Fazer backup antes. O comando preserva todas as linhas existentes.
USE gestao_financeira;
ALTER TABLE transacao MODIFY COLUMN id_categoria INT NULL;
-- A regra de categoria obrigatoria para DESPESA continua validada no Spring Boot.
