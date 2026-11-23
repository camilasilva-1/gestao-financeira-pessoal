import React, { useState, useEffect, useRef } from 'react';

interface HeaderProps {
  saldoTotal: number;
  onLogout?: () => void;
}

// Atalhos apenas para áreas relevantes que ficam mais abaixo no dashboard.
const secoes = [
  { id: 'visao-geral', titulo: 'Visão geral', icone: '⌂' },
  { id: 'patrimonio', titulo: 'Evolução do patrimônio', icone: '↗' },
  { id: 'recomendacoes', titulo: 'Recomendações financeiras', icone: '✦' },
  { id: 'relatorios-salvos', titulo: 'Histórico de relatórios mensais', icone: '▤' }
] as const;

export const Header: React.FC<HeaderProps> = ({ saldoTotal, onLogout }) => {
  const [nomeCompleto, setNomeCompleto] = useState('Usuário');
  const [menuAberto, setMenuAberto] = useState(false);
  const [perfilAberto, setPerfilAberto] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const nomeUsuario = nomeCompleto.trim().split(/\s+/)[0] || 'Usuário';

  useEffect(() => {
    const nomeSalvo = localStorage.getItem('usuarioNome');
    if (nomeSalvo?.trim()) setNomeCompleto(nomeSalvo.trim());
  }, []);

  // Fecha o menu ao clicar fora dele ou pressionar Escape.
  useEffect(() => {
    if (!menuAberto) return;
    const fecharFora = (evento: PointerEvent) => {
      if (!menuRef.current?.contains(evento.target as Node)) {
        setMenuAberto(false);
        setPerfilAberto(false);
      }
    };
    const fecharEscape = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') {
        setMenuAberto(false);
        setPerfilAberto(false);
      }
    };
    document.addEventListener('pointerdown', fecharFora);
    document.addEventListener('keydown', fecharEscape);
    return () => {
      document.removeEventListener('pointerdown', fecharFora);
      document.removeEventListener('keydown', fecharEscape);
    };
  }, [menuAberto]);

  const irPara = (id: string) => {
    const destino = document.getElementById(id);
    if (!destino) return;
    setMenuAberto(false);
    setPerfilAberto(false);
    destino.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  return (
    <header className="relative bg-gradient-to-r from-[#f0005c] via-[#e6004c] to-[#cc092f] text-white pt-6 pb-10 px-4 sm:px-6 rounded-b-[2.5rem] shadow-md mb-10">
      <div className="max-w-5xl mx-auto space-y-4">
        <div className="flex justify-between items-center gap-3 border-b border-white/20 pb-3">
          <h1 className="text-xl font-bold tracking-wide">
            ByteBank <span className="font-light text-xs">finance</span>
          </h1>

          <div className="flex items-center gap-2 sm:gap-3">
            <span className="hidden sm:inline text-xs font-medium text-white/90">Olá, {nomeUsuario}</span>
            <div className="relative" ref={menuRef}>
              <button
                type="button"
                aria-label={menuAberto ? 'Fechar menu de navegação' : 'Abrir menu de navegação'}
                aria-expanded={menuAberto}
                aria-controls="menu-secoes"
                onClick={() => {
                  setMenuAberto(aberto => !aberto);
                  setPerfilAberto(false);
                }}
                className="flex h-10 w-10 items-center justify-center rounded-xl border border-white/30 bg-white/10 text-white transition-colors hover:bg-white/20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-white"
              >
                <span aria-hidden="true" className="text-xl leading-none">{menuAberto ? '×' : '☰'}</span>
              </button>

              {menuAberto && (
                <nav
                  id="menu-secoes"
                  aria-label="Navegação do ByteBank"
                  className="absolute right-0 top-full z-50 mt-3 w-[min(19rem,calc(100vw-2rem))] overflow-hidden rounded-2xl border border-slate-200 bg-white text-slate-800 shadow-2xl"
                >
                  <div className="bg-rose-50 px-4 py-4 border-b border-rose-100">
                    <p className="text-[11px] font-bold uppercase tracking-widest text-[#ae1646]">Sua conta</p>
                    <p className="mt-1 truncate text-base font-bold text-slate-900" title={nomeCompleto}>{nomeCompleto}</p>
                    <button
                      type="button"
                      aria-expanded={perfilAberto}
                      onClick={() => setPerfilAberto(aberto => !aberto)}
                      className="mt-3 flex w-full items-center justify-between rounded-lg border border-rose-200 bg-white px-3 py-2 text-left text-sm font-semibold text-[#a8093a] transition-colors hover:bg-rose-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#c4003d]"
                    >
                      Meu perfil <span aria-hidden="true">{perfilAberto ? '⌃' : '⌄'}</span>
                    </button>
                    {perfilAberto && (
                      <div className="mt-2 rounded-lg bg-white p-3 text-sm text-slate-600">
                        <p className="text-xs font-medium text-slate-500">Nome do usuário</p>
                        <p className="mt-1 break-words font-semibold text-slate-800">{nomeCompleto}</p>
                      </div>
                    )}
                  </div>

                  <div className="px-2 py-3">
                    <p className="px-3 pb-2 text-[11px] font-bold uppercase tracking-widest text-slate-500">Navegação</p>
                    {secoes.map(({ id, titulo, icone }) => (
                      <button
                        key={id}
                        type="button"
                        onClick={() => irPara(id)}
                        className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-sm font-medium text-slate-700 transition-colors hover:bg-rose-50 hover:text-[#a8093a] focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#c4003d]"
                      >
                        <span aria-hidden="true" className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-base text-[#b20b42]">{icone}</span>
                        <span>{titulo}</span>
                      </button>
                    ))}
                  </div>
                  <div className="border-t border-slate-100 p-2">
                    <button
                      type="button"
                      onClick={() => {
                        setMenuAberto(false);
                        onLogout?.();
                      }}
                      className="w-full rounded-lg px-3 py-2 text-left text-sm font-medium text-slate-600 transition-colors hover:bg-slate-100 focus-visible:outline focus-visible:outline-2 focus-visible:outline-[#c4003d]"
                    >
                      Sair da conta
                    </button>
                  </div>
                </nav>
              )}
            </div>
            <button
              type="button"
              onClick={onLogout}
              className="text-xs bg-white/10 hover:bg-white/20 text-white font-medium px-3 py-2 rounded-full transition-colors border border-white/20 cursor-pointer"
            >
              Sair
            </button>
          </div>
        </div>

        <div className="pt-2">
          <p className="text-xs uppercase tracking-wider text-white/80 font-medium">Meu saldo disponível</p>
          <p className="text-3xl sm:text-4xl font-extrabold mt-1">
            R$ {saldoTotal.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </p>
        </div>
      </div>
    </header>
  );
};
