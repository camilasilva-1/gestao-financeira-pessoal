import { useState, useEffect } from 'react';
import { Login } from './components/Login';
import { Cadastro } from './components/Cadastro';
import { EsqueciSenha } from './components/EsqueciSenha';
import { Dashboard } from './components/Dashboard';
import { Transacoes } from './components/Transacoes';
import { OrcamentosEMetas } from './components/OrcamentosEMetas';
import { Header } from './components/Header';
import type { Transacao, Orcamento, OrcamentoAPI, CategoriaAPI } from './types';
import { CATEGORIAS_PADRAO } from './types';
import { Relatorios } from './components/Relatorios';
import { ScoreCard } from './components/ScoreCard';
import { AlertasFinanceiros } from './components/AlertasFinanceiros';
import { EvolucaoPatrimonio } from './components/EvolucaoPatrimonio';


function App() {
  // Inicialização inteligente: se houver 'usuarioId' no localStorage, inicia direto em 'app'
  const [telaAtual, setTelaAtual] = useState<'login' | 'cadastro' | 'esqueci-senha' | 'app'>(() => {
    const usuarioSalvo = localStorage.getItem('usuarioId');
    return usuarioSalvo ? 'app' : 'login';
  });

  const [transacoes, setTransacoes] = useState<Transacao[]>([]);

  const [orcamentos, setOrcamentos] = useState<Orcamento[]>([
    { id: '1', categoria: 'Alimentação', limite: 1000, gasto: 0 },
    { id: '2', categoria: 'Lazer', limite: 500, gasto: 0 },
    { id: '3', categoria: 'Transporte', limite: 600, gasto: 0 }
  ]);

  const [categorias, setCategorias] = useState<string[]>(CATEGORIAS_PADRAO);

  const adicionarCategoria = (novaCat: string) => {
    if (!categorias.includes(novaCat)) {
      setCategorias([...categorias, novaCat]);
    }
  };

useEffect(() => {
  const buscarTransacoes = async () => {
    if (telaAtual !== 'app') return;

    try {
      const resposta = await fetch(
        'http://localhost:8080/api/transacoes',
        { credentials: 'include' }
      );

      if (!resposta.ok) {
        throw new Error('Erro ao buscar transações');
      }

      const dados = await resposta.json();

      const transacoesFormatadas: Transacao[] = dados.map((item: any) => ({
        id: String(item.idTransacao),
        descricao: item.descricao,
        valor: Number(item.valor),
        data: item.dataTransacao,
        tipo: item.tipo,
        categoria: item.categoria?.nome,
        establishment: item.estabelecimento,
        recorrente: item.recorrencia ?? false,
        usuarioId: item.usuario?.idUsuario,
        categoriaId: item.categoria?.idCategoria
      }));

      setTransacoes(transacoesFormatadas);
    } catch (erro) {
      console.error('Erro ao carregar transações:', erro);
    }
  };

  buscarTransacoes();
}, [telaAtual]);

const [, setCarregandoOrcamentos] = useState(true);

useEffect(() => {
  const buscarOrcamentos = async () => {
    setCarregandoOrcamentos(true);
    try {
      if (telaAtual !== 'app') return;
      const usuarioId = localStorage.getItem('usuarioId');

      if (!usuarioId) {
          setOrcamentos([]);
        return;
      }
      const resposta = await fetch(
        `http://localhost:8080/orcamentos/usuario/${usuarioId}`,
        { credentials: 'include' }
      );

      if (!resposta.ok) {
        throw new Error('Erro ao buscar orçamentos');
      }

      const dados: OrcamentoAPI[] = await resposta.json();


const orcamentosFormatados: Orcamento[] = await Promise.all(
  dados.map(async (orcamento) => {
    const respostaGasto = await fetch(
      `http://localhost:8080/orcamentos/${orcamento.idOrcamento}/gasto`,
      { credentials: 'include' }
    );

    if (!respostaGasto.ok) {
      throw new Error('Erro ao buscar gasto do orçamento');
    }

    const gasto = await respostaGasto.json();

    return {
      id: String(orcamento.idOrcamento),
      categoria: orcamento.categoria.nome,
      limite: Number(orcamento.limiteValor),
      gasto: Number(gasto),
    };
  })
);

      setOrcamentos(orcamentosFormatados);
    } catch (erro) {
      console.error('Erro ao carregar orçamentos:', erro);
    } finally {
      setCarregandoOrcamentos(false);
    }
  };

  buscarOrcamentos();
// Recalcular os gastos mensais quando as transacoes forem alteradas.
}, [telaAtual, transacoes]);

  const [categoriasAPI, setCategoriasAPI] = useState<CategoriaAPI[]>([]);

  useEffect(() => {
  const buscarCategorias = async () => {
    try {
      const resposta = await fetch('http://localhost:8080/categorias');

      if (!resposta.ok) {
        throw new Error('Erro ao buscar categorias');
      }

      const dados: CategoriaAPI[] = await resposta.json();
      setCategoriasAPI(dados);
    } catch (erro) {
      console.error('Erro ao carregar categorias:', erro);
    }
  };

  buscarCategorias();
}, []);
  // Função para fazer logout e limpar a sessão salva no navegador
  const handleLogout = () => {
    localStorage.removeItem('usuarioId');
    localStorage.removeItem('usuarioNome');
    setTelaAtual('login');
  };

  // Cálculo dinâmico do saldo para o Header em degradê
  const saldoTotal = transacoes.reduce((acc, t) => {
    return t.tipo === 'RECEITA' ? acc + t.valor : acc - t.valor;
  }, 0);

  // Renderização condicional das telas de entrada
  if (telaAtual === 'login') {
    return (
      <Login
        onLoginSuccess={() => setTelaAtual('app')}
        onNavigateToCadastro={() => setTelaAtual('cadastro')}
        onNavigateToEsqueciSenha={() => setTelaAtual('esqueci-senha')}
      />
    );
  }

  if (telaAtual === 'cadastro') {
    return (
      <Cadastro
        onCadastroSuccess={() => setTelaAtual('app')}
        onNavigateToLogin={() => setTelaAtual('login')}
      />
    );
  }

  if (telaAtual === 'esqueci-senha') {
    return (
      <EsqueciSenha
        onNavigateToLogin={() => setTelaAtual('login')}
      />
    );
  }

  // Renderização do sistema principal quando autenticado ('app')
  return (
    <div className="min-h-screen bg-slate-50 pb-12">
      {/* Header em degradê com Logout que limpa a sessão */}
      <Header 
        saldoTotal={saldoTotal} 
        onLogout={handleLogout} 
      />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6 -mt-6">
       <main className="space-y-6">
          {/* 1. Cards de Indicadores */}
          <Dashboard transacoes={transacoes} />

          {/* 2. Faixa do Score Financeiro */}
          <ScoreCard atualizarGatilho={transacoes} />

          {/* 3. Diagnósticos de Queda de Renda e Reserva de Emergência */}
          <AlertasFinanceiros atualizarGatilho={transacoes} />

          {/* 4. Evolução do Patrimônio */}
          <EvolucaoPatrimonio transacoes={transacoes} />

          {/* 5. Evolução Mensal e Ranking */}
          <Relatorios transacoes={transacoes} />

          {/* 6. Nova Transação e Histórico */}
          <Transacoes 
            transacoes={transacoes} 
            setTransacoes={setTransacoes} 
            categorias={categorias}
            adicionarCategoria={adicionarCategoria}
          />
          
         
         {/* 7. Orçamentos e Metas */}
          <OrcamentosEMetas 
            transacoes={transacoes} 
            orcamentos={orcamentos} 
            setOrcamentos={setOrcamentos} 
            categorias={categorias}
            categoriasAPI={categoriasAPI}
          />
        </main>
      </div>
    </div>
  );
}

export default App;