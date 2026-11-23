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

interface RelatorioConsultado {
  receitaTotal: number;
  despesaTotal: number;
  saldoFinal: number;
  distribuicao: Array<{ categoria: string; valor: number; percentual: number }>;
}

interface RelatoriosProps {
  transacoes: Transacao[];
}

// Sempre seleciona o mês completo, independentemente do dia.
const obterDatasDoMes = (periodo: string) => {
  const partes = /^(\d{4})-(\d{2})$/.exec(periodo);
  if (!partes) throw new Error('Selecione um mês e ano válidos.');
  const ano = Number(partes[1]);
  const mes = Number(partes[2]);
  if (ano < 1 || mes < 1 || mes > 12) {
    throw new Error('Selecione um mês e ano válidos.');
  }
  const ultimoDia = new Date(ano, mes, 0).getDate();
  return {
    inicio: periodo + '-01',
    fim: periodo + '-' + String(ultimoDia).padStart(2, '0')
  };
};

export const Relatorios: React.FC<RelatoriosProps> = ({ transacoes }) => {
  const [regra, setRegra] = useState<Regra503020 | null>(null);
  const [erroRegra, setErroRegra] = useState(false);
  const [historicoRelatorios, setHistoricoRelatorios] = useState<RelatorioSalvo[]>([]);
  const [salvandoRelatorio, setSalvandoRelatorio] = useState(false);
  const [excluindoRelatorioId, setExcluindoRelatorioId] = useState<number | null>(null);
  const [mensagemRelatorio, setMensagemRelatorio] = useState('');
  const [periodoSelecionado, setPeriodoSelecionado] = useState(() => {
    const data = new Date();
    return String(data.getFullYear()) + '-' + String(data.getMonth() + 1).padStart(2, '0');
  });
  const [consultandoRelatorio, setConsultandoRelatorio] = useState(false);
  const [relatorioConsultado, setRelatorioConsultado] = useState<RelatorioConsultado | null>(null);

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

  // Consultar não grava nada: somente exibe o relatório do mês escolhido.
  const consultarRelatorio = async () => {
    const id = localStorage.getItem('usuarioId');
    if (!id || consultandoRelatorio) return;
    setConsultandoRelatorio(true);
    setMensagemRelatorio('');
    setRelatorioConsultado(null);
    try {
      const { inicio, fim } = obterDatasDoMes(periodoSelecionado);
      const params = new URLSearchParams({ usuarioId: id, inicio, fim });
      const resposta = await fetch('http://localhost:8080/api/relatorios/mensal?' + params, {
        credentials: 'include'
      });
      if (!resposta.ok) throw new Error('HTTP ' + resposta.status);
      setRelatorioConsultado((await resposta.json()) as RelatorioConsultado);
    } catch (erro) {
      console.error('Falha ao consultar relatório:', erro);
      setMensagemRelatorio('Não foi possível consultar o relatório do mês selecionado.');
    } finally {
      setConsultandoRelatorio(false);
    }
  };

  // Salvar é uma ação separada: mantém o histórico RN009.
  const salvarRelatorioDoMes = async () => {
    const id = localStorage.getItem('usuarioId');
    if (!id || salvandoRelatorio) return;
    setSalvandoRelatorio(true);
    setMensagemRelatorio('');
    try {
      const { inicio, fim } = obterDatasDoMes(periodoSelecionado);
      const params = new URLSearchParams({ usuarioId: id, inicio, fim });
      const resposta = await fetch('http://localhost:8080/api/relatorios/mensal/arquivar?' + params, {
        method: 'POST',
        credentials: 'include',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
      });
      if (!resposta.ok) throw new Error('HTTP ' + resposta.status);
      await buscarHistorico();
      setMensagemRelatorio('Relatório do mês selecionado salvo no histórico.');
    } catch (erro) {
      console.error('Falha ao salvar relatório mensal:', erro);
      setMensagemRelatorio('Não foi possível salvar o relatório do período escolhido.');
    } finally {
      setSalvandoRelatorio(false);
    }
  };

  // Abre um documento pronto para imprimir ou salvar como PDF no navegador.
  const exportarPdf = (relatorio: RelatorioSalvo) => {

    const escapar = (valor: string) => valor.replace(/[&<>"']/g, caractere => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[caractere] || caractere);
    const moedaPdf = (valor: number) => Number(valor).toLocaleString('pt-BR', {
      style: 'currency', currency: 'BRL'
    });
    const transacoesDoMes = transacoes
      .filter(t => t.data.slice(0, 7) === relatorio.inicio.slice(0, 7))
      .sort((a, b) => a.data.localeCompare(b.data));
    const linhas = transacoesDoMes.map(t => `
      <tr>
        <td>${escapar(t.data.split('-').reverse().join('/'))}</td>
        <td>${escapar(t.descricao)}</td>
        <td>${escapar(t.categoria || 'Sem categoria')}</td>
        <td>${t.tipo === 'RECEITA' ? 'Receita' : 'Despesa'}</td>
        <td>${moedaPdf(Number(t.valor))}</td>
      </tr>`).join('');

    const janela = window.open('', '_blank');
    if (!janela) {
      setMensagemRelatorio('Permita a abertura de pop-ups para exportar o PDF.');
      return;
    }
    const dataEmissao = new Date().toLocaleString('pt-BR');
    const mes = relatorio.inicio.slice(5, 7) + '/' + relatorio.inicio.slice(0, 4);
    const dataSalvamento = new Date(relatorio.geradoEm).toLocaleString('pt-BR');
    janela.document.write(`<!doctype html><html lang="pt-BR"><head>
      <meta charset="UTF-8"><title>ByteBank Finance - ${mes}</title>
      <style>
        body { font-family: Arial, sans-serif; color: #222; margin: 32px; }
        h1 { font-size: 22px; margin-bottom: 4px; }
        h2 { font-size: 16px; margin-top: 24px; }
        p { font-size: 13px; }
        table { width: 100%; border-collapse: collapse; font-size: 12px; }
        th, td { padding: 8px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background: #f1f5f9; }
        footer { margin-top: 30px; font-size: 11px; color: #666; }
        @media print { body { margin: 12mm; } thead { display: table-header-group; } }
      </style></head><body>
      <h1>ByteBank Finance</h1>
      <p>Relatório financeiro mensal — ${mes}<br>Salvo em: ${dataSalvamento}<br>Emitido em: ${dataEmissao}</p>
      <h2>Resumo financeiro</h2>
      <p>Receitas: ${moedaPdf(Number(relatorio.receitaTotal))}<br>
      Despesas: ${moedaPdf(Number(relatorio.despesaTotal))}<br>
      Saldo do mês: ${moedaPdf(Number(relatorio.saldoFinal))}</p>
      <h2>Transações do mês</h2>
      <p>Transações conforme cadastro atual. O resumo acima preserva os valores do relatório salvo.</p>
      <table><thead><tr><th>Data</th><th>Descrição</th><th>Categoria</th><th>Tipo</th><th>Valor</th></tr></thead>
      <tbody>${linhas || '<tr><td colspan="5">Nenhuma transação neste mês.</td></tr>'}</tbody></table>
      <footer>ByteBank Finance — Relatório gerado automaticamente.</footer>
      </body></html>`);
    janela.document.close();
    janela.focus();
    janela.print(); // Na janela de impressão, escolha "Salvar como PDF".
  };

  const excluirRelatorio = async (item: RelatorioSalvo) => {
    const usuarioId = localStorage.getItem('usuarioId');
    if (!usuarioId || excluindoRelatorioId !== null) return;
    const mes = item.inicio.slice(5, 7) + '/' + item.inicio.slice(0, 4);
    if (!window.confirm('Excluir o relatório salvo de ' + mes + '? Esta ação não exclui transações, orçamentos nem metas.')) return;

    setExcluindoRelatorioId(item.idHistorico);
    setMensagemRelatorio('');
    try {
      const resposta = await fetch(
        'http://localhost:8080/api/relatorios/historico/' + item.idHistorico +
        '?usuarioId=' + encodeURIComponent(usuarioId),
        {
          method: 'DELETE',
          credentials: 'include',
          headers: { 'X-Requested-With': 'XMLHttpRequest' }
        }
      );
      if (!resposta.ok) throw new Error('HTTP ' + resposta.status);
      setHistoricoRelatorios(anteriores =>
        anteriores.filter(relatorio => relatorio.idHistorico !== item.idHistorico)
      );
      setMensagemRelatorio('Relatório excluído do histórico.');
    } catch (erro) {
      console.error('Falha ao excluir relatório:', erro);
      setMensagemRelatorio('Não foi possível excluir o relatório. Tente novamente.');
    } finally {
      setExcluindoRelatorioId(null);
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
        <h2 id="relatorios" className="scroll-mt-6 text-lg font-bold text-slate-800 flex items-center gap-2">
          <span>📊</span> Evolução Mensal (Mês Atual vs Anterior)
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Card Despesas Comparativo */}
          <div className="bg-slate-50 p-4 rounded-xl border border-slate-200">
            <span className="text-xs font-semibold uppercase text-slate-500">Gastos do Mês Atual</span>
            <div className="flex items-baseline justify-between mt-1">
              <span className="text-2xl font-bold text-slate-800">R$ {despesasMesAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
              {despesasMesAnterior > 0 && (
                <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                  diferencaDespesas <= 0 ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
                }`}>
                  {diferencaDespesas > 0 ? `+${diferencaDespesas}%` : `${diferencaDespesas}%`} vs mês anterior
                </span>
              )}
            </div>
            <p className="text-xs text-slate-400 mt-2">
              Mês anterior: R$ {despesasMesAnterior.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
          </div>

          {/* Card Receitas Comparativo */}
          <div className="bg-slate-50 p-4 rounded-xl border border-slate-200">
            <span className="text-xs font-semibold uppercase text-slate-500">Entradas do Mês Atual</span>
            <div className="flex items-baseline justify-between mt-1">
              <span className="text-2xl font-bold text-emerald-600">R$ {receitasMesAtual.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
              <span className="text-xs text-slate-500 font-medium">Balanço do Período</span>
            </div>
            <p className="text-xs text-slate-400 mt-2">
              Saldo líquido do mês: R$ {(receitasMesAtual - despesasMesAtual).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </p>
          </div>
        </div>
      </div>

      {/* Regra 50/30/20 — seção 3.7 do Takeoff, dados da API */}
      <section className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3" aria-label="Planejamento pela regra 50/30/20">
        <h2 id="planejamento" className="scroll-mt-6 text-lg font-bold text-slate-800">Planejamento 50/30/20</h2>
        {regra ? (
          <>
            <p className="text-sm text-slate-600">
              Base: receitas registradas no mês (ou salário líquido, quando não há receitas lançadas) — <strong>{moeda(regra.rendaTotal)}</strong>.
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

      {/* RN009: consulta por mês/ano e histórico salvo somente após escolha do usuário. */}
      <section className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-3" aria-label="Histórico de relatórios mensais">
        <h2 id="relatorios-salvos" className="scroll-mt-6 text-lg font-bold text-slate-800">Histórico de Relatórios Mensais</h2>
        <div className="flex flex-col sm:flex-row sm:items-end gap-3">
          <label className="flex flex-col gap-1 text-sm text-slate-700">
            Selecione o mês e o ano
            <input
              type="month"
              aria-label="Mês e ano do relatório"
              value={periodoSelecionado}
              onChange={e => {
                setPeriodoSelecionado(e.target.value);
                setRelatorioConsultado(null);
                setMensagemRelatorio('');
              }}
              className="border border-slate-300 rounded-xl px-3 py-2 text-sm"
            />
          </label>
          <button type="button" disabled={consultandoRelatorio || salvandoRelatorio}
            onClick={consultarRelatorio}
            className="bg-slate-200 text-slate-900 text-xs font-bold rounded-xl px-4 py-2 disabled:opacity-50">
            {consultandoRelatorio ? 'Consultando...' : 'Consultar mês'}
          </button>
          <button type="button" disabled={salvandoRelatorio || consultandoRelatorio}
            onClick={salvarRelatorioDoMes}
            className="bg-slate-900 text-white text-xs font-bold rounded-xl px-4 py-2 disabled:opacity-50">
            {salvandoRelatorio ? 'Salvando...' : 'Salvar relatório selecionado'}
          </button>
        </div>
        <p className="text-xs text-slate-500">Escolha qualquer mês e ano para consultar. O histórico só muda quando você seleciona Salvar.</p>
        {mensagemRelatorio && <p className="text-sm text-slate-600" role="status">{mensagemRelatorio}</p>}
        {relatorioConsultado && (
          <div className="rounded-xl border border-slate-200 bg-slate-50 p-3 space-y-1" aria-label="Resultado da consulta mensal">
            <strong className="text-sm text-slate-800">
              Relatório consultado: {periodoSelecionado.slice(5)}/{periodoSelecionado.slice(0, 4)}
            </strong>
            <p className="text-sm text-slate-600">
              Receitas: {moeda(Number(relatorioConsultado.receitaTotal))} ·
              Despesas: {moeda(Number(relatorioConsultado.despesaTotal))}
            </p>
            <p className="text-sm text-slate-700">
              Saldo: {moeda(Number(relatorioConsultado.saldoFinal))}
            </p>
            {relatorioConsultado.distribuicao?.length > 0 && (
              <div className="border-t border-slate-200 pt-2 mt-2">
                <p className="font-semibold text-xs text-slate-700 mb-1">Gastos por categoria no mês escolhido</p>
                {[...relatorioConsultado.distribuicao]
                  .sort((a, b) => Number(b.valor) - Number(a.valor))
                  .map(item => (
                    <p key={item.categoria} className="text-xs text-slate-600">
                      {item.categoria}: {moeda(Number(item.valor))} ({item.percentual}%)
                    </p>
                  ))}
              </div>
            )}
          </div>
        )}
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
                <div className="mt-2 flex flex-wrap gap-2">
                  <button type="button" onClick={() => exportarPdf(item)}
                    aria-label={'Exportar PDF do relatório salvo de ' + item.inicio.slice(5, 7) + '/' + item.inicio.slice(0, 4)}
                    className="rounded-lg bg-[#c4003d] px-3 py-1.5 text-xs font-semibold text-white hover:bg-[#a50033] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#c4003d]">
                    Exportar PDF
                  </button>
                  <button
                    type="button"
                    onClick={() => excluirRelatorio(item)}
                    disabled={excluindoRelatorioId !== null}
                    aria-label={'Excluir relatório salvo de ' + item.inicio.slice(5, 7) + '/' + item.inicio.slice(0, 4)}
                    className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-slate-600 disabled:opacity-50"
                  >
                    {excluindoRelatorioId === item.idHistorico ? 'Excluindo...' : 'Excluir relatório'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* #14 & #15 - Relatório Mensal e Ranking de Categorias */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm space-y-4">
        <h2 id="ranking" className="scroll-mt-6 text-lg font-bold text-slate-800 flex items-center gap-2">
          <span>🏆</span> Ranking de Gastos por Categoria (mês atual)
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
                    R$ {item.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ({item.porcentagem}%)
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