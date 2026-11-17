import { useMemo } from 'react';
import { TrendingUp, TrendingDown, Landmark } from 'lucide-react';
import type { Transacao } from '../types';

interface EvolucaoPatrimonioProps {
  transacoes: Transacao[];
}

interface PontoPatrimonio {
  chaveMes: string;
  labelMes: string;
  receitas: number;
  despesas: number;
  saldoMes: number;
  patrimonioAcumulado: number;
}

const MESES_CURTOS = [
  'Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun',
  'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez',
];

function formatarMoeda(valor: number) {
  return valor.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });
}

export function EvolucaoPatrimonio({ transacoes }: EvolucaoPatrimonioProps) {
  const seriePatrimonio = useMemo<PontoPatrimonio[]>(() => {
    if (!transacoes || transacoes.length === 0) return [];

    const mapaMensal = new Map<string, { receitas: number; despesas: number }>();

    for (const t of transacoes) {
      if (!t.data) continue;
      const chaveMes = t.data.slice(0, 7); // YYYY-MM
      const atual = mapaMensal.get(chaveMes) || { receitas: 0, despesas: 0 };

      if (t.tipo === 'RECEITA') {
        atual.receitas += Number(t.valor) || 0;
      } else {
        atual.despesas += Number(t.valor) || 0;
      }

      mapaMensal.set(chaveMes, atual);
    }

    const chavesOrdenadas = Array.from(mapaMensal.keys()).sort();
    let acumulado = 0;

    return chavesOrdenadas.map((chave) => {
      const { receitas, despesas } = mapaMensal.get(chave)!;
      const saldoMes = receitas - despesas;
      acumulado += saldoMes;

      const [ano, mesStr] = chave.split('-');
      const indiceMes = Math.max(0, Math.min(11, Number(mesStr) - 1));
      const labelMes = `${MESES_CURTOS[indiceMes]}/${ano.slice(2)}`;

      return {
        chaveMes: chave,
        labelMes,
        receitas,
        despesas,
        saldoMes,
        patrimonioAcumulado: acumulado,
      };
    });
  }, [transacoes]);

  const patrimonioAtual =
    seriePatrimonio.length > 0
      ? seriePatrimonio[seriePatrimonio.length - 1].patrimonioAcumulado
      : 0;

  const patrimonioAnterior =
    seriePatrimonio.length > 1
      ? seriePatrimonio[seriePatrimonio.length - 2].patrimonioAcumulado
      : 0;

  const variacaoValor = patrimonioAtual - patrimonioAnterior;
  const variacaoPercentual =
    seriePatrimonio.length > 1 && patrimonioAnterior !== 0
      ? ((patrimonioAtual - patrimonioAnterior) / Math.abs(patrimonioAnterior)) * 100
      : null;

  // Coordenadas para gráfico SVG responsivo
  const larguraSvg = 640;
  const alturaSvg = 180;
  const paddingX = 36;
  const paddingY = 24;

  const valores = seriePatrimonio.map((p) => p.patrimonioAcumulado);
  const minValor = valores.length > 0 ? Math.min(0, ...valores) : 0;
  const maxValor = valores.length > 0 ? Math.max(1, ...valores) : 1;
  const amplitude = maxValor - minValor || 1;

  const pontos = seriePatrimonio.map((item, idx) => {
    const x =
      seriePatrimonio.length === 1
        ? larguraSvg / 2
        : paddingX +
          (idx / (seriePatrimonio.length - 1)) * (larguraSvg - paddingX * 2);
    const y =
      alturaSvg -
      paddingY -
      ((item.patrimonioAcumulado - minValor) / amplitude) *
        (alturaSvg - paddingY * 2);

    return { ...item, x, y };
  });

  const linhaPath = pontos
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x.toFixed(1)} ${p.y.toFixed(1)}`)
    .join(' ');

  const areaPath =
    pontos.length > 1
      ? `${linhaPath} L ${pontos[pontos.length - 1].x.toFixed(1)} ${
          alturaSvg - paddingY
        } L ${pontos[0].x.toFixed(1)} ${alturaSvg - paddingY} Z`
      : '';

  return (
    <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm space-y-5">
      {/* Cabeçalho */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-emerald-100 text-emerald-700">
            <Landmark className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-lg font-bold text-slate-800">
              Evolução do Patrimônio
            </h2>
            <p className="text-xs text-slate-500">
              Acumulado líquido (receitas menos despesas) ao longo dos meses
            </p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <div className="text-left sm:text-right">
            <span className="text-xs font-medium text-slate-500 block">
              Patrimônio Acumulado
            </span>
            <span
              className={`text-xl font-bold ${
                patrimonioAtual >= 0 ? 'text-slate-900' : 'text-rose-600'
              }`}
            >
              {formatarMoeda(patrimonioAtual)}
            </span>
          </div>

          {seriePatrimonio.length > 1 && (
            <div
              className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${
                variacaoValor >= 0
                  ? 'bg-emerald-50 text-emerald-700'
                  : 'bg-rose-50 text-rose-700'
              }`}
            >
              {variacaoValor >= 0 ? (
                <TrendingUp className="w-3.5 h-3.5" />
              ) : (
                <TrendingDown className="w-3.5 h-3.5" />
              )}
              <span>
                {variacaoPercentual !== null
                  ? `${variacaoPercentual >= 0 ? '+' : ''}${variacaoPercentual.toFixed(1)}%`
                  : formatarMoeda(variacaoValor)}
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Gráfico */}
      {seriePatrimonio.length === 0 ? (
        <div className="text-center py-8 text-sm text-slate-500 bg-slate-50 rounded-xl border border-dashed border-slate-200">
          Cadastre transações para visualizar a evolução histórica do seu patrimônio.
        </div>
      ) : (
        <>
          <div className="w-full overflow-x-auto">
            <svg
              viewBox={`0 0 ${larguraSvg} ${alturaSvg}`}
              className="w-full h-48 overflow-visible"
            >
              <defs>
                <linearGradient id="gradPatrimonio" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#10b981" stopOpacity="0.25" />
                  <stop offset="100%" stopColor="#10b981" stopOpacity="0.0" />
                </linearGradient>
              </defs>

              {/* Linha base */}
              <line
                x1={paddingX}
                y1={alturaSvg - paddingY}
                x2={larguraSvg - paddingX}
                y2={alturaSvg - paddingY}
                stroke="#e2e8f0"
                strokeWidth="1"
              />

              {/* Área preenchida */}
              {areaPath && <path d={areaPath} fill="url(#gradPatrimonio)" />}

              {/* Linha de evolução */}
              {pontos.length > 1 && (
                <path
                  d={linhaPath}
                  fill="none"
                  stroke="#10b981"
                  strokeWidth="3"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              )}

              {/* Pontos e rótulos */}
              {pontos.map((p) => (
                <g key={p.chaveMes}>
                  <circle
                    cx={p.x}
                    cy={p.y}
                    r="5"
                    fill="#ffffff"
                    stroke="#059669"
                    strokeWidth="2.5"
                  />
                  <text
                    x={p.x}
                    y={p.y - 11}
                    textAnchor="middle"
                    className="fill-slate-700 text-[10px] font-semibold"
                  >
                    {formatarMoeda(p.patrimonioAcumulado)}
                  </text>
                  <text
                    x={p.x}
                    y={alturaSvg - 6}
                    textAnchor="middle"
                    className="fill-slate-500 text-[11px] font-medium"
                  >
                    {p.labelMes}
                  </text>
                </g>
              ))}
            </svg>
          </div>

          {/* Resumo tabular compacto por mês */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2 border-t border-slate-100">
            {seriePatrimonio.slice(-4).map((item) => (
              <div
                key={item.chaveMes}
                className="rounded-xl bg-slate-50 p-3 border border-slate-100"
              >
                <span className="text-xs font-semibold text-slate-500 block">
                  {item.labelMes}
                </span>
                <span className="text-sm font-bold text-slate-800 block mt-0.5">
                  {formatarMoeda(item.patrimonioAcumulado)}
                </span>
                <span
                  className={`text-[11px] font-medium block mt-0.5 ${
                    item.saldoMes >= 0 ? 'text-emerald-600' : 'text-rose-600'
                  }`}
                >
                  Saldo mês: {item.saldoMes >= 0 ? '+' : ''}
                  {formatarMoeda(item.saldoMes)}
                </span>
              </div>
            ))}
          </div>
        </>
      )}
    </section>
  );
}