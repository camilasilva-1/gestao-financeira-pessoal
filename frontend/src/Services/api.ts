const API_BASE_URL = 'http://localhost:8080/api';

// 1. Função de Login
// 1. Função de Login
export const loginUsuario = async (credentials: { email: string; senha: string }) => {
  const response = await fetch(`${API_BASE_URL}/usuarios/login`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
    },
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    throw new Error('Falha na autenticação. Verifique o e-mail e a palavra-passe.');
  }

  return response.json();
};

// 2. Função de Registo/Cadastro
export const cadastrarUsuario = async (usuarioData: { nome: string; email: string; senha: string }) => {
  const response = await fetch(`${API_BASE_URL}/usuarios`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
    },
    body: JSON.stringify(usuarioData),
  });

  if (!response.ok) {
    throw new Error('Erro ao cadastrar utilizador.');
  }

  return response.json();
};

// 3. Cadastrar nova Transação (Receita ou Despesa)
export const cadastrarTransacao = async (transacaoData: {
  descricao: string;
  valor: number;
  tipo: 'RECEITA' | 'DESPESA';
  categoriaId: number | null;
  usuarioId: number;
  dataTransacao: string;
  estabelecimento?: string;
  recorrencia?: boolean;
}) => {
  const response = await fetch(`${API_BASE_URL}/transacoes`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
    },
    body: JSON.stringify(transacaoData),
  });

  if (!response.ok) {
    throw new Error('Erro ao registar a transação.');
  }

  return response.json();
};

// 4. Listar todas as Transações do Utilizador
export const listarTransacoes = async (usuarioId: number) => {
  const response = await fetch(`${API_BASE_URL}/transacoes/usuario/${usuarioId}`, {
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Erro ao procurar as transações.');
  }

  return response.json();
};
