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


interface RecomendacaoFinanceira {
  tipo: string;
  mensagem: string;
}

// Usa somente as recomendações calculadas pelo backend.
function RecomendacoesFinanceiras({ transacoes }: { transacoes: Transacao[] }) {
  const [recomendacao, setRecomendacao] = useState<RecomendacaoFinanceira | null>(null);
  const [metas, setMetas] = useState<RecomendacaoFinanceira[]>([]);
  const [erro, setErro] = useState(false);

  useEffect(() => {
    const usuarioId = localStorage.getItem('usuarioId');
    if (!usuarioId) return;
    let ativo = true;

    Promise.all([
      fetch(`http://localhost:8080/insights/${usuarioId}/recomendacao-investimento`, { credentials: 'include' }),
      fetch(`http://localhost:8080/insights/${usuarioId}/metas-sugeridas`, { credentials: 'include' })
    ])
      .then(async ([resposta, respostaMetas]) => {
        if (!resposta.ok || !respostaMetas.ok) throw new Error('Falha ao consultar recomendações');
        return Promise.all([
          resposta.json() as Promise<RecomendacaoFinanceira>,
          respostaMetas.json() as Promise<RecomendacaoFinanceira[]>
        ]);
      })
      .then(([dados, lista]) => {
        if (!ativo) return;
        setRecomendacao(dados);
        setMetas(lista);
        setErro(false);
      })
      .catch(() => { if (ativo) setErro(true); });

    return () => { ativo = false; };
  }, [transacoes]);

  return (
    <section id="recomendacoes" className="scroll-mt-6 rounded-2xl border border-rose-200 bg-rose-50/70 p-6 shadow-sm space-y-3">
      <h2 className="text-lg font-bold text-[#c4003d]">Recomendações Financeiras</h2>
      {erro ? (
        <p className="text-sm text-slate-500">Não foi possível carregar as recomendações.</p>
      ) : !recomendacao ? (
        <p className="text-sm text-slate-500">Carregando recomendações...</p>
      ) : (
        <>
          <p className="text-sm text-slate-700">{recomendacao.mensagem}</p>
          {metas.length > 0 && (
            <div className="border-t border-rose-200 pt-3 space-y-2">
              <h3 className="text-sm font-semibold text-slate-800">Metas sugeridas</h3>
              {metas.map((meta, indice) => (
                <p key={indice} className="text-sm text-slate-600">• {meta.mensagem}</p>
              ))}
            </div>
          )}
        </>
      )}
    </section>
  );
}


function App() {
  // Inicialização inteligente: se houver 'usuarioId' no localStorage, inicia direto em 'app'
  const [telaAtual, setTelaAtual] = useState<'login' | 'cadastro' | 'esqueci-senha' | 'app'>(() => {
    const usuarioSalvo = localStorage.getItem('usuarioId');
    return usuarioSalvo ? 'app' : 'login';
  });

  const [verificandoSessao, setVerificandoSessao] = useState(
    Boolean(localStorage.getItem('usuarioId'))
  );

  useEffect(() => {
    const usuarioId = localStorage.getItem('usuarioId');
    if (!usuarioId) return;

    fetch(`http://localhost:8080/api/usuarios/${usuarioId}`, { credentials: 'include' })
      .then(resposta => {
        if (resposta.status === 401 || resposta.status === 403) {
          localStorage.removeItem('usuarioId');
          localStorage.removeItem('usuarioNome');
          setTelaAtual('login');
        }
      })
      .catch(erro => console.error('Não foi possível verificar a sessão:', erro))
      .finally(() => setVerificandoSessao(false));
  }, []);

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
    setTransacoes([]);
    setTelaAtual('login');
    fetch('http://localhost:8080/api/usuarios/logout', {
      method: 'POST',
      credentials: 'include',
      headers: { 'X-Requested-With': 'XMLHttpRequest' }
    }).catch(erro => console.error('Erro ao encerrar sessão:', erro));
  };

  // Cálculo dinâmico do saldo para o Header em degradê
  const saldoTotal = transacoes.reduce((acc, t) => {
    return t.tipo === 'RECEITA' ? acc + t.valor : acc - t.valor;
  }, 0);

  if (verificandoSessao) return <div className="min-h-screen flex items-center justify-center">Verificando sessão...</div>;

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
        onCadastroSuccess={() => setTelaAtual('login')}
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
          <div id="visao-geral" className="scroll-mt-6"><Dashboard transacoes={transacoes} /></div>

          {/* 2. Faixa do Score Financeiro */}
          <div id="score" className="scroll-mt-6"><ScoreCard atualizarGatilho={transacoes} /></div>

          {/* 3. Diagnósticos de Queda de Renda e Reserva de Emergência */}
          <div id="alertas" className="scroll-mt-6"><AlertasFinanceiros atualizarGatilho={transacoes} /></div>

          {/* 4. Evolução do Patrimônio */}
          <div id="patrimonio" className="scroll-mt-6"><EvolucaoPatrimonio transacoes={transacoes} /></div>

          {/* Recomendações e metas sugeridas do TakeOff 3.8 */}
          <RecomendacoesFinanceiras transacoes={transacoes} />

          {/* 5. Evolução Mensal e Ranking */}
          <Relatorios transacoes={transacoes} />

          {/* 6. Nova Transação e Histórico */}
          <Transacoes 
            transacoes={transacoes} 
            setTransacoes={setTransacoes} 
            categorias={categorias}
            categoriasAPI={categoriasAPI}
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