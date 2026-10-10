import React from 'react';
import type { Transacao } from '../types';

interface RelatoriosProps {
  transacoes: Transacao[];
}

export const Relatorios: React.FC<RelatoriosProps> = ({ transacoes }) => {
  const agora = new Date();
  const mesAtual = agora.getMonth();
  const anoAtual = agora.getFullYear();

  // Filtrar transações do mês atual e mês anterior
  const transacoesMesAtual = transacoes.filter(t => {
    const d = new Date(t.data);
    return d.getMonth() === mesAtual && d.getFullYear() === anoAtual;
  });

  const transacoesMesAnterior = transacoes.filter(t => {
    const d = new Date(t.data);
    const mesAnterior = mesAtual === 0 ? 11 : mesAtual - 1;
    const anoAnterior = mesAtual === 0 ? anoAtual - 1 : anoAtual;
    return d.getMonth() === mesAnterior && d.getFullYear() === anoAnterior;
  });

  // Totais Mês Atual (#12)
  const despesasMesAtual = transacoesMesAtual
    .filter(t => t.tipo === 'DESPESA')
    .reduce((acc, t) => acc + t.valor, 0);

  const receitasMesAtual = transacoesMesAtual
    .filter(t => t.tipo === 'RECEITA')
    .reduce((acc, t) => acc + t.valor, 0);

  // Totais Mês Anterior (#12)
  const despesasMesAnterior = transacoesMesAnterior
    .filter(t => t.tipo === 'DESPESA')
    .reduce((acc, t) => acc + t.valor, 0);

  // Comparativo percentual (#12)
  const diferencaDespesas = despesasMesAnterior > 0
    ? Math.round(((despesasMesAtual - despesasMesAnterior) / despesasMesAnterior) * 100)
    : 0;

  // Agrupamento e Ranking de Categorias (#14 e #15)
  const despesasPorCategoria: { [cat: string]: number } = {};
  transacoesMesAtual
    .filter(t => t.tipo === 'DESPESA')
    .forEach(t => {
      despesasPorCategoria[t.categoria] = (despesasPorCategoria[t.categoria] || 0) + t.valor;
    });

  const rankingCategorias = Object.entries(despesasPorCategoria)
    .map(([categoria, valor]) => ({
      categoria,
      valor,
      porcentagem: despesasMesAtual > 0 ? Math.round((valor / despesasMesAtual) * 100) : 0
    }))
    .sort((a, b) => b.valor - a.valor);

  return (
    <div className="space-y-6">
      {/* #12 - Evolução Mensal e Comparativo */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
          <span>📊</span> Evolução Mensal (Mês Atual vs Anterior)
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Card Despesas Comparativo */}
          <div className="bg-slate-50 p-4 rounded-xl border border-slate-200">
            <span className="text-xs font-semibold uppercase text-slate-500">Gastos do Mês Atual</span>
            <div className="flex items-baseline justify-between mt-1">
              <span className="text-2xl font-bold text-slate-800">R$ {despesasMesAtual.toFixed(2)}</span>
              {despesasMesAnterior > 0 && (
                <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                  diferencaDespesas <= 0 ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
                }`}>
                  {diferencaDespesas > 0 ? `+${diferencaDespesas}%` : `${diferencaDespesas}%`} vs mês anterior
                </span>
              )}
            </div>
            <p className="text-xs text-slate-400 mt-2">
              Mês anterior: R$ {despesasMesAnterior.toFixed(2)}
            </p>
          </div>

          {/* Card Receitas Comparativo */}
          <div className="bg-slate-50 p-4 rounded-xl border border-slate-200">
            <span className="text-xs font-semibold uppercase text-slate-500">Entradas do Mês Atual</span>
            <div className="flex items-baseline justify-between mt-1">
              <span className="text-2xl font-bold text-emerald-600">R$ {receitasMesAtual.toFixed(2)}</span>
              <span className="text-xs text-slate-500 font-medium">Balanço do Período</span>
            </div>
            <p className="text-xs text-slate-400 mt-2">
              Saldo líquido do mês: R$ {(receitasMesAtual - despesasMesAtual).toFixed(2)}
            </p>
          </div>
        </div>
      </div>

      {/* #14 & #15 - Relatório Mensal e Ranking de Categorias */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        <h2 className="text-lg font-bold text-slate-800 flex items-center gap-2">
          <span>🏆</span> Ranking de Gastos por Categoria
        </h2>

        {rankingCategorias.length === 0 ? (
          <p className="text-sm text-slate-400 py-4 text-center">Nenhuma despesa registrada neste mês.</p>
        ) : (
          <div className="space-y-4">
            {rankingCategorias.map((item, idx) => (
              <div key={item.categoria} className="space-y-1">
                <div className="flex justify-between items-center text-sm font-medium">
                  <span className="text-slate-700">
                    <strong className="text-slate-400 mr-2">#{idx + 1}</strong>
                    {item.categoria}
                  </span>
                  <span className="text-slate-900 font-bold">
                    R$ {item.valor.toFixed(2)} ({item.porcentagem}%)
                  </span>
                </div>
                {/* Barra de Progresso do Ranking */}
                <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                  <div 
                    className="bg-gradient-to-r from-[#c4003d] to-[#e60039] h-2.5 rounded-full transition-all duration-300"
                    style={{ width: `${item.porcentagem}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};