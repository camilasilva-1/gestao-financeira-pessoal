import React, { useState, useEffect } from 'react';
import type { Transacao } from '../types';
import { ModalEditarTransacao } from './ModalEditarTransacao';
import { cadastrarTransacao, listarTransacoes } from '../Services/api';

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
  const [data, setData] = useState('');
  const [tipo, setTipo] = useState<'RECEITA' | 'DESPESA'>('RECEITA');
  const [categoria, setCategoria] = useState<string>(
    categorias && categorias.length > 0 ? categorias[0] : 'Alimentação'
  );
  const [establishment, setEstablishment] = useState('');
  const [carregando, setCarregando] = useState(false);

  // Filtros
  const [busca, setBusca] = useState('');
  const [filtroTipo, setFiltroTipo] = useState<'TODOS' | 'RECEITA' | 'DESPESA'>('TODOS');
  const [filtroCategoria, setFiltroCategoria] = useState('TODAS');
  const [filtroPeriodo, setFiltroPeriodo] = useState<'TODOS' | 'ESTE_MES' | 'MES_PASSADO' | 'ESTE_ANO'>('TODOS');

  const [dataInicial, setDataInicial] = useState('');
  const [dataFinal, setDataFinal] = useState('');
  const [filtroEstabelecimento, setFiltroEstabelecimento] = useState('');
  const [filtroRecorrencia, setFiltroRecorrencia] = useState<'TODOS' | 'SIM' | 'NAO'>('TODOS');

  // Modal de Edição
  const [transacaoParaEditar, setTransacaoParaEditar] = useState<Transacao | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // 1. Busca as transações reais cadastradas no MySQL via Spring Boot
  useEffect(() => {
    const usuarioIdSalvo = localStorage.getItem('usuarioId');
    if (usuarioIdSalvo) {
      const idNum = parseInt(usuarioIdSalvo, 10);
      listarTransacoes(idNum)
        .then((dados) => {
          if (Array.isArray(dados)) {
            // Mapeia o retorno da API para a estrutura do Front-end
            const formatadas: Transacao[] = dados.map((t: any) => ({
              id: String(t.idTransacao || t.id),
              descricao: t.descricao,
              valor: typeof t.valor === 'number' ? t.valor : parseFloat(t.valor),
              data: t.dataTransacao || t.data,
              tipo: t.tipo,
              categoria: typeof t.categoria === 'object' ? t.categoria.nome : t.categoria,
              establishment: t.estabelecimento,
              recorrente: Boolean(t.recorrencia)
            }));
            setTransacoes(formatadas);
          }
        })
        .catch((err) => console.error('Erro ao carregar transações:', err));
    }
  }, [setTransacoes]);

  useEffect(() => {
    if (categorias && categorias.length > 0 && !categorias.includes(categoria)) {
      setCategoria(categorias[0]);
    }
  }, [categorias, categoria]);

  // 2. Salva a nova transação chamando o Spring Boot (POST)
  const handleNovaTransacao = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!descricao || !valor || !data) return;

    const usuarioIdSalvo = localStorage.getItem('usuarioId');
    const usuarioIdNum = usuarioIdSalvo ? parseInt(usuarioIdSalvo, 10) : 1;

    setCarregando(true);

    try {
      const payload = {
        descricao,
        valor: parseFloat(valor),
        tipo,
        categoriaId: 1, // ID da categoria padrão no banco
        usuarioId: usuarioIdNum,
        dataTransacao: data,
        estabelecimento: establishment.trim() ? establishment.trim() : undefined
      };

      const res = await cadastrarTransacao(payload);

      // Adiciona na lista do estado
      const nova: Transacao = {
        id: String(res.idTransacao || Date.now()),
        descricao,
        valor: parseFloat(valor),
        data,
        tipo,
        categoria,
        establishment: establishment.trim() ? establishment.trim() : undefined
      };

      setTransacoes([nova, ...transacoes]);

      // Limpa os campos do formulário
      setDescricao('');
      setValor('');
      setEstablishment('');
      setData('');
    } catch (err) {
      console.error('Erro ao cadastrar transação:', err);
      alert('Não foi possível salvar a transação. Verifique se o servidor Back-end está rodando.');
    } finally {
      setCarregando(false);
    }
  };

  // 3. Exclui transação
  const handleExcluir = async (id: string) => {
    if (confirm('Deseja realmente excluir esta transação?')) {
      try {
        await fetch(`http://localhost:8080/api/transacoes/${id}`, { method: 'DELETE' });
        setTransacoes(transacoes.filter(t => t.id !== id));
      } catch (err) {
        console.error('Erro ao excluir transação:', err);
      }
    }
  };

  const handleAbrirEditar = (t: Transacao) => {
    setTransacaoParaEditar(t);
    setIsModalOpen(true);
  };

  const handleSalvarEdicao = (transacaoAtualizada: Transacao) => {
    setTransacoes(transacoes.map(t => t.id === transacaoAtualizada.id ? transacaoAtualizada : t));
  };

  const ehNoPeriodo = (dataString: string) => {
    if (filtroPeriodo === 'TODOS') return true;

    const dataTransacao = new Date(dataString + 'T00:00:00');
    const hoje = new Date();
    const anoAtual = hoje.getFullYear();
    const mesAtual = hoje.getMonth();

    if (filtroPeriodo === 'ESTE_MES') {
      return dataTransacao.getFullYear() === anoAtual && dataTransacao.getMonth() === mesAtual;
    }

    if (filtroPeriodo === 'MES_PASSADO') {
      const mesAnterior = mesAtual === 0 ? 11 : mesAtual - 1;
      const anoDoMesAnterior = mesAtual === 0 ? anoAtual - 1 : anoAtual;
      return dataTransacao.getFullYear() === anoDoMesAnterior && dataTransacao.getMonth() === mesAnterior;
    }

    if (filtroPeriodo === 'ESTE_ANO') {
      return dataTransacao.getFullYear() === anoAtual;
    }

    return true;
  };

  const transacoesFiltradas = transacoes.filter(t => {
    const bateTexto = t.descricao.toLowerCase().includes(busca.toLowerCase()) ||
      (t.establishment && t.establishment.toLowerCase().includes(busca.toLowerCase()));
    const bateTipo = filtroTipo === 'TODOS' || t.tipo === filtroTipo;
    const bateCat = filtroCategoria === 'TODAS' || (filtroCategoria === 'SEM_CATEGORIA' ? !t.categoria : t.categoria === filtroCategoria);
    const batePeriodo = ehNoPeriodo(t.data);
    const bateDataInicial = !dataInicial || t.data >= dataInicial;
    const bateDataFinal = !dataFinal || t.data <= dataFinal;
    const bateEstabelecimento = !filtroEstabelecimento || (t.establishment || '').toLowerCase().includes(filtroEstabelecimento.toLowerCase());
    const bateRecorrencia = filtroRecorrencia === 'TODOS' || (filtroRecorrencia === 'SIM' ? Boolean(t.recorrente) : !t.recorrente);

    return bateTexto && bateTipo && bateCat && batePeriodo && bateDataInicial && bateDataFinal && bateEstabelecimento && bateRecorrencia;
  });

  return (
    <div className="space-y-6">
      {/* Formulário de Nova Transação */}
      <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <h2 className="text-lg font-bold text-slate-800">Nova Transação</h2>
        <form onSubmit={handleNovaTransacao} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Descrição *</label>
              <input
                type="text"
                placeholder="Ex: Ração do cachorro, Almoço"
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
                className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
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
                onChange={(e) => setValor(e.target.value)}
                className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
                required
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Data *</label>
              <input
                type="date"
                value={data}
                onChange={(e) => setData(e.target.value)}
                className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Tipo *</label>
              <select
                value={tipo}
                onChange={(e) => setTipo(e.target.value as 'RECEITA' | 'DESPESA')}
                className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
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
                className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
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
                value={establishment}
                onChange={(e) => setEstablishment(e.target.value)}
                className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500"
              />
            </div>
          </div>

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={carregando}
              className="w-full bg-gradient-to-r from-[#c4003d] to-[#e60039] text-white font-bold py-3 rounded-xl shadow-md hover:opacity-95 transition-all duration-200 cursor-pointer text-sm disabled:opacity-50"
            >
              {carregando ? 'Salvando...' : 'Salvar Transação'}
            </button>
          </div>
        </form>
      </div>

      {/* Histórico com Barra de Filtros */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-2">
        <div className="grid grid-cols-1 lg:grid-cols-[220px_minmax(0,1fr)] gap-3 border-b pb-3">
          <h2 className="text-lg font-bold text-slate-800 lg:pt-1 lg:whitespace-nowrap">
            Histórico de Transações
          </h2>

          <div className="min-w-0 space-y-2">
            {/* Linha 1: filtros principais alinhados */}
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-2">
              <input type="text" placeholder="Buscar por descrição..." value={busca}
                onChange={e => setBusca(e.target.value)}
                aria-label="Buscar por descrição"
                className="w-full min-w-0 h-9 border border-slate-300 rounded-lg px-3 text-xs" />
              <select value={filtroPeriodo}
                onChange={e => setFiltroPeriodo(e.target.value as 'TODOS' | 'ESTE_MES' | 'MES_PASSADO' | 'ESTE_ANO')}
                aria-label="Período"
                className="w-full min-w-0 h-9 border border-slate-300 rounded-lg px-2 text-xs">
                <option value="TODOS">Todos os Períodos</option>
                <option value="ESTE_MES">Este Mês</option>
                <option value="MES_PASSADO">Mês Passado</option>
                <option value="ESTE_ANO">Este Ano</option>
              </select>
              <select value={filtroTipo}
                onChange={e => setFiltroTipo(e.target.value as 'TODOS' | 'RECEITA' | 'DESPESA')}
                aria-label="Tipo de transação"
                className="w-full min-w-0 h-9 border border-slate-300 rounded-lg px-2 text-xs">
                <option value="TODOS">Todos os Tipos</option>
                <option value="RECEITA">Receitas</option>
                <option value="DESPESA">Despesas</option>
              </select>
              <select value={filtroCategoria} onChange={e => setFiltroCategoria(e.target.value)}
                aria-label="Categoria"
                className="w-full min-w-0 h-9 border border-slate-300 rounded-lg px-2 text-xs">
                <option value="TODAS">Todas as Categorias</option>
                <option value="SEM_CATEGORIA">Sem categoria</option>
                {categorias.map(cat => <option key={cat} value={cat}>{cat}</option>)}
              </select>
            </div>

            {/* Linha 2: filtros adicionais com campos do mesmo tamanho */}
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-2">
              <label className="block min-w-0 text-xs text-slate-600">
                De
                <input type="date" value={dataInicial} onChange={e => setDataInicial(e.target.value)}
                  className="mt-0.5 block w-full min-w-0 h-9 border border-slate-300 rounded-lg px-2 text-xs" />
              </label>
              <label className="block min-w-0 text-xs text-slate-600">
                Até
                <input type="date" min={dataInicial || undefined} value={dataFinal}
                  onChange={e => setDataFinal(e.target.value)}
                  className="mt-0.5 block w-full min-w-0 h-9 border border-slate-300 rounded-lg px-2 text-xs" />
              </label>
              <label className="block min-w-0 text-xs text-slate-600">
                Estabelecimento
                <input type="text" placeholder="Nome do estabelecimento" value={filtroEstabelecimento}
                  onChange={e => setFiltroEstabelecimento(e.target.value)}
                  className="mt-0.5 block w-full min-w-0 h-9 border border-slate-300 rounded-lg px-2 text-xs" />
              </label>
              <div className="block min-w-0">
                <div className="flex items-center justify-between gap-2 text-xs text-slate-600">
                  <label htmlFor="filtro-recorrencia">Recorrência</label>
                  <button type="button" onClick={() => {
                    setBusca(''); setFiltroTipo('TODOS'); setFiltroCategoria('TODAS'); setFiltroPeriodo('TODOS');
                    setDataInicial(''); setDataFinal(''); setFiltroEstabelecimento(''); setFiltroRecorrencia('TODOS');
                  }} className="text-blue-600 hover:underline whitespace-nowrap">Limpar filtros</button>
                </div>
                <select id="filtro-recorrencia" value={filtroRecorrencia}
                  onChange={e => setFiltroRecorrencia(e.target.value as 'TODOS' | 'SIM' | 'NAO')}
                  className="mt-0.5 block w-full min-w-0 h-9 border border-slate-300 rounded-lg px-2 text-xs">
                  <option value="TODOS">Todas as recorrências</option>
                  <option value="SIM">Recorrentes</option>
                  <option value="NAO">Não recorrentes</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        {/* Tabela de Resultados */}
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="text-slate-500 border-b text-xs uppercase tracking-wider">
                <th className="pb-3 font-semibold">Descrição</th>
                <th className="pb-3 font-semibold">Categoria</th>
                <th className="pb-3 font-semibold">Data</th>
                <th className="pb-3 font-semibold text-right">Valor</th>
                <th className="pb-3 font-semibold text-center">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {transacoesFiltradas.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-6 text-center text-slate-400 text-xs">
                    Nenhuma transação encontrada para os filtros selecionados.
                  </td>
                </tr>
              ) : (
                transacoesFiltradas.map((t) => (
                  <tr key={t.id} className="hover:bg-slate-50/80 transition">
                    <td className="py-3 font-medium text-slate-800">
                      {t.descricao}
                      {t.establishment && (
                        <span className="block text-xs text-slate-400 font-normal">{t.establishment}</span>
                      )}
                    </td>
                    <td className="py-3">
                      <span className="px-2.5 py-1 text-xs rounded-full bg-slate-100 text-slate-600">
                        {t.categoria}
                      </span>
                    </td>
                    <td className="py-3 text-slate-500 text-xs">{t.data}</td>
                    <td className={`py-3 text-right font-bold ${t.tipo === 'RECEITA' ? 'text-emerald-600' : 'text-red-600'}`}>
                      {t.tipo === 'RECEITA' ? '+' : '-'} R$ {t.valor.toFixed(2)}
                    </td>
                    <td className="py-3 text-center space-x-2">
                      <button
                        onClick={() => handleAbrirEditar(t)}
                        className="text-xs text-blue-600 hover:text-blue-800 font-medium"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => handleExcluir(t.id)}
                        className="text-xs text-slate-400 hover:text-red-600 font-medium"
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

      <ModalEditarTransacao
        transacao={transacaoParaEditar}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSave={handleSalvarEdicao}
        categorias={categorias}
      />
    </div>
  );
};