import React from 'react';
import type { Transacao } from '../types';

interface DashboardProps {
  transacoes: Transacao[];
}

export const Dashboard: React.FC<DashboardProps> = ({ transacoes }) => {
  const totalReceitas = transacoes
    .filter(t => t.tipo === 'RECEITA')
    .reduce((acc, t) => acc + t.valor, 0);

  const totalDespesas = transacoes
    .filter(t => t.tipo === 'DESPESA')
    .reduce((acc, t) => acc + t.valor, 0);

  const saldoAtual = totalReceitas - totalDespesas;
  
  const taxaPoupanca = totalReceitas > 0 
    ? Math.max(0, Math.round(((totalReceitas - totalDespesas) / totalReceitas) * 100))
    : 0;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Card 1: Saldo Total */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between hover:border-[#c4003d]/30 transition-colors">
        <div className="flex justify-between items-center text-slate-500 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider">Saldo Total</span>
          <span className="text-base">💳</span>
        </div>
        <div>
          <h3 className={`text-2xl font-bold ${saldoAtual >= 0 ? 'text-slate-800' : 'text-[#c4003d]'}`}>
            R$ {saldoAtual.toFixed(2)}
          </h3>
          <p className="text-xs text-slate-400 mt-1">Disponível em conta</p>
        </div>
      </div>

      {/* Card 2: Total de Receitas */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between hover:border-emerald-500/30 transition-colors">
        <div className="flex justify-between items-center text-slate-500 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-emerald-700">Entradas / Receitas</span>
          <span className="text-base">📈</span>
        </div>
        <div>
          <h3 className="text-2xl font-bold text-emerald-600">
            + R$ {totalReceitas.toFixed(2)}
          </h3>
          <p className="text-xs text-slate-400 mt-1">Ganhos acumulados</p>
        </div>
      </div>

      {/* Card 3: Total de Despesas */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between hover:border-[#c4003d]/30 transition-colors">
        <div className="flex justify-between items-center text-slate-500 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-[#c4003d]">Saídas / Despesas</span>
          <span className="text-base">📉</span>
        </div>
        <div>
          <h3 className="text-2xl font-bold text-[#c4003d]">
            - R$ {totalDespesas.toFixed(2)}
          </h3>
          <p className="text-xs text-slate-400 mt-1">Gastos computados</p>
        </div>
      </div>

      {/* Card 4: Taxa de Poupança */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex flex-col justify-between hover:border-slate-300 transition-colors">
        <div className="flex justify-between items-center text-slate-500 mb-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-600">Taxa de Poupança</span>
          <span className="text-base">🎯</span>
        </div>
        <div>
          <h3 className="text-2xl font-bold text-slate-800">
            {taxaPoupanca}%
          </h3>
          <p className="text-xs text-slate-400 mt-1">Da renda preservada</p>
        </div>
      </div>
    </div>
  );
};