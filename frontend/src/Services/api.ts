const API_BASE_URL = 'http://localhost:8080/api';

// 1. Função de Login
interface UsuarioLogin {
  idUsuario: number;
  id?: number;
  nome: string;
  email: string;
  salarioLiquido: number;
}

export const loginUsuario = async (
  credentials: { email: string; senha: string }
): Promise<UsuarioLogin> => {
  const response = await fetch(`${API_BASE_URL}/usuarios/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    throw new Error('Falha na autenticação. Verifique o e-mail e a senha.');
  }

  return response.json();
};

// 2. Função de Registo/Cadastro
export const cadastrarUsuario = async (usuarioData: { nome: string; email: string; senha: string; salarioLiquido: number }) => {
  const response = await fetch(`${API_BASE_URL}/usuarios`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
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
  categoriaId: number;
  usuarioId: number;
  dataTransacao: string;
}) => {
  const response = await fetch(`${API_BASE_URL}/transacoes`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
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
  const response = await fetch(`${API_BASE_URL}/transacoes/usuario/${usuarioId}`);

  if (!response.ok) {
    throw new Error('Erro ao procurar as transações.');
  }

  return response.json();
};

// 5. Atualizar uma Transação
export const atualizarTransacao = async (
  id: string,
  usuarioId: number,
  transacaoData: {
    descricao: string;
    valor: number;
    tipo: 'RECEITA' | 'DESPESA';
    dataTransacao: string;
    estabelecimento?: string;
    recorrencia?: boolean;
    categoria: { idCategoria: number };
  }
) => {
  const response = await fetch(
    `${API_BASE_URL}/transacoes/${id}/usuario/${usuarioId}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(transacaoData),
    }
  );

  if (!response.ok) {
    throw new Error('Erro ao atualizar transação');
  }

  return response.json();
};

// 6. Listar categorias
export const listarCategorias = async () => {
  const response = await fetch(`${API_BASE_URL.replace('/api', '')}/categorias`);

  if (!response.ok) {
    throw new Error('Erro ao buscar categorias.');
  }

  return response.json();
};