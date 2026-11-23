import { useEffect, useState } from 'react';
import { TrendingDown, ShieldAlert, CheckCircle2, Info } from 'lucide-react';
import type { Transacao } from '../types';

interface InsightResponse {
  tipo: string;
  mensagem: string;
  data: string;
  nivelAlerta: string;
}

// Formata somente a exibição de valores das mensagens, sem alterar o cálculo.
const formatarMoedaNaMensagem = (mensagem: string) =>
  mensagem.replace(/R\$\s*(\d+(?:\.\d{1,2})?)/g, (_trecho, numero: string) =>
    Number(numero).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
  );

interface AlertasFinanceirosProps {
  atualizarGatilho?: Transacao[];
}

export function AlertasFinanceiros({ atualizarGatilho }: AlertasFinanceirosProps) {
  const [quedaRenda, setQuedaRenda] = useState<InsightResponse | null>(null);
  const [reserva, setReserva] = useState<InsightResponse | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    const usuarioSalvo = localStorage.getItem('usuario');
    const usuarioId =
      localStorage.getItem('usuarioId') ||
      (usuarioSalvo ? JSON.parse(usuarioSalvo).idUsuario : null) ||
      '1';

    const buscarInsights = async () => {
      try {
        setCarregando(true);
        const [resQueda, resReserva] = await Promise.all([
          fetch(`http://localhost:8080/insights/${usuarioId}/queda-renda`, {
            credentials: 'include',
          }),
          fetch(`http://localhost:8080/insights/${usuarioId}/reserva-insuficiente`, {
            credentials: 'include',
          }),
        ]);

        if (resQueda.ok) {
          setQuedaRenda(await resQueda.json());
        }
        if (resReserva.ok) {
          setReserva(await resReserva.json());
        }
      } catch (erro) {
        console.error('Erro ao buscar diagnósticos de renda e reserva:', erro);
      } finally {
        setCarregando(false);
      }
    };

    buscarInsights();
  }, [atualizarGatilho]);

  const emAlertaQueda =
    quedaRenda?.nivelAlerta?.toLowerCase() === 'atenção' ||
    quedaRenda?.tipo?.toLowerCase().includes('queda');

  return (
    <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {/* Card 1: Detecção de Queda de Renda */}
      <div
        className={`rounded-2xl border p-5 shadow-sm transition-all ${
          emAlertaQueda
            ? 'bg-amber-50/70 border-amber-200'
            : 'bg-white border-slate-200'
        }`}
      >
        <div className="flex items-start gap-3.5">
          <div
            className={`p-2.5 rounded-xl shrink-0 ${
              emAlertaQueda
                ? 'bg-amber-100 text-amber-700'
                : 'bg-emerald-100 text-emerald-700'
            }`}
          >
            {emAlertaQueda ? (
              <TrendingDown className="w-5 h-5" />
            ) : (
              <CheckCircle2 className="w-5 h-5" />
            )}
          </div>

          <div className="space-y-1 flex-1">
            <div className="flex items-center justify-between gap-2">
              <h3 className="text-sm font-semibold text-slate-800">
                {carregando ? 'Analisando renda...' : quedaRenda?.tipo || 'Monitoramento de Renda'}
              </h3>
              {quedaRenda?.nivelAlerta && (
                <span
                  className={`text-[11px] font-medium px-2.5 py-0.5 rounded-full ${
                    emAlertaQueda
                      ? 'bg-amber-200/80 text-amber-900'
                      : 'bg-slate-100 text-slate-600'
                  }`}
                >
                  {quedaRenda.nivelAlerta}
                </span>
              )}
            </div>
            <p className="text-sm text-slate-600 leading-relaxed">
              {carregando
                ? 'Comparando sua receita do último mês com a média trimestral...'
                : quedaRenda?.mensagem ||
                  'Não foi possível carregar a análise de queda de renda.'}
            </p>
          </div>
        </div>
      </div>

      {/* Card 2: Reserva de Emergência */}
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-all">
        <div className="flex items-start gap-3.5">
          <div className="p-2.5 rounded-xl bg-indigo-100 text-indigo-700 shrink-0">
            {reserva?.tipo === 'Dados insuficientes' ? (
              <Info className="w-5 h-5" />
            ) : (
              <ShieldAlert className="w-5 h-5" />
            )}
          </div>

          <div className="space-y-1 flex-1">
            <div className="flex items-center justify-between gap-2">
              <h3 className="text-sm font-semibold text-slate-800">
                {carregando ? 'Calculando reserva...' : reserva?.tipo || 'Reserva de Emergência'}
              </h3>
              {reserva?.nivelAlerta && (
                <span className="text-[11px] font-medium px-2.5 py-0.5 rounded-full bg-indigo-50 text-indigo-700">
                  {reserva.nivelAlerta}
                </span>
              )}
            </div>
            <p className="text-sm text-slate-600 leading-relaxed">
              {carregando
                ? 'Calculando reserva recomendada a partir das despesas mensais...'
                : reserva?.mensagem
                  ? formatarMoedaNaMensagem(reserva.mensagem)
                  : 'Não foi possível carregar o cálculo da reserva de emergência.'}
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}