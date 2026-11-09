-- ATUALIZACAO CONSERVADORA PARA BANCO EXISTENTE - MySQL 8.0+
-- LEIA ANTES: faca BACKUP do schema gestao_financeira antes de ALTER TABLE.
-- Este arquivo NAO apaga nem modifica valores das linhas existentes.
-- Se a FK de transacao.id_categoria existir, ela sera recriada mantendo suas regras.
-- DDL nao e transacional: backup e obrigatorio em banco com dados.
-- Requer privilegio ALTER e CREATE ROUTINE. Roda apos 01_instalacao_mysql8.sql.
-- Pode falhar se seu esquema antigo tiver colunas/FKs incompativeis: nesse
-- caso pare e inspecione SHOW CREATE TABLE; NAO remova tabelas ou FKs a cegas.
USE gestao_financeira;

DELIMITER $$
DROP PROCEDURE IF EXISTS _atualizar_colunas_etapas12_20261108 $$
CREATE PROCEDURE _atualizar_colunas_etapas12_20261108()
BEGIN
  DECLARE v_tipo VARCHAR(255);
  DECLARE v_nullable VARCHAR(3);
  DECLARE v_fk_nome VARCHAR(64) DEFAULT NULL;
  DECLARE v_acao_delete VARCHAR(30) DEFAULT NULL;
  DECLARE v_acao_update VARCHAR(30) DEFAULT NULL;

  -- Usuario: salario opcional; senha exigida pela autenticação do backend.
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'senha'
  ) THEN
    ALTER TABLE usuario ADD COLUMN senha VARCHAR(255) NULL;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
     WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'salario_liquido'
  ) THEN
    ALTER TABLE usuario ADD COLUMN salario_liquido DECIMAL(19,2) NULL;
  ELSE
    SELECT column_type, is_nullable INTO v_tipo,v_nullable
    FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name='usuario' AND column_name='salario_liquido';
    IF v_nullable = 'NO' THEN
      SET @alter_sql=CONCAT('ALTER TABLE usuario MODIFY COLUMN salario_liquido ',v_tipo,' NULL');
      PREPARE cmd FROM @alter_sql; EXECUTE cmd; DEALLOCATE PREPARE cmd;
    END IF;
  END IF;

  -- Campos opcionais da transacao.
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='transacao' AND column_name='estabelecimento'
  ) THEN
    ALTER TABLE transacao ADD COLUMN estabelecimento VARCHAR(255) NULL;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='transacao' AND column_name='recorrencia'
  ) THEN
    ALTER TABLE transacao ADD COLUMN recorrencia TINYINT(1) NULL;
  END IF;

  -- Receita sem categoria é válida; despesa precisa de categoria no backend.
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='transacao'
      AND column_name='id_categoria' AND is_nullable='NO'
  ) THEN
    SELECT column_type INTO v_tipo FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='transacao'
      AND column_name='id_categoria';
    -- MySQL nao permite modificar uma coluna enquanto ela participa de uma FK.
    -- Identificar e reconstruir somente a FK da categoria, preservando as regras.
    SELECT k.constraint_name, r.delete_rule, r.update_rule
      INTO v_fk_nome, v_acao_delete, v_acao_update
    FROM information_schema.key_column_usage k
    JOIN information_schema.referential_constraints r
      ON r.constraint_schema = k.constraint_schema
      AND r.table_name = k.table_name
      AND r.constraint_name = k.constraint_name
    WHERE k.table_schema = DATABASE() AND k.table_name='transacao'
      AND k.column_name='id_categoria' AND k.referenced_table_name='categoria'
    LIMIT 1;

    IF v_fk_nome IS NOT NULL THEN
      SET @alter_sql=CONCAT('ALTER TABLE transacao DROP FOREIGN KEY `',
         REPLACE(v_fk_nome,'`','``'),'`');
      PREPARE cmd FROM @alter_sql; EXECUTE cmd; DEALLOCATE PREPARE cmd;
    END IF;

    SET @alter_sql=CONCAT('ALTER TABLE transacao MODIFY COLUMN id_categoria ',v_tipo,' NULL');
    PREPARE cmd FROM @alter_sql; EXECUTE cmd; DEALLOCATE PREPARE cmd;

    IF v_fk_nome IS NOT NULL THEN
      SET @alter_sql=CONCAT('ALTER TABLE transacao ADD CONSTRAINT `',
        REPLACE(v_fk_nome,'`','``'),
        '` FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)',
        ' ON DELETE ',v_acao_delete,' ON UPDATE ',v_acao_update);
      PREPARE cmd FROM @alter_sql; EXECUTE cmd; DEALLOCATE PREPARE cmd;
    END IF;
  END IF;
END $$
CALL _atualizar_colunas_etapas12_20261108() $$
DROP PROCEDURE IF EXISTS _atualizar_colunas_etapas12_20261108 $$
DELIMITER ;

-- SOMENTE LEITURA: se id_categoria = YES, receita pode ficar sem categoria.
SELECT table_name,column_name,column_type,is_nullable
FROM information_schema.columns
WHERE table_schema=DATABASE()
  AND ((table_name='usuario' AND column_name IN ('senha','salario_liquido'))
    OR (table_name='transacao' AND column_name IN ('id_categoria','estabelecimento','recorrencia')))
ORDER BY table_name,column_name;

-- OBS: linhas antigas de usuario sem senha continuam sem autenticação.
-- Nunca inserir senhas em texto puro: o backend armazena hash BCrypt.
-- Dados antigos inconsistentes/duplicados exigem migração específica, não exclusão.
