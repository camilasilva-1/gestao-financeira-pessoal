import React, { useState } from 'react';
import type { Transacao, Orcamento } from '../types';

export interface Meta {
  id: string;
  nome: string;
  valorAlvo: number;
  valorAtual: number;
  dataLimite: string;
}

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
  categorias,
}) => {
  // Estados para Metas
  const [metas, setMetas] = useState<Meta[]>([
    { id: '1', nome: 'Reserva de Emergência', valorAlvo: 10000, valorAtual: 4500, dataLimite: '2027-12-31' },
    { id: '2', nome: 'Viagem de Férias', valorAlvo: 3000, valorAtual: 2100, dataLimite: '2026-12-15' },
  ]);

  const [nomeMeta, setNomeMeta] = useState('');
  const [valorAlvoMeta, setValorAlvoMeta] = useState('');
  const [valorAtualMeta, setValorAtualMeta] = useState('');
  const [dataLimiteMeta, setDataLimiteMeta] = useState('');

  // Estados para Orçamentos
  const [categoriaSel, setCategoriaSel] = useState('');
  const [limiteOrcamento, setLimiteOrcamento] = useState('');

  // Estado para o Modal Personalizado (Substitui o prompt nativo)
  const [modalAberto, setModalAberto] = useState(false);
  const [metaSelecionada, setMetaSelecionada] = useState<Meta | null>(null);
  const [tipoAcaoModal, setTipoAcaoModal] = useState<'guardar' | 'resgatar'>('guardar');
  const [valorModal, setValorModal] = useState('');

  // Adicionar Nova Meta
  const handleAdicionarMeta = (e: React.FormEvent) => {
    e.preventDefault();
    if (!nomeMeta || !valorAlvoMeta || !dataLimiteMeta) return;

    const novaMeta: Meta = {
      id: Date.now().toString(),
      nome: nomeMeta,
      valorAlvo: parseFloat(valorAlvoMeta),
      valorAtual: valorAtualMeta ? parseFloat(valorAtualMeta) : 0,
      dataLimite: dataLimiteMeta,
    };

    setMetas([...metas, novaMeta]);
    setNomeMeta('');
    setValorAlvoMeta('');
    setValorAtualMeta('');
    setDataLimiteMeta('');
  };

  // Remover Meta
  const handleRemoverMeta = (id: string) => {
    setMetas(metas.filter((m) => m.id !== id));
  };

  // Abrir o Modal Personalizado
  const abrirModal = (meta: Meta, tipo: 'guardar' | 'resgatar') => {
    setMetaSelecionada(meta);
    setTipoAcaoModal(tipo);
    setValorModal('');
    setModalAberto(true);
  };

  // Confirmar ação do Modal
  const handleConfirmarModal = (e: React.FormEvent) => {
    e.preventDefault();
    const val = parseFloat(valorModal);

    if (isNaN(val) || val <= 0 || !metaSelecionada) return;

    setMetas(
      metas.map((meta) => {
        if (meta.id === metaSelecionada.id) {
          const novoValor =
            tipoAcaoModal === 'guardar'
              ? meta.valorAtual + val
              : Math.max(0, meta.valorAtual - val);
          return { ...meta, valorAtual: novoValor };
        }
        return meta;
      })
    );

    setModalAberto(false);
    setMetaSelecionada(null);
  };

  // Definir ou atualizar Limite de Orçamento
  const handleDefinirOrcamento = (e: React.FormEvent) => {
    e.preventDefault();
    if (!categoriaSel || !limiteOrcamento) return;

    const limiteNum = parseFloat(limiteOrcamento);
    const orcamentoExistente = orcamentos.find((o) => o.categoria === categoriaSel);

    if (orcamentoExistente) {
      setOrcamentos(
        orcamentos.map((o) =>
          o.categoria === categoriaSel ? { ...o, limite: limiteNum } : o
        )
      );
    } else {
      setOrcamentos([
        ...orcamentos,
        { id: Date.now().toString(), categoria: categoriaSel, limite: limiteNum, gasto: 0 },
      ]);
    }

    setLimiteOrcamento('');
  };

  const handleRemoverOrcamento = (id: string) => {
    setOrcamentos(orcamentos.filter((o) => o.id !== id));
  };

  // Calcular gastos por categoria
  const calcularGasto = (categoria: string) => {
    return transacoes
      .filter((t) => t.tipo === 'DESPESA' && t.categoria === categoria)
      .reduce((acc, t) => acc + t.valor, 0);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {/* SEÇÃO 1: ORÇAMENTOS POR CATEGORIA */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        <h2 className="text-lg font-bold text-slate-800">Orçamentos por Categoria</h2>

        <form onSubmit={handleDefinirOrcamento} className="flex gap-2">
          <select
            value={categoriaSel}
            onChange={(e) => setCategoriaSel(e.target.value)}
            className="flex-1 rounded-xl border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#c4003d]"
          >
            <option value="">Selecione uma categoria...</option>
            {categorias.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
          <input
            type="number"
            step="0.01"
            placeholder="Limite R$"
            value={limiteOrcamento}
            onChange={(e) => setLimiteOrcamento(e.target.value)}
            className="w-28 rounded-xl border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#c4003d]"
          />
          <button
            type="submit"
            className="bg-slate-900 text-white px-4 py-2 rounded-xl text-xs font-bold hover:bg-slate-800 transition cursor-pointer"
          >
            Definir
          </button>
        </form>

        <div className="space-y-3 pt-2">
          {orcamentos.map((orc) => {
            const gastoAtual = calcularGasto(orc.categoria);
            const porc = Math.min(100, Math.round((gastoAtual / orc.limite) * 100));
            const resta = orc.limite - gastoAtual;

            let statusColor = 'bg-emerald-100 text-emerald-700';
            let barColor = 'bg-emerald-500';
            let statusText = 'Normal';

            if (porc >= 80 && porc < 100) {
              statusColor = 'bg-amber-100 text-amber-700';
              barColor = 'bg-amber-500';
              statusText = 'Atenção';
            } else if (porc >= 100) {
              statusColor = 'bg-red-100 text-red-700';
              barColor = 'bg-red-600';
              statusText = 'Excedido';
            }

            return (
              <div key={orc.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
                <div className="flex justify-between items-center">
                  <span className="font-semibold text-sm text-slate-800">{orc.categoria}</span>
                  <div className="flex items-center gap-2">
                    <span className={`text-xs px-2 py-0.5 rounded-full font-bold ${statusColor}`}>
                      {statusText} ({porc}%)
                    </span>
                    <button
                      onClick={() => handleRemoverOrcamento(orc.id)}
                      className="text-slate-400 hover:text-red-600 text-xs font-bold"
                    >
                      ✕
                    </button>
                  </div>
                </div>

                <div className="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
                  <div className={`${barColor} h-2 rounded-full transition-all`} style={{ width: `${porc}%` }} />
                </div>

                <div className="flex justify-between text-xs text-slate-500">
                  <span>Gasto: R$ {gastoAtual.toFixed(2)} / R$ {orc.limite.toFixed(2)}</span>
                  <span>Resta: R$ {resta > 0 ? resta.toFixed(2) : '0.00'}</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* SEÇÃO 2: METAS FINANCEIRAS */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        <h2 className="text-lg font-bold text-slate-800">Metas Financeiras</h2>

        <form onSubmit={handleAdicionarMeta} className="space-y-2">
          <input
            type="text"
            placeholder="Nome da Meta (Ex: Carro Novo) *"
            value={nomeMeta}
            onChange={(e) => setNomeMeta(e.target.value)}
            className="w-full rounded-xl border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#c4003d]"
          />
          <div className="grid grid-cols-2 gap-2">
            <input
              type="number"
              step="0.01"
              placeholder="Valor Alvo (R$) *"
              value={valorAlvoMeta}
              onChange={(e) => setValorAlvoMeta(e.target.value)}
              className="rounded-xl border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#c4003d]"
            />
            <input
              type="number"
              step="0.01"
              placeholder="Valor Atual (R$)"
              value={valorAtualMeta}
              onChange={(e) => setValorAtualMeta(e.target.value)}
              className="rounded-xl border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#c4003d]"
            />
          </div>
          <div className="flex gap-2">
            <input
              type="date"
              value={dataLimiteMeta}
              onChange={(e) => setDataLimiteMeta(e.target.value)}
              className="flex-1 rounded-xl border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#c4003d]"
            />
            <button
              type="submit"
              className="bg-[#c4003d] text-white px-4 py-2 rounded-xl text-xs font-bold hover:bg-[#a00032] transition cursor-pointer"
            >
              Adicionar Meta
            </button>
          </div>
        </form>

        <div className="space-y-3 pt-2">
          {metas.map((meta) => {
            const porcMeta = Math.min(100, Math.round((meta.valorAtual / meta.valorAlvo) * 100));

            return (
              <div key={meta.id} className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
                <div className="flex justify-between items-center">
                  <div>
                    <h3 className="font-semibold text-sm text-slate-800">{meta.nome}</h3>
                    <p className="text-[10px] text-slate-400">Data limite: {meta.dataLimite}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs px-2 py-0.5 rounded-full font-bold bg-red-100 text-[#c4003d]">
                      {porcMeta}% atingido
                    </span>
                    <button
                      onClick={() => handleRemoverMeta(meta.id)}
                      className="text-slate-400 hover:text-red-600 text-xs font-bold"
                    >
                      ✕
                    </button>
                  </div>
                </div>

                <div className="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
                  <div
                    className="bg-[#c4003d] h-2 rounded-full transition-all"
                    style={{ width: `${porcMeta}%` }}
                  />
                </div>

                <div className="flex justify-between items-center text-xs pt-1">
                  <span className="text-slate-500">
                    Guardado: <strong>R$ {meta.valorAtual.toFixed(2)}</strong> / Alvo: R${' '}
                    {meta.valorAlvo.toFixed(2)}
                  </span>
                  <div className="flex gap-1">
                    <button
                      onClick={() => abrirModal(meta, 'guardar')}
                      className="bg-emerald-600 hover:bg-emerald-700 text-white text-[11px] font-semibold px-2.5 py-1 rounded-lg transition cursor-pointer"
                    >
                      + Guardar
                    </button>
                    <button
                      onClick={() => abrirModal(meta, 'resgatar')}
                      className="bg-slate-200 hover:bg-slate-300 text-slate-700 text-[11px] font-semibold px-2.5 py-1 rounded-lg transition cursor-pointer"
                    >
                      - Resgatar
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* MODAL PERSONALIZADO DENTRO DA APLICAÇÃO */}
      {modalAberto && metaSelecionada && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-sm w-full p-6 shadow-2xl space-y-4 animate-in fade-in zoom-in duration-150">
            <div className="border-b border-slate-100 pb-2">
              <h3 className="text-base font-bold text-slate-800">
                {tipoAcaoModal === 'guardar' ? 'Guardar Dinheiro' : 'Resgatar Dinheiro'}
              </h3>
              <p className="text-xs text-slate-500">
                Meta: <strong>{metaSelecionada.nome}</strong>
              </p>
            </div>

            <form onSubmit={handleConfirmarModal} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Quanto deseja {tipoAcaoModal}? (R$)
                </label>
                <input
                  type="number"
                  step="0.01"
                  autoFocus
                  required
                  placeholder="0,00"
                  value={valorModal}
                  onChange={(e) => setValorModal(e.target.value)}
                  className="w-full rounded-xl border border-slate-300 px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-[#c4003d]"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setModalAberto(false)}
                  className="px-4 py-2 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-100 transition cursor-pointer"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className={`px-4 py-2 rounded-xl text-xs font-bold text-white shadow-md transition cursor-pointer ${
                    tipoAcaoModal === 'guardar'
                      ? 'bg-emerald-600 hover:bg-emerald-700'
                      : 'bg-[#c4003d] hover:bg-[#a00032]'
                  }`}
                >
                  Confirmar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};