import { useState } from 'react';
import { Login } from './components/Login';
import { Cadastro } from './components/Cadastro';
import { EsqueciSenha } from './components/EsqueciSenha';
import { Dashboard } from './components/Dashboard';
import { Transacoes } from './components/Transacoes';
import { OrcamentosEMetas } from './components/OrcamentosEMetas';
import { Header } from './components/Header';
import type { Transacao, Orcamento } from './types';
import { CATEGORIAS_PADRAO } from './types';
import { Relatorios } from './components/Relatorios';

function App() {
  // Estado para controlar a navegação das telas
  const [telaAtual, setTelaAtual] = useState<'login' | 'cadastro' | 'esqueci-senha' | 'app'>('login');

  const [transacoes, setTransacoes] = useState<Transacao[]>([
    { id: '1', descricao: 'Salário Mensal', valor: 5000, data: '2026-09-05', tipo: 'RECEITA', categoria: 'Outros' },
    { id: '2', descricao: 'Supermercado', valor: 450, data: '2026-09-08', tipo: 'DESPESA', categoria: 'Alimentação', establishment: 'Carrefour' },
    { id: '3', descricao: 'Cinema e Pipoca', valor: 120, data: '2026-09-10', tipo: 'DESPESA', categoria: 'Lazer' }
  ]);

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
          <Transacoes 
            transacoes={transacoes} 
            setTransacoes={setTransacoes} 
            categorias={categorias}
            adicionarCategoria={adicionarCategoria}
          />
          
          {/* 4. Orçamentos e Metas */}
          <OrcamentosEMetas 
            transacoes={transacoes} 
            orcamentos={orcamentos} 
            setOrcamentos={setOrcamentos} 
            categorias={categorias}
          />
        </main>
      </div>
    </div>
  );
}

export default App;