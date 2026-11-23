import React, { useState, useEffect } from 'react';

interface HeaderProps {
  saldoTotal: number;
  onLogout?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ saldoTotal, onLogout }) => {
  const [nomeUsuario, setNomeUsuario] = useState('Usuário');

  useEffect(() => {
    // Pega o nome que foi salvo no localStorage durante o Login
    const nomeSalvo = localStorage.getItem('usuarioNome');
    if (nomeSalvo) {
      // Exibe apenas o primeiro nome ("Giovana") para manter a interface limpa
      const primeiroNome = nomeSalvo.split(' ')[0];
      setNomeUsuario(primeiroNome);
    }
  }, []);

  const [menuAberto, setMenuAberto] = useState(false);
  const secoes = [
    ['visao-geral', 'Visão geral'],
    ['score', 'Score financeiro'],
    ['alertas', 'Alertas financeiros'],
    ['patrimonio', 'Evolução do patrimônio'],
    ['recomendacoes', 'Recomendações financeiras'],
    ['relatorios', 'Relatórios mensais'],
    ['planejamento', 'Planejamento 50/30/20'],
    ['relatorios-salvos', 'Relatórios salvos'],
    ['ranking', 'Ranking por categoria'],
    ['nova-transacao', 'Nova transação'],
    ['historico-transacoes', 'Histórico de transações'],
    ['orcamentos', 'Orçamentos'],
    ['metas', 'Metas financeiras']
  ] as const;

  const irPara = (id: string) => {
    setMenuAberto(false);
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  useEffect(() => {
    const aoPressionarTecla = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') setMenuAberto(false);
    };
    document.addEventListener('keydown', aoPressionarTecla);
    return () => document.removeEventListener('keydown', aoPressionarTecla);
  }, []);

  return (
    <header className="bg-gradient-to-r from-[#f0005c] via-[#e6004c] to-[#cc092f] text-white pt-6 pb-10 px-6 rounded-b-[2.5rem] shadow-md mb-10">
      <div className="max-w-5xl mx-auto space-y-4">
        <div className="flex justify-between items-center border-b border-white/20 pb-3">
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-wide">
              ByteBank <span className="font-light text-xs">finance</span>
            </h1>
          </div>

          <div className="flex items-center gap-3">
            <div className="relative">
              <button
                type="button"
                aria-label="Abrir menu de navegação"
                aria-expanded={menuAberto}
                aria-controls="menu-secoes"
                onClick={() => setMenuAberto(!menuAberto)}
                className="rounded-lg border border-white/30 p-2 text-white hover:bg-white/20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
              >
                <span aria-hidden="true" className="text-xl leading-none">☰</span>
              </button>
              {menuAberto && (
                <div id="menu-secoes" className="absolute right-0 top-full z-50 mt-2 w-64 max-h-[70vh] overflow-y-auto rounded-xl border border-slate-200 bg-white py-2 text-slate-800 shadow-xl">
                  <div className="px-4 py-2 text-xs font-bold uppercase tracking-wide text-[#c4003d]">Navegar pelo ByteBank</div>
                  {secoes.map(([id, titulo]) => (
                    <button
                      key={id}
                      type="button"
                      onClick={() => irPara(id)}
                      className="block w-full px-4 py-2 text-left text-sm hover:bg-rose-50 focus-visible:bg-rose-50 focus-visible:outline-none"
                    >
                      {titulo}
                    </button>
                  ))}
                </div>
              )}
            </div>
            {/* Nome dinâmico aqui */}
            <span className="text-xs font-medium text-white/90">Olá, {nomeUsuario}</span>
            <button
              type="button"
              onClick={onLogout}
              className="text-xs bg-white/10 hover:bg-white/20 text-white font-medium px-3 py-1.5 rounded-full transition-colors border border-white/20 cursor-pointer"
            >
              Sair
            </button>
          </div>
        </div>

        <div className="pt-2">
          <p className="text-xs uppercase tracking-wider text-white/80 font-medium">
            Meu saldo disponível
          </p>
          <p className="text-3xl sm:text-4xl font-extrabold mt-1">
            R$ {saldoTotal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
          </p>
        </div>
      </div>
    </header>
  );
};
