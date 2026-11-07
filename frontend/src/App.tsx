import { useEffect, useState } from 'react';
import { Login } from './components/Login';
import { Cadastro } from './components/Cadastro';
import { EsqueciSenha } from './components/EsqueciSenha';
import { Dashboard } from './components/Dashboard';
import { Transacoes } from './components/Transacoes';
import { OrcamentosEMetas } from './components/OrcamentosEMetas';
import { Header } from './components/Header';
import type { Transacao, Orcamento, TransacaoAPI, OrcamentoAPI, CategoriaAPI} from './types';
import { CATEGORIAS_PADRAO } from './types';
import { Relatorios } from './components/Relatorios';

function App() {
  // Estado para controlar a navegação das telas
  const [telaAtual, setTelaAtual] = useState<'login' | 'cadastro' | 'esqueci-senha' | 'app'>('login');

  const [transacoes, setTransacoes] = useState<Transacao[]>([]);
  const [carregandoTransacoes, setCarregandoTransacoes] = useState(true);
  
  useEffect(() => {
  const buscarTransacoes = async () => {
    try {
      const resposta = await fetch('http://localhost:8080/transacoes');

      if (!resposta.ok) {
        throw new Error('Erro ao buscar transações');
      }

      const dados: TransacaoAPI[] = await resposta.json();

      const transacoesFormatadas: Transacao[] = dados.map((t) => ({
        id: String(t.idTransacao),
        descricao: t.descricao,
        valor: Number(t.valor),
        data: t.dataTransacao,
        tipo: t.tipo,
        categoria: t.categoria.nome,
        establishment: t.estabelecimento,
        recorrente: t.recorrencia,
      }));

      setTransacoes(transacoesFormatadas);
    } catch (erro) {
      console.error('Erro ao carregar transações:', erro);
    } finally {
      setCarregandoTransacoes(false);
    }
  };

  buscarTransacoes();
}, []);

  const [orcamentos, setOrcamentos] = useState<Orcamento[]>([]);
const [carregandoOrcamentos, setCarregandoOrcamentos] = useState(true);

useEffect(() => {
  const buscarOrcamentos = async () => {
    try {
      const resposta = await fetch('http://localhost:8080/orcamentos');

      if (!resposta.ok) {
        throw new Error('Erro ao buscar orçamentos');
      }

      const dados: OrcamentoAPI[] = await resposta.json();

      
const orcamentosFormatados: Orcamento[] = await Promise.all(
  dados.map(async (orcamento) => {
    const respostaGasto = await fetch(
      `http://localhost:8080/orcamentos/${orcamento.idOrcamento}/gasto`
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
}, []);

  const [categorias, setCategorias] = useState<string[]>(CATEGORIAS_PADRAO);
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

  const adicionarCategoria = (novaCat: string) => {
    if (!categorias.includes(novaCat)) {
      setCategorias([...categorias, novaCat]);
    }
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
  // Renderização do sistema principal quando autenticado ('app')
  return (
    <div className="min-h-screen bg-slate-50 pb-12">
      {/* Header em degradê */}
      <Header 
        saldoTotal={saldoTotal} 
        onLogout={() => setTelaAtual('login')} 
      />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6 -mt-6">
        <main className="space-y-6">
          {/* 1. Cards de Indicadores */}
          <Dashboard transacoes={transacoes} />

          {/* 2. Evolução Mensal e Ranking */}
          <Relatorios transacoes={transacoes} />

          {/* 3. Nova Transação e Histórico */}
          {carregandoTransacoes ? (
          <p className="text-center text-slate-500 py-4">
          Carregando transações...
          </p>
          ) : (
          <Transacoes
            transacoes={transacoes}
            setTransacoes={setTransacoes}
            categorias={categorias}
            adicionarCategoria={adicionarCategoria}
          />
          )}
          
          {/* 4. Orçamentos e Metas */}
          {carregandoOrcamentos ? (
          <p className="text-center text-slate-500 py-4">
          Carregando orçamentos...
          </p>
          ) : (
          <OrcamentosEMetas
              transacoes={transacoes}
              orcamentos={orcamentos}
              setOrcamentos={setOrcamentos}
              categorias={categorias}
              categoriasAPI={categoriasAPI}
          />
          )}
        </main>
      </div>
    </div>
  );
}

export default App;