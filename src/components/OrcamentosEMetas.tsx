import React, { useState } from 'react';
import type { Orcamento, Meta, Transacao } from '../types';

interface OrcamentosEMetasProps {
  transacoes: Transacao[];
  orcamentos: Orcamento[];
  setOrcamentos: React.Dispatch<React.SetStateAction<Orcamento[]>>;
  categorias: string[];
}

export const OrcamentosEMetas: React.FC<OrcamentosEMetasProps> = ({ 
  transacoes, 
  orcamentos, 
  setOrcamentos,
  categorias
}) => {
  const [metas, setMetas] = useState<Meta[]>([
    { id: '1', titulo: 'Reserva de Emergência', valorAlvo: 10000, valorAtual: 4500, dataLimite: '2027-12-31' },
    { id: '2', titulo: 'Viagem de Férias', valorAlvo: 3000, valorAtual: 2100, dataLimite: '2026-12-15' }
  ]);

  // Estados dos formulários
  const [categoriaSel, setCategoriaSel] = useState('Alimentação');
  const [limiteInput, setLimiteInput] = useState('');

  const [tituloMeta, setTituloMeta] = useState('');
  const [valorAlvoMeta, setValorAlvoMeta] = useState('');
  const [valorAtualMeta, setValorAtualMeta] = useState('');
  const [dataLimiteMeta, setDataLimiteMeta] = useState('');

  // Orçamentos
  const handleAddOrcamento = (e: React.FormEvent) => {
    e.preventDefault();
    const val = parseFloat(limiteInput);
    if (isNaN(val) || val <= 0) return;

    setOrcamentos([
      ...orcamentos,
      { id: String(Date.now()), categoria: categoriaSel, limite: val, gasto: 0 }
    ]);
    setLimiteInput('');
  };

  const handleDeleteOrcamento = (id: string) => {
    setOrcamentos(orcamentos.filter(o => o.id !== id));
  };

  // Metas
  const handleAddMeta = (e: React.FormEvent) => {
    e.preventDefault();
    const alvo = parseFloat(valorAlvoMeta);
    const atual = parseFloat(valorAtualMeta) || 0;

    if (!tituloMeta.trim() || isNaN(alvo) || alvo <= 0 || !dataLimiteMeta) {
      alert('Preencha os campos obrigatórios da meta.');
      return;
    }

    setMetas([...metas, {
      id: String(Date.now()),
      titulo: tituloMeta.trim(),
      valorAlvo: alvo,
      valorAtual: atual,
      dataLimite: dataLimiteMeta
    }]);

    setTituloMeta('');
    setValorAlvoMeta('');
    setValorAtualMeta('');
    setDataLimiteMeta('');
  };

  const handleDeleteMeta = (id: string) => {
    setMetas(metas.filter(m => m.id !== id));
  };

  // Função para Adicionar ou Retirar dinheiro da Meta
  const handleMovimentarMeta = (id: string, tipo: 'ADICIONAR' | 'RETIRAR') => {
    const quantiaStr = prompt(
      tipo === 'ADICIONAR' 
        ? 'Quanto deseja guardar nesta meta? (R$)' 
        : 'Quanto deseja resgatar desta meta? (R$)'
    );

    if (!quantiaStr) return;
    const quantia = parseFloat(quantiaStr.replace(',', '.'));

    if (isNaN(quantia) || quantia <= 0) {
      alert('Informe um valor válido maior que zero.');
      return;
    }

    setMetas(metas.map(meta => {
      if (meta.id === id) {
        let novoValor = tipo === 'ADICIONAR' 
          ? meta.valorAtual + quantia 
          : meta.valorAtual - quantia;

        if (novoValor < 0) {
          alert('Você não pode resgatar mais do que o valor guardado.');
          return meta;
        }

        return { ...meta, valorAtual: novoValor };
      }
      return meta;
    }));
  };

  const calcularGastoReal = (categoria: string) => {
    return transacoes
      .filter(t => t.tipo === 'DESPESA' && t.categoria?.toLowerCase() === categoria.toLowerCase())
      .reduce((soma, t) => soma + t.valor, 0);
  };

  const calcularStatus = (gasto: number, limite: number) => {
    const perc = Math.round((gasto / limite) * 100);
    const restante = limite - gasto;

    if (perc <= 70) {
      return { status: 'Normal', tagClass: 'bg-emerald-100 text-emerald-700 border-emerald-300', barClass: 'bg-emerald-500', perc, restante };
    } else if (perc <= 89) {
      return { status: 'Atenção', tagClass: 'bg-amber-100 text-amber-800 border-amber-300', barClass: 'bg-amber-500', perc, restante };
    } else if (perc <= 100) {
      return { status: 'Risco', tagClass: 'bg-orange-100 text-orange-800 border-orange-300', barClass: 'bg-orange-500', perc, restante };
    } else {
      return { status: 'Estourado', tagClass: 'bg-red-100 text-red-800 border-red-300', barClass: 'bg-red-600', perc, restante };
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      
      {/* SEÇÃO DE ORÇAMENTOS */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <h2 className="text-lg font-bold text-slate-800">Orçamentos por Categoria </h2>
        
        <form onSubmit={handleAddOrcamento} className="flex gap-2">
          <select
            value={categoriaSel}
            onChange={e => setCategoriaSel(e.target.value)}
            className="flex-1 border border-slate-300 rounded-lg p-2 text-xs bg-white focus:outline-none focus:border-red-600"
          >
            {categorias.map(c => <option key={c} value={c}>{c}</option>)}
          </select>

          <input
            type="number"
            step="0.01"
            placeholder="Limite R$"
            value={limiteInput}
            onChange={e => setLimiteInput(e.target.value)}
            className="w-28 border border-slate-300 rounded-lg p-2 text-xs focus:outline-none focus:border-red-600"
            required
          />

          <button type="submit" className="bg-slate-800 text-white px-3 py-2 rounded-lg text-xs font-semibold hover:bg-slate-900 cursor-pointer transition-colors">
            Definir
          </button>
        </form>

        <div className="space-y-3 pt-2">
          {orcamentos.length === 0 ? (
            <p className="text-xs text-slate-400 text-center py-4">Nenhum limite orçamentário definido.</p>
          ) : (
            orcamentos.map(orc => {
              const gastoTotal = calcularGastoReal(orc.categoria);
              const { status, tagClass, barClass, perc, restante } = calcularStatus(gastoTotal, orc.limite);

              return (
                <div key={orc.id} className="p-3 bg-slate-50 rounded-lg border border-slate-200 text-sm">
                  <div className="flex justify-between items-center mb-1">
                    <span className="font-semibold text-slate-700">{orc.categoria}</span>
                    <div className="flex items-center gap-2">
                      <span className={`text-xs px-2 py-0.5 rounded-full border font-bold ${tagClass}`}>
                        {status} ({perc}%)
                      </span>
                      <button
                        type="button"
                        onClick={() => handleDeleteOrcamento(orc.id)}
                        className="text-slate-400 hover:text-red-600 text-xs font-bold px-1 transition-colors cursor-pointer"
                        title="Excluir orçamento"
                      >
                        ✕
                      </button>
                    </div>
                  </div>

                  <div className="w-full bg-slate-200 h-2.5 rounded-full overflow-hidden my-2">
                    <div className={`h-full ${barClass}`} style={{ width: `${Math.min(perc, 100)}%` }} />
                  </div>

                  <div className="flex justify-between text-xs text-slate-500">
                    <span>Gasto: R$ {gastoTotal.toFixed(2)} / R$ {orc.limite.toFixed(2)}</span>
                    <span className={restante < 0 ? 'text-red-600 font-bold' : ''}>
                      {restante >= 0 ? `Resta: R$ ${restante.toFixed(2)}` : `Excedido: R$ ${Math.abs(restante).toFixed(2)}`}
                    </span>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* SEÇÃO DE METAS */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <h2 className="text-lg font-bold text-slate-800">Metas Financeiras </h2>
        
        <form onSubmit={handleAddMeta} className="grid grid-cols-2 gap-2 bg-slate-50 p-3 rounded-lg border border-slate-200">
          <input
            type="text"
            placeholder="Nome da Meta (Ex: Carro) *"
            value={tituloMeta}
            onChange={e => setTituloMeta(e.target.value)}
            className="col-span-2 border border-slate-300 rounded-lg p-2 text-xs bg-white focus:outline-none focus:border-red-600"
            required
          />
          <input
            type="number"
            step="0.01"
            placeholder="Valor Alvo (R$) *"
            value={valorAlvoMeta}
            onChange={e => setValorAlvoMeta(e.target.value)}
            className="border border-slate-300 rounded-lg p-2 text-xs bg-white focus:outline-none focus:border-red-600"
            required
          />
          <input
            type="number"
            step="0.01"
            placeholder="Valor Atual (R$)"
            value={valorAtualMeta}
            onChange={e => setValorAtualMeta(e.target.value)}
            className="border border-slate-300 rounded-lg p-2 text-xs bg-white focus:outline-none focus:border-red-600"
          />
          <input
            type="date"
            value={dataLimiteMeta}
            onChange={e => setDataLimiteMeta(e.target.value)}
            className="border border-slate-300 rounded-lg p-2 text-xs bg-white focus:outline-none focus:border-red-600"
            required
          />
          <button
            type="submit"
            className="bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-semibold py-2 transition-colors cursor-pointer"
          >
            Adicionar Meta
          </button>
        </form>

        <div className="space-y-3 pt-2">
          {metas.length === 0 ? (
            <p className="text-xs text-slate-400 text-center py-4">Nenhuma meta cadastrada.</p>
          ) : (
            metas.map(meta => {
              const progresso = Math.min(Math.round((meta.valorAtual / meta.valorAlvo) * 100), 100);
              return (
                <div key={meta.id} className="p-4 border border-slate-200 rounded-lg bg-slate-50 text-sm space-y-2">
                  <div className="flex justify-between items-center">
                    <div>
                      <h3 className="font-semibold text-slate-800">{meta.titulo}</h3>
                      <p className="text-xs text-slate-400">Data limite: {meta.dataLimite}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-red-600 bg-red-50 border border-red-200 px-2 py-0.5 rounded-md">
                        {progresso}% atingido
                      </span>
                      <button
                        type="button"
                        onClick={() => handleDeleteMeta(meta.id)}
                        className="text-slate-400 hover:text-red-600 text-xs font-bold px-1 transition-colors cursor-pointer"
                        title="Excluir meta"
                      >
                        ✕
                      </button>
                    </div>
                  </div>

                  <div className="w-full bg-slate-200 h-2.5 rounded-full overflow-hidden">
                    <div className="h-full bg-red-600 transition-all duration-300" style={{ width: `${progresso}%` }} />
                  </div>

                  <div className="flex justify-between items-center text-xs text-slate-500 pt-1">
                    <div>
                      <span>Guardado: <strong>R$ {meta.valorAtual.toFixed(2)}</strong></span>
                      <span className="mx-1">/</span>
                      <span>Alvo: R$ {meta.valorAlvo.toFixed(2)}</span>
                    </div>

                    {/* Botões de Movimentação */}
                    <div className="flex gap-1.5">
                      <button
                        type="button"
                        onClick={() => handleMovimentarMeta(meta.id, 'ADICIONAR')}
                        className="bg-emerald-600 hover:bg-emerald-700 text-white px-2.5 py-1 rounded text-xs font-semibold cursor-pointer transition-colors"
                        title="Depositar valor na meta"
                      >
                        + Guardar
                      </button>
                      <button
                        type="button"
                        onClick={() => handleMovimentarMeta(meta.id, 'RETIRAR')}
                        className="bg-slate-200 hover:bg-slate-300 text-slate-700 px-2.5 py-1 rounded text-xs font-semibold cursor-pointer transition-colors"
                        title="Retirar valor da meta"
                      >
                        - Resgatar
                      </button>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

    </div>
  );
};