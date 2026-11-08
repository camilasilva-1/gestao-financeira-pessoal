-- Etapa 1: padronizar categorias exigidas no Takeoff
-- Execute no MySQL somente apos backup e conferencia dos dados.
-- Nao executa DELETE geral, nao renumera chaves primarias e nao altera transacoes.
USE gestao_financeira;

-- Restaurar Moradia se ausente; preserva categorias existentes.
INSERT INTO categoria (id_categoria, nome)
SELECT 2, 'Moradia'
WHERE NOT EXISTS (
  SELECT 1 FROM categoria WHERE id_categoria = 2 OR nome = 'Moradia'
);

-- Remover apenas o registro invalido identificado nos testes locais,
-- e somente se nao houver transacoes ou orcamentos relacionados.
DELETE FROM categoria
WHERE id_categoria = 14
  AND nome IS NULL
  AND NOT EXISTS (SELECT 1 FROM transacao WHERE id_categoria = 14)
  AND NOT EXISTS (SELECT 1 FROM orcamento WHERE id_categoria = 14);

-- Conferencia final: devem existir 12 categorias com nomes preenchidos.
SELECT id_categoria, nome
FROM categoria
ORDER BY id_categoria;
