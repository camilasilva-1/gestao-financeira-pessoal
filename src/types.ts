export type TipoTransacao = 'RECEITA' | 'DESPESA';

export interface Transacao {
  id: string;
  descricao: string;
  valor: number;
  data: string;
  tipo: TipoTransacao;
  categoria?: string;
  estabelecimento?: string;
  recorrente?: boolean;
}

// Lista oficial das categorias padrão exigidas no requisito 3.2
export const CATEGORIAS_PADRAO = [
  'Alimentação',
  'Moradia',
  'Transporte',
  'Saúde',
  'Educação',
  'Lazer',
  'Compras',
  'Investimentos',
  'Assinaturas',
  'Impostos',
  'Contas e Serviços',
  'Outros'
];

export interface Orcamento {
  id: string;
  categoria: string;
  limite: number;
  gasto: number;
}

export interface Meta {
  id: string;
  titulo: string;
  valorAlvo: number;
  valorAtual: number;
  dataLimite: string;
}