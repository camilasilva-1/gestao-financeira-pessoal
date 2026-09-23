import { useState } from 'react';
import { Login } from './components/Login';
import { Cadastro } from './components/Cadastro';
import { EsqueciSenha } from './components/EsqueciSenha';
import { Dashboard } from './components/Dashboard';
import { Transacoes } from './components/Transacoes';
import { OrcamentosEMetas } from './components/OrcamentosEMetas';
import type { Transacao, Orcamento } from './types';
import { CATEGORIAS_PADRAO } from './types';

function App() {
  // Estado para controlar a navegação das telas
  const [telaAtual, setTelaAtual] = useState<'login' | 'cadastro' | 'esqueci-senha' | 'app'>('login');

  const [transacoes, setTransacoes] = useState<Transacao[]>([
    { id: '1', descricao: 'Salário Mensal', valor: 5000, data: '2026-09-05', tipo: 'RECEITA', categoria: 'Outros' },
    { id: '2', descricao: 'Supermercado', valor: 450, data: '2026-09-08', tipo: 'DESPESA', categoria: 'Alimentação', estabelecimento: 'Carrefour' },
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
    <div className="min-h-screen bg-slate-100 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-6">
        <header className="flex justify-between items-center bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <div>
            <h1 className="text-xl font-bold text-red-600">Organizando suas Finanças</h1>
            <p className="text-xs text-slate-500">Sistema de Gestão Financeira Pessoal</p>
          </div>
          
          <div className="flex items-center gap-4">
            <button
              onClick={() => setTelaAtual('login')}
              className="text-xs font-semibold text-slate-500 hover:text-red-600 border border-slate-200 px-3 py-1.5 rounded-lg transition-colors"
            >
              Sair
            </button>
            
          </div>
        </header>

        <main className="space-y-6">
          <Dashboard transacoes={transacoes} />

          <Transacoes 
            transacoes={transacoes} 
            setTransacoes={setTransacoes} 
            categorias={categorias}
            adicionarCategoria={adicionarCategoria}
          />
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