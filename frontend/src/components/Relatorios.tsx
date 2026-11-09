import React, { useEffect, useState } from 'react';
import type { Transacao } from '../types';

interface Regra503020 {
  rendaTotal: number;
  idealNecessidades: number;
  idealDesejos: number;
  idealPoupanca: number;
}

interface RelatorioSalvo {
  idHistorico: number;
  inicio: string;
  fim: string;
  geradoEm: string;
  receitaTotal: number;
  despesaTotal: number;
  saldoFinal: number;
}

interface RelatoriosProps {
  transacoes: Transacao[];
}

export const Relatorios: React.FC<RelatoriosProps> = ({ transacoes }) => {
  const [regra, setRegra] = useState<Regra503020 | null>(null);
  const [erroRegra, setErroRegra] = useState(false);
  const [historicoRelatorios, setHistoricoRelatorios] = useState<RelatorioSalvo[]>([]);
  const [salvandoRelatorio, setSalvandoRelatorio] = useState(false);
  const [mensagemRelatorio, setMensagemRelatorio] = useState('');

  const buscarHistorico = async () => {
    const id = localStorage.getItem('usuarioId');
    if (!id) return;
    const resposta = await fetch(`http://localhost:8080/api/relatorios/historico?usuarioId=${id}`, {
      credentials: 'include'
    });
    if (!resposta.ok) throw new Error('Falha na consulta ao histórico');
    setHistoricoRelatorios(await resposta.json());
  };

  useEffect(() => {
    buscarHistorico().catch(() => setMensagemRelatorio('Histórico indisponível no momento.'));
  }, []);

  const salvarRelatorioDoMes = async () => {
    const id = localStorage.getItem('usuarioId');
    if (!id || salvandoRelatorio) return;
    const hoje = new Date();
    const inicio = `${hoje.getFullYear()}-${String(hoje.getMonth()+1).padStart(2,'0')}-01`;
    const fim = `${hoje.getFullYear()}-${String(hoje.getMonth()+1).padStart(2,'0')}-${String(new Date(hoje.getFullYear(), hoje.getMonth()+1, 0).getDate()).padStart(2,'0')}`;
    setSalvandoRelatorio(true);
    setMensagemRelatorio('');
    try {
      const params = new URLSearchParams({ usuarioId: id, inicio, fim });
      const resposta = await fetch(`http://localhost:8080/api/relatorios/mensal/arquivar?${params}`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      });
      if (!resposta.ok) throw new Error('Falha ao salvar relatório');
      await buscarHistorico();
      setMensagemRelatorio('Relatório mensal salvo no histórico.');
    } catch {
      setMensagemRelatorio('Não foi possível salvar o relatório mensal.');
    } finally {
      setSalvandoRelatorio(false);
    }
  };

  useEffect(() => {
    const id = localStorage.getItem('usuarioId');
    if (!id) return;
    const controller = new AbortController();
    fetch(`http://localhost:8080/api/relatorios/regra-50-30-20/${id}`, {
      credentials: 'include',
      signal: controller.signal
    })
      .then(async r => {
        if (!r.ok) throw new Error('Não foi possível consultar a regra');
        return r.json();
      })
      .then(d => {
        setRegra({
          rendaTotal: Number(d.rendaTotal),
          idealNecessidades: Number(d.idealNecessidades),
          idealDesejos: Number(d.idealDesejos),
          idealPoupanca: Number(d.idealPoupanca)
        });
        setErroRegra(false);
      })
      .catch(e => {
        if (e.name !== 'AbortError') setErroRegra(true);
      });
    return () => controller.abort();
  }, [transacoes]);

  const moeda = (valor: number) => valor.toLocaleString('pt-BR', {
    style: 'currency', currency: 'BRL'
  });

  const agora = new Date();
  const mesAtual = agora.getMonth();
  const anoAtual = agora.getFullYear();

  // Filtrar transações do mês atual e mês anterior
  const transacoesMesAtual = transacoes.filter(t => {
    const d = new Date(t.data + 'T00:00:00');
    return d.getMonth() === mesAtual && d.getFullYear() === anoAtual;
  });

  const transacoesMesAnterior = transacoes.filter(t => {
    const d = new Date(t.data + 'T00:00:00');
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
      const categoria = t.categoria || 'Sem categoria';

      despesasPorCategoria[categoria] =
        (despesasPorCategoria[categoria] || 0) + t.valor;
    });

  const totalDespesas = despesasMesAtual;
  const rankingCategorias = Object.entries(despesasPorCategoria)
    .map(([categoria, valor]) => ({
      categoria,
      valor,
      porcentagem: totalDespesas > 0
  ? Math.round((valor / totalDespesas) * 100)
  : 0
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

      {/* Regra 50/30/20 — seção 3.7 do Takeoff, dados da API */}
      <section className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3" aria-label="Planejamento pela regra 50/30/20">
        <h2 className="text-lg font-bold text-slate-800">Planejamento 50/30/20</h2>
        {regra ? (
          <>
            <p className="text-sm text-slate-600">
              Base: receitas registradas neste mês — <strong>{moeda(regra.rendaTotal)}</strong>.
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {([
                ['50% Necessidades', regra.idealNecessidades],
                ['30% Desejos', regra.idealDesejos],
                ['20% Reserva e investimentos', regra.idealPoupanca]
              ] as const).map(([titulo, valor]) => (
                <div key={titulo} className="rounded-xl bg-slate-50 p-4 border border-slate-200">
                  <p className="text-xs font-semibold text-slate-500">{titulo}</p>
                  <p className="text-lg font-bold text-slate-800">{moeda(valor)}</p>
                </div>
              ))}
            </div>
            {regra.rendaTotal === 0 && (
              <p className="text-xs text-slate-500">Registre receitas no mês para calcular os valores sugeridos.</p>
            )}
          </>
        ) : (
          <p className="text-sm text-slate-500">{erroRegra
            ? 'Não foi possível consultar o planejamento. Verifique a conexão com o backend.'
            : 'Carregando planejamento...'}</p>
        )}
      </section>

      {/* RN009: histórico salvo somente sob comando da pessoa usuária. */}
      <section className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3" aria-label="Histórico de relatórios mensais">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <h2 className="text-lg font-bold text-slate-800">Histórico de Relatórios Mensais</h2>
          <button type="button" disabled={salvandoRelatorio} onClick={salvarRelatorioDoMes}
            className="bg-slate-900 text-white text-xs font-bold rounded-xl px-4 py-2 disabled:opacity-50">
            {salvandoRelatorio ? 'Salvando...' : 'Salvar relatório deste mês'}
          </button>
        </div>
        <p className="text-xs text-slate-500">O relatório é registrado somente quando você seleciona Salvar.</p>
        {mensagemRelatorio && <p className="text-sm text-slate-600" role="status">{mensagemRelatorio}</p>}
        {historicoRelatorios.length === 0 ? (
          <p className="text-sm text-slate-500">Nenhum relatório mensal salvo.</p>
        ) : (
          <div className="space-y-2">
            {historicoRelatorios.map(item => (
              <div key={item.idHistorico} className="bg-slate-50 rounded-xl border border-slate-200 p-3 text-sm">
                <div className="flex flex-col sm:flex-row sm:justify-between gap-1">
                  <strong className="text-slate-700">{item.inicio.slice(0,7)} — gerado em {new Date(item.geradoEm).toLocaleString('pt-BR')}</strong>
                  <span className="text-slate-600">Saldo: {moeda(Number(item.saldoFinal))}</span>
                </div>
                <p className="text-xs text-slate-500">
                  Receitas: {moeda(Number(item.receitaTotal))} · Despesas: {moeda(Number(item.despesaTotal))}
                </p>
              </div>
            ))}
          </div>
        )}
      </section>

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