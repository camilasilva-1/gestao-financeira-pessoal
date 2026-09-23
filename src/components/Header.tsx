import React from 'react';

interface HeaderProps {
  saldoTotal: number;
  onLogout?: () => void;
}

export const Header: React.FC<HeaderProps> = ({ saldoTotal, onLogout }) => {
  return (
    // Adicionámos mb-10 para dar o espaçamento necessário em relação ao Dashboard
    <header className="bg-gradient-to-r from-[#f0005c] via-[#e6004c] to-[#cc092f] text-white pt-6 pb-10 px-6 rounded-b-[2.5rem] shadow-md mb-10">
      <div className="max-w-5xl mx-auto space-y-4">
        <div className="flex justify-between items-center border-b border-white/20 pb-3">
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold tracking-wide">ByteBank <span className="font-light text-xs">finance</span></h1>
          </div>
          
          <div className="flex items-center gap-3">
            <span className="text-xs font-medium text-white/90">Olá, Usuário</span>
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
          <p className="text-xs uppercase tracking-wider text-white/80 font-medium">Meu saldo disponível</p>
          <p className="text-3xl sm:text-4xl font-extrabold mt-1">
            R$ {saldoTotal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}
          </p>
        </div>
      </div>
    </header>
  );
};