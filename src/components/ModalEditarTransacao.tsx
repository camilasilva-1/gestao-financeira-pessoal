import React, { useState, useEffect } from 'react';
import type { Transacao } from '../types';

interface ModalEditarTransacaoProps {
  transacao: Transacao | null;
  isOpen: boolean;
  onClose: () => void;
  onSave: (transacaoAtualizada: Transacao) => void;
  categorias: string[];
}

export const ModalEditarTransacao: React.FC<ModalEditarTransacaoProps> = ({
  transacao,
  isOpen,
  onClose,
  onSave,
  categorias
}) => {
  const [descricao, setDescricao] = useState('');
  const [valor, setValor] = useState('');
  const [data, setData] = useState('');
  const [tipo, setTipo] = useState<'RECEITA' | 'DESPESA'>('DESPESA');
  const [categoria, setCategoria] = useState('');
  const [establishment, setEstablishment] = useState('');

  useEffect(() => {
    if (transacao) {
      setDescricao(transacao.descricao);
      setValor(transacao.valor.toString());
      setData(transacao.data);
      setTipo(transacao.tipo);
      setCategoria(transacao.categoria || (categorias && categorias.length > 0 ? categorias[0] : 'Alimentação'));
      setEstablishment(transacao.establishment || '');
    }
  }, [transacao, categorias]);

  if (!isOpen || !transacao) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!descricao || !valor || !data) return;

    onSave({
      ...transacao,
      descricao,
      valor: parseFloat(valor),
      data,
      tipo,
      categoria,
      establishment: establishment.trim() ? establishment.trim() : undefined
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="bg-white rounded-xl shadow-lg max-w-md w-full p-6 space-y-4">
        <div className="flex justify-between items-center border-b pb-3">
          <h2 className="text-lg font-bold text-slate-800">Editar Transação</h2>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 font-bold">✕</button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-3">
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Descrição</label>
            <input
              type="text"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              className="w-full border rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Valor (R$)</label>
              <input
                type="number"
                step="0.01"
                value={valor}
                onChange={(e) => setValor(e.target.value)}
                className="w-full border rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Data</label>
              <input
                type="date"
                value={data}
                onChange={(e) => setData(e.target.value)}
                className="w-full border rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Tipo</label>
              <select
                value={tipo}
                onChange={(e) => setTipo(e.target.value as 'RECEITA' | 'DESPESA')}
                className="w-full border rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              >
                <option value="RECEITA">Receita</option>
                <option value="DESPESA">Despesa</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Categoria</label>
              <select
                value={categoria}
                onChange={(e) => setCategoria(e.target.value)}
                className="w-full border rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              >
                {categorias.map((cat) => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Estabelecimento (Opcional)</label>
            <input
              type="text"
              value={establishment}
              onChange={(e) => setEstablishment(e.target.value)}
              className="w-full border rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
            />
          </div>

          <div className="flex justify-end space-x-2 pt-3 border-t">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border text-slate-600 text-sm rounded-lg hover:bg-slate-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-red-600 text-white text-sm font-semibold rounded-lg hover:bg-red-700"
            >
              Salvar Alterações
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};