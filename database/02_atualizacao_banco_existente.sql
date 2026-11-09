-- ATUALIZACAO CONSERVADORA PARA BANCO EXISTENTE - MySQL 8.0+
-- LEIA ANTES: faca BACKUP do schema gestao_financeira antes de ALTER TABLE.
-- Este arquivo NAO apaga nem modifica valores das linhas existentes.
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
    SET @alter_sql=CONCAT('ALTER TABLE transacao MODIFY COLUMN id_categoria ',v_tipo,' NULL');
    PREPARE cmd FROM @alter_sql; EXECUTE cmd; DEALLOCATE PREPARE cmd;
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
