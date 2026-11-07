export type TipoTransacao = 'RECEITA' | 'DESPESA';

export interface Transacao {
  id: string;
  descricao: string;
  valor: number;
  data: string;
  tipo: 'RECEITA' | 'DESPESA';
  categoria?: string;
  establishment?: string;
  recorrente?: boolean;
}

  export interface TransacaoAPI {
    idTransacao: number;
    descricao: string;
    valor: number;
    dataTransacao: string;
    tipo: 'RECEITA' | 'DESPESA';
    estabelecimento?: string;
    recorrencia?: boolean;
    usuario: {
      idUsuario: number;
    };
    categoria: {
      idCategoria: number;
      nome: string;
    };
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


export interface OrcamentoAPI {
  idOrcamento: number;
  limiteValor: number;
  usuario: {
    idUsuario: number;
    nome: string;
    email: string;
    salarioLiquido: number;
  };
  categoria: {
    idCategoria: number;
    nome: string;
  };
}

export interface CategoriaAPI {
  idCategoria: number;
  nome: string;
}