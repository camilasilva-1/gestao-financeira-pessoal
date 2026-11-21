import { useEffect, useState } from 'react';
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
import { listarTransacoes } from './Services/api';


function App() {
  // Inicialização inteligente: se houver 'usuarioId' no localStorage, inicia direto em 'app'
  const [telaAtual, setTelaAtual] = useState<'login' | 'cadastro' | 'esqueci-senha' | 'app'>(() => {
    const usuarioSalvo = localStorage.getItem('usuarioId');
    return usuarioSalvo ? 'app' : 'login';
  });

  const [transacoes, setTransacoes] = useState<Transacao[]>([]);

  const [orcamentos, setOrcamentos] = useState<Orcamento[]>([]);

  const [categorias, setCategorias] = useState<string[]>(CATEGORIAS_PADRAO);

  const adicionarCategoria = (novaCat: string) => {
    if (!categorias.includes(novaCat)) {
      setCategorias([...categorias, novaCat]);
    }
  };


  const [categoriasAPI, setCategoriasAPI] = useState<CategoriaAPI[]>([]);

  useEffect(() => {
    if (telaAtual !== 'app') return;
    const usuarioId = localStorage.getItem('usuarioId');
    if (!usuarioId) return;

    listarTransacoes(Number(usuarioId)).then((dados) => {
      if (!Array.isArray(dados)) return;
      setTransacoes(dados.map((t) => ({
        id: String(t.idTransacao ?? t.id),
        descricao: t.descricao,
        valor: Number(t.valor),
        data: t.dataTransacao ?? t.data,
        tipo: t.tipo,
        categoria: typeof t.categoria === 'object' ? t.categoria?.nome : t.categoria,
        establishment: t.estabelecimento,
        recorrente: t.recorrencia
      })));
    }).catch((erro) => console.error('Erro ao carregar transações:', erro));

    fetch('http://localhost:8080/categorias', { credentials: 'include' })
      .then((r) => { if (!r.ok) throw new Error('Categorias'); return r.json(); })
      .then((dados: CategoriaAPI[]) => setCategoriasAPI(dados))
      .catch((erro) => console.error('Erro ao carregar categorias:', erro));
  }, [telaAtual]);

  useEffect(() => {
    if (telaAtual !== 'app') return;
    const usuarioId = localStorage.getItem('usuarioId');
    if (!usuarioId) return;

    fetch(`http://localhost:8080/orcamentos/usuario/${usuarioId}`, { credentials: 'include' })
      .then(async (r) => {
        if (!r.ok) throw new Error('Orçamentos');
        const dados: OrcamentoAPI[] = await r.json();
        return Promise.all(dados.map(async (orcamento) => {
          const gastoResp = await fetch(
            `http://localhost:8080/orcamentos/${orcamento.idOrcamento}/gasto`,
            { credentials: 'include' }
          );
          if (!gastoResp.ok) throw new Error('Gasto do orçamento');
          const gasto = await gastoResp.json();
          return {
            id: String(orcamento.idOrcamento),
            categoria: orcamento.categoria.nome,
            limite: Number(orcamento.limiteValor),
            gasto: Number(gasto)
          };
        }));
      })
      .then(setOrcamentos)
      .catch((erro) => console.error('Erro ao carregar orçamentos:', erro));
  }, [telaAtual, transacoes]);

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