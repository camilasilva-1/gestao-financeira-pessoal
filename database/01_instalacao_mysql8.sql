-- SISTEMA DE GESTAO FINANCEIRA PESSOAL - BRADESCO / BYTEBANK
-- INSTALACAO INICIAL - MySQL 8.0+ / utf8mb4
-- Validado contra entidades JPA da branch fix/correcoes-etapa-2 em 08/11/2026.
-- NUNCA usa DROP DATABASE/TABLE, DELETE, TRUNCATE ou altera credenciais.
-- Para banco ja existente: executar primeiro diagnostico 03 e verificar o upgrade 02.
-- IMPORTANTE: CREATE TABLE IF NOT EXISTS nao corrige estrutura antiga divergente.

CREATE DATABASE IF NOT EXISTS gestao_financeira
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gestao_financeira;

CREATE TABLE IF NOT EXISTS usuario (
  id_usuario INT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(255),
  email VARCHAR(255) NOT NULL,
  senha VARCHAR(255),
  salario_liquido DECIMAL(19,2) NULL,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categoria (
  id_categoria INT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(255) NOT NULL,
  PRIMARY KEY (id_categoria),
  UNIQUE KEY uk_categoria_nome (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS transacao (
  id_transacao INT NOT NULL AUTO_INCREMENT,
  descricao VARCHAR(255),
  valor DECIMAL(19,2),
  data_transacao DATE,
  tipo ENUM('RECEITA','DESPESA'),
  estabelecimento VARCHAR(255),
  recorrencia TINYINT(1),
  id_usuario INT NOT NULL,
  id_categoria INT NULL,
  PRIMARY KEY (id_transacao),
  KEY idx_transacao_usuario_data (id_usuario,data_transacao),
  KEY idx_transacao_categoria (id_categoria),
  CONSTRAINT fk_transacao_usuario FOREIGN KEY (id_usuario)
      REFERENCES usuario(id_usuario),
  CONSTRAINT fk_transacao_categoria FOREIGN KEY (id_categoria)
      REFERENCES categoria(id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orcamento (
  id_orcamento INT NOT NULL AUTO_INCREMENT,
  limite_valor DECIMAL(19,2),
  id_usuario INT NOT NULL,
  id_categoria INT NOT NULL,
  PRIMARY KEY (id_orcamento),
  KEY idx_orcamento_usuario (id_usuario),
  KEY idx_orcamento_categoria (id_categoria),
  CONSTRAINT fk_orcamento_usuario FOREIGN KEY (id_usuario)
      REFERENCES usuario(id_usuario),
  CONSTRAINT fk_orcamento_categoria FOREIGN KEY (id_categoria)
      REFERENCES categoria(id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Entidade JPA presente no backend para compatibilidade com o projeto.
-- A persistencia de metas na interface pertence a um escopo posterior.
CREATE TABLE IF NOT EXISTS meta_financeira (
  id_meta INT NOT NULL AUTO_INCREMENT,
  nome VARCHAR(255),
  valor_objetivo DOUBLE,
  valor_acumulado DOUBLE,
  data_desejada DATE,
  percentual_atingido DOUBLE,
  valor_mensal_necessario DOUBLE,
  dias_restantes BIGINT,
  usuario_id INT NULL,
  PRIMARY KEY (id_meta),
  KEY idx_meta_usuario (usuario_id),
  CONSTRAINT fk_meta_usuario FOREIGN KEY (usuario_id)
      REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RN009: historico de relatorios mensais salvos por escolha do usuario.
CREATE TABLE IF NOT EXISTS historico_relatorio_mensal (
  id_historico INT NOT NULL AUTO_INCREMENT,
  id_usuario INT NOT NULL,
  data_inicio DATE NOT NULL,
  data_fim DATE NOT NULL,
  gerado_em DATETIME(6) NOT NULL,
  total_receitas DECIMAL(16,2) NOT NULL,
  total_despesas DECIMAL(16,2) NOT NULL,
  saldo_final DECIMAL(16,2) NOT NULL,
  relatorio_json LONGTEXT NOT NULL,
  PRIMARY KEY (id_historico),
  KEY idx_historico_usuario_gerado (id_usuario,gerado_em),
  CONSTRAINT fk_historico_usuario FOREIGN KEY (id_usuario)
      REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Exatamente 12 categorias do Takeoff. Insercao idempotente.
INSERT INTO categoria (nome)
SELECT 'Alimentação' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Alimentação');

INSERT INTO categoria (nome)
SELECT 'Moradia' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Moradia');

INSERT INTO categoria (nome)
SELECT 'Transporte' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Transporte');

INSERT INTO categoria (nome)
SELECT 'Saúde' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Saúde');

INSERT INTO categoria (nome)
SELECT 'Educação' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Educação');

INSERT INTO categoria (nome)
SELECT 'Lazer' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Lazer');

INSERT INTO categoria (nome)
SELECT 'Compras' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Compras');

INSERT INTO categoria (nome)
SELECT 'Investimentos' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Investimentos');

INSERT INTO categoria (nome)
SELECT 'Assinaturas' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Assinaturas');

INSERT INTO categoria (nome)
SELECT 'Impostos' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Impostos');

INSERT INTO categoria (nome)
SELECT 'Contas e Serviços' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Contas e Serviços');

INSERT INTO categoria (nome)
SELECT 'Outros' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Outros');

-- Conferencia simples (somente leitura)
SELECT id_categoria,nome FROM categoria ORDER BY id_categoria;
SELECT table_name FROM information_schema.tables
 WHERE table_schema='gestao_financeira' ORDER BY table_name;
