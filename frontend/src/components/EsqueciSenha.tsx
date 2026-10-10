import React, { useState } from 'react';

interface EsqueciSenhaProps {
  onNavigateToLogin: () => void;
}

export const EsqueciSenha: React.FC<EsqueciSenhaProps> = ({ onNavigateToLogin }) => {
  const [email, setEmail] = useState('');
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email) {
      setErro('O e-mail é obrigatório.');
      return;
    }

    if (!regexEmail.test(email)) {
      setErro('Informe um e-mail válido.');
      return;
    }

    setErro('');
    setSucesso(true);
  };

  return (
    <div className="min-h-screen w-full flex flex-col items-center justify-center bg-gradient-to-br from-[#8a0047] via-[#c4003d] to-[#e60039] px-4 py-8 relative overflow-hidden">
      
      {/* Efeitos sutis de iluminação no fundo */}
      <div className="absolute -top-24 -left-24 w-96 h-96 bg-white/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-24 -right-24 w-96 h-96 bg-black/20 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md space-y-6 z-10">
        
        {/* Marca/Cabeçalho da tela */}
        <div className="text-center space-y-2">
          <div className="flex justify-center mb-1">
            <svg
              className="w-16 h-16 text-white drop-shadow-md"
              viewBox="0 0 100 100"
              fill="currentColor"
            >
              <path d="M50 15 C30 15 15 30 15 50 C15 60 20 70 30 75 C25 65 25 50 35 40 C45 30 60 30 70 40 C60 25 45 15 50 15 Z" />
              <path d="M45 55 L45 80 L50 80 L50 55 Z" />
              <path d="M55 55 L55 80 L60 80 L60 55 Z" />
            </svg>
          </div>

          <h1 className="text-3xl font-extrabold text-white tracking-tight drop-shadow-sm">
            ByteBank <span className="font-light text-xl">finance</span>
          </h1>
          <p className="text-xs sm:text-sm text-white/80 font-medium">
            Recuperação de Acesso
          </p>
        </div>

        {/* Card do Formulário */}
        <div className="bg-white/95 backdrop-blur-md p-6 sm:p-8 rounded-2xl shadow-2xl space-y-5 border border-white/20">
          {sucesso ? (
            <div className="text-center space-y-4 py-2">
              <div className="text-5xl">📩</div>
              <h3 className="text-lg font-bold text-slate-800">E-mail enviado!</h3>
              <p className="text-xs text-slate-600">
                Enviamos as instruções para <span className="font-semibold text-slate-800">{email}</span>. Verifique sua caixa de entrada.
              </p>
              <button
                type="button"
                onClick={onNavigateToLogin}
                className="w-full mt-4 py-3 bg-gradient-to-r from-[#c4003d] to-[#e60039] text-white rounded-xl text-sm font-bold shadow-md hover:opacity-95 transition-all duration-200 cursor-pointer"
              >
                Voltar para o Login
              </button>
            </div>
          ) : (
            <>
              <div className="text-center border-b border-slate-100 pb-3">
                <h2 className="text-lg font-bold text-slate-800">Esqueceu sua senha?</h2>
                <p className="text-xs text-slate-500">
                  Digite seu e-mail cadastrado para enviarmos o link de redefinição
                </p>
              </div>

              <form className="space-y-4" onSubmit={handleSubmit}>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    E-mail *
                  </label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="seu.email@exemplo.com"
                    className={`w-full rounded-xl border ${
                      erro ? 'border-red-500 focus:ring-red-500' : 'border-slate-300 focus:ring-[#c4003d]'
                    } px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 transition`}
                  />
                  {erro && <p className="mt-1 text-xs text-red-500 font-medium">{erro}</p>}
                </div>

                <button
                  type="submit"
                  className="w-full bg-gradient-to-r from-[#c4003d] to-[#e60039] text-white font-bold py-3 rounded-xl shadow-md hover:opacity-95 transition-all duration-200 cursor-pointer text-sm"
                >
                  Enviar E-mail de Recuperação
                </button>
              </form>

              <div className="text-center border-t border-slate-100 pt-4">
                <button
                  type="button"
                  onClick={onNavigateToLogin}
                  className="text-xs font-bold text-[#c4003d] hover:underline cursor-pointer"
                >
                  ← Voltar para o Login
                </button>
              </div>
            </>
          )}
        </div>

        {/* Rodapé */}
        <p className="text-center text-xs text-white/60">
          © 2026 Bradesco Finanças. Todos os direitos reservados.
        </p>
      </div>
    </div>
  );
};