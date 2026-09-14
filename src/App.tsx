import { useState } from 'react';
import { Transacoes } from './components/Transacoes';
import { OrcamentosEMetas } from './components/OrcamentosEMetas';
import type { Transacao, Orcamento } from './types';
import { CATEGORIAS_PADRAO } from './types';

function App() {
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

  return (
    <div className="min-h-screen bg-slate-100 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-6">
        <header className="flex justify-between items-center bg-white p-4 rounded-xl border border-slate-200 shadow-sm">
          <div>
            <h1 className="text-xl font-bold text-red-600">Bradesco Finanças</h1>
            <p className="text-xs text-slate-500">Sistema de Gestão Financeira Pessoal</p>
          </div>
          
          <img 
            src="/bradesco-logo.png" 
            alt="Logo Bradesco" 
            className="h-10 w-auto object-contain"
          />
        </header>

        <main className="space-y-6">
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