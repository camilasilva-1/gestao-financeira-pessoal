import React, { useState } from 'react';
import type { Transacao, TipoTransacao } from '../types';

interface TransacoesProps {
  transacoes: Transacao[];
  setTransacoes: React.Dispatch<React.SetStateAction<Transacao[]>>;
  categorias: string[];
  adicionarCategoria: (novaCat: string) => void;
}

export const Transacoes: React.FC<TransacoesProps> = ({ 
  transacoes, 
  setTransacoes, 
  categorias,
  adicionarCategoria
}) => {
  const [descricao, setDescricao] = useState('');
  const [valor, setValor] = useState('');
  const [data, setData] = useState(new Date().toISOString().split('T')[0]);
  const [tipo, setTipo] = useState<TipoTransacao>('DESPESA');
  const [categoria, setCategoria] = useState('Alimentação');
  const [outraCategoriaInput, setOutraCategoriaInput] = useState('');
  const [estabelecimento, setEstabelecimento] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const valorNumerico = parseFloat(valor);
    if (isNaN(valorNumerico) || valorNumerico <= 0) {
      alert('O valor da transação deve ser maior que zero.');
      return;
    }

    if (!descricao.trim()) {
      alert('A descrição é obrigatória.');
      return;
    }

    // Se o usuário selecionou "Outros", usa o nome digitado no input
    let categoriaFinal = categoria;
    if (categoria === 'Outros') {
      const nomeFormatado = outraCategoriaInput.trim();
      if (!nomeFormatado) {
        alert('Por favor, informe o nome da nova categoria.');
        return;
      }
      categoriaFinal = nomeFormatado;
      // Salva a nova categoria na lista global do sistema
      adicionarCategoria(nomeFormatado);
    }

    const novaTransacao: Transacao = {
      id: String(Date.now()),
      descricao: descricao.trim(),
      valor: valorNumerico,
      data,
      tipo,
      categoria: categoriaFinal,
      estabelecimento: estabelecimento.trim() || undefined
    };

    setTransacoes([novaTransacao, ...transacoes]);

    // Reseta os campos do formulário
    setDescricao('');
    setValor('');
    setEstabelecimento('');
    setOutraCategoriaInput('');
    setCategoria('Alimentação');
  };

  const handleDelete = (id: string) => {
    setTransacoes(transacoes.filter(item => item.id !== id));
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
        <h2 className="text-lg font-bold text-slate-800 mb-4">Nova Transação</h2>

        <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Descrição *</label>
            <input
              type="text"
              placeholder="Ex: Ração do cachorro, Almoço"
              value={descricao}
              onChange={e => setDescricao(e.target.value)}
              className="w-full border border-slate-300 rounded-lg p-2.5 text-sm focus:outline-none focus:border-red-600"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Valor (R$) *</label>
            <input
              type="number"
              step="0.01"
              placeholder="0,00"
              value={valor}
              onChange={e => setValor(e.target.value)}
              className="w-full border border-slate-300 rounded-lg p-2.5 text-sm focus:outline-none focus:border-red-600"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Data *</label>
            <input
              type="date"
              value={data}
              onChange={e => setData(e.target.value)}
              className="w-full border border-slate-300 rounded-lg p-2.5 text-sm focus:outline-none focus:border-red-600"
              required
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Tipo *</label>
            <select
              value={tipo}
              onChange={e => setTipo(e.target.value as TipoTransacao)}
              className="w-full border border-slate-300 rounded-lg p-2.5 text-sm bg-white focus:outline-none focus:border-red-600"
            >
              <option value="DESPESA">Despesa</option>
              <option value="RECEITA">Receita</option>
            </select>
          </div>

          {/* Select de Categorias */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Categoria</label>
            <select
              value={categoria}
              onChange={e => setCategoria(e.target.value)}
              className="w-full border border-slate-300 rounded-lg p-2.5 text-sm bg-white focus:outline-none focus:border-red-600"
            >
              {categorias.map(cat => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Estabelecimento</label>
            <input
              type="text"
              placeholder="Ex: Petz, McDonald's"
              value={estabelecimento}
              onChange={e => setEstabelecimento(e.target.value)}
              className="w-full border border-slate-300 rounded-lg p-2.5 text-sm focus:outline-none focus:border-red-600"
            />
          </div>

          {/* Campo extra que aparece quando o usuário escolhe 'Outros' */}
          {categoria === 'Outros' && (
            <div className="md:col-span-3 bg-red-50/50 p-3 rounded-lg border border-red-200">
              <label className="block text-xs font-semibold text-red-700 mb-1">
                Qual o nome da nova categoria? *
              </label>
              <input
                type="text"
                placeholder="Ex: Pets, Skincare, Doações"
                value={outraCategoriaInput}
                onChange={e => setOutraCategoriaInput(e.target.value)}
                className="w-full border border-red-300 rounded-lg p-2 text-sm bg-white focus:outline-none focus:border-red-600"
                required
              />
            </div>
          )}

          <div className="md:col-span-3 flex justify-end">
            <button
              type="submit"
              className="bg-red-600 hover:bg-red-700 text-white font-medium px-5 py-2.5 rounded-lg text-sm transition-colors cursor-pointer"
            >
              Salvar Transação
            </button>
          </div>
        </form>
      </div>

      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="p-4 border-b border-slate-200">
          <h2 className="text-lg font-bold text-slate-800">Histórico de Transações</h2>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 text-slate-600 font-semibold border-b border-slate-200">
              <tr>
                <th className="py-3 px-4">Descrição</th>
                <th className="py-3 px-4">Categoria</th>
                <th className="py-3 px-4">Data</th>
                <th className="py-3 px-4 text-right">Valor</th>
                <th className="py-3 px-4 text-center">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {transacoes.length === 0 ? (
                <tr>
                  <td colSpan={5} className="text-center py-6 text-slate-400">
                    Nenhuma transação cadastrada ainda.
                  </td>
                </tr>
              ) : (
                transacoes.map(item => (
                  <tr key={item.id} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-3 px-4">
                      <p className="font-medium text-slate-800">{item.descricao}</p>
                      {item.estabelecimento && (
                        <p className="text-xs text-slate-400">{item.estabelecimento}</p>
                      )}
                    </td>
                    <td className="py-3 px-4">
                      <span className="inline-block bg-slate-100 text-slate-600 text-xs px-2.5 py-1 rounded-md">
                        {item.categoria}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-500">{item.data}</td>
                    <td className={`py-3 px-4 text-right font-semibold ${
                      item.tipo === 'RECEITA' ? 'text-emerald-600' : 'text-red-600'
                    }`}>
                      {item.tipo === 'RECEITA' ? '+' : '-'} R$ {item.valor.toFixed(2)}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="text-xs font-semibold text-slate-400 hover:text-red-600 transition-colors cursor-pointer"
                      >
                        Excluir
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};