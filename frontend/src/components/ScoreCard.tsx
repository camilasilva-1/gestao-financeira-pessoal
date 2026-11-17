import { useEffect, useState } from 'react';

interface ScoreFinanceiro {
  score: number;
  nivel: string;
  descricao: string;
}

interface ScoreCardProps {
  atualizarGatilho?: unknown; // Opcional: recarrega o score se houver nova transação
}

export const ScoreCard = ({ atualizarGatilho }: ScoreCardProps) => {
  const [dadosScore, setDadosScore] = useState<ScoreFinanceiro | null>(null);
  const [carregando, setCarregando] = useState(true);

 useEffect(() => {
    const usuarioSalvo = localStorage.getItem('usuario');
    const usuarioId =
      localStorage.getItem('usuarioId') ||
      (usuarioSalvo ? JSON.parse(usuarioSalvo).idUsuario : null);

    if (!usuarioId) {
      setCarregando(false);
      return;
    }

    fetch(`http://localhost:8080/insights/${usuarioId}/score`, {
      credentials: 'include',
    })
      .then((res) => {
        if (!res.ok) throw new Error('Erro ao buscar score');
        return res.json();
      })
      .then((data: ScoreFinanceiro) => {
        setDadosScore(data);
      })
      .catch((err) => {
        console.error('Erro ao carregar score financeiro:', err);
      })
      .finally(() => {
        setCarregando(false);
      });
  }, [atualizarGatilho]);

  // Alinhado às faixas do InsightService.java (0 a 100 pontos)
  const obterEstiloFaixa = (nivel: string, score: number) => {
    const nivelNormalizado = nivel?.toLowerCase() || '';

    if (nivelNormalizado.includes('excelente') || score >= 80) {
      return {
        badge: 'bg-emerald-100 text-emerald-800 border-emerald-200',
        barra: 'bg-emerald-500',
        texto: 'text-emerald-600',
      };
    }
    if (nivelNormalizado.includes('bom') || score >= 60) {
      return {
        badge: 'bg-blue-100 text-blue-800 border-blue-200',
        barra: 'bg-blue-500',
        texto: 'text-blue-600',
      };
    }
    if (
      nivelNormalizado.includes('atencao') ||
      nivelNormalizado.includes('atenção') ||
      score >= 40
    ) {
      return {
        badge: 'bg-amber-100 text-amber-800 border-amber-200',
        barra: 'bg-amber-500',
        texto: 'text-amber-600',
      };
    }
    return {
      badge: 'bg-rose-100 text-rose-800 border-rose-200',
      barra: 'bg-rose-500',
      texto: 'text-rose-600',
    };
  };

  if (carregando) {
    return (
      <div className="bg-white rounded-2xl shadow-sm border border-slate-100 p-6 animate-pulse">
        <div className="h-4 bg-slate-200 rounded w-1/3 mb-4"></div>
        <div className="h-8 bg-slate-200 rounded w-1/4 mb-2"></div>
        <div className="h-3 bg-slate-200 rounded w-2/3"></div>
      </div>
    );
  }

  if (!dadosScore) return null;

  const estilo = obterEstiloFaixa(dadosScore.nivel, dadosScore.score);
  // Escala de 0 a 100 pontos igual ao back-end
  const porcentagem = Math.min(Math.max(dadosScore.score, 5), 100);

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-slate-100 p-6">
      <div className="flex items-center justify-between mb-3">
        <div>
          <h3 className="text-sm font-medium text-slate-500">Score Financeiro</h3>
          <div className="flex items-baseline gap-2 mt-1">
            <span className={`text-3xl font-bold ${estilo.texto}`}>
              {dadosScore.score}
            </span>
            <span className="text-xs text-slate-400 font-medium">/ 100 pts</span>
          </div>
        </div>

        <span
          className={`px-3 py-1 rounded-full text-xs font-semibold border ${estilo.badge}`}
        >
          {dadosScore.nivel}
        </span>
      </div>

      {/* Barra de progresso da faixa do score */}
      <div className="w-full bg-slate-100 h-2.5 rounded-full overflow-hidden mb-3">
        <div
          className={`h-full rounded-full transition-all duration-500 ${estilo.barra}`}
          style={{ width: `${porcentagem}%` }}
        />
      </div>

      {/* Descrição retornada pelo back-end */}
      <p className="text-xs text-slate-600 leading-relaxed">
        {dadosScore.descricao}
      </p>
    </div>
  );
};