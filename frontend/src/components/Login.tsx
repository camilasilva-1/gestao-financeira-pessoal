import React, { useState } from 'react';
import { loginUsuario } from '../Services/api';

interface LoginProps {
  onLoginSuccess: () => void;
  onNavigateToCadastro: () => void;
  onNavigateToEsqueciSenha: () => void;
}

export const Login: React.FC<LoginProps> = ({
  onLoginSuccess,
  onNavigateToCadastro,
  onNavigateToEsqueciSenha,
}) => {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [lembrarMe, setLembrarMe] = useState(false);
  const [carregando, setCarregando] = useState(false);
  const [shake, setShake] = useState(false);
  const [erros, setErros] = useState<{ email?: string; senha?: string; geral?: string }>({});

  const triggerShake = () => {
    setShake(true);
    setTimeout(() => setShake(false), 500);
  };

  const validar = () => {
    const novosErros: { email?: string; senha?: string } = {};
    const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email) {
      novosErros.email = 'O e-mail é obrigatório.';
    } else if (!regexEmail.test(email)) {
      novosErros.email = 'Informe um e-mail válido.';
    }

    if (!senha) {
      novosErros.senha = 'A senha é obrigatória.';
    } else if (senha.length < 6) {
      novosErros.senha = 'A senha deve ter no mínimo 6 caracteres.';
    }

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (validar()) {
      setCarregando(true);
      setErros({});

      try {
        // 1. Chamada real à API do Spring Boot
        const usuario = await loginUsuario({ email, senha });

        // 2. Guarda o id do utilizador no navegador para usar nas requisições do Dashboard/Transações
        localStorage.setItem('usuarioId', usuario.idUsuario || usuario.id);
        if (usuario.nome) {
          localStorage.setItem('usuarioNome', usuario.nome);
        }

        setCarregando(false);
        onLoginSuccess();
      } catch (err: any) {
        setCarregando(false);
        triggerShake();
        setErros({ geral: err.message || 'Falha ao autenticar. Verifique o e-mail e a senha.' });
      }
    } else {
      triggerShake();
    }
  };

  return (
    <div className="min-h-screen w-full flex flex-col items-center justify-center bg-gradient-to-br from-[#8a0047] via-[#c4003d] to-[#e60039] px-4 py-8 relative overflow-hidden">
      
      {/* Efeitos sutis de iluminação no fundo */}
      <div className="absolute -top-24 -left-24 w-96 h-96 bg-white/10 rounded-full blur-3xl pointer-events-none animate-pulse" />
      <div className="absolute -bottom-24 -right-24 w-96 h-96 bg-black/20 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md space-y-6 z-10">
        
        {/* Marca/Cabeçalho da tela de apresentação */}
        <div className="text-center space-y-2 animate-fade-in">
          <div className="flex justify-center mb-1 transition-transform duration-300 hover:scale-105">
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
            ByteBank <span className="font-light text-xl">Finance</span>
          </h1>
          <p className="text-xs sm:text-sm text-white/80 font-medium">
            Gestão financeira simples, moderna e segura.
          </p>
        </div>

        {/* Card do Formulário */}
        <div 
          className={`bg-white/95 backdrop-blur-md p-6 sm:p-8 rounded-2xl shadow-2xl space-y-5 border border-white/20 transition-all duration-300 ${
            shake ? 'animate-bounce' : ''
          }`}
        >
          <div className="text-center border-b border-slate-100 pb-3">
            <h2 className="text-lg font-bold text-slate-800">Acesse sua conta</h2>
            <p className="text-xs text-slate-500">Informe suas credenciais para continuar</p>
          </div>

          {erros.geral && (
            <div className="bg-red-50 border border-red-200 text-red-600 px-3 py-2 rounded-xl text-xs font-medium text-center">
              {erros.geral}
            </div>
          )}

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
                  erros.email ? 'border-red-500 focus:ring-red-500' : 'border-slate-300 focus:ring-[#c4003d]'
                } px-3.5 py-2.5 text-sm focus:outline-none focus:ring-2 transition-all duration-200`}
              />
              {erros.email && <p className="mt-1 text-xs text-red-500 font-medium">{erros.email}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Senha *
              </label>
              <div className="relative">
                <input
                  type={mostrarSenha ? 'text' : 'password'}
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                  placeholder="••••••••"
                  className={`w-full rounded-xl border ${
                    erros.senha ? 'border-red-500 focus:ring-red-500' : 'border-slate-300 focus:ring-[#c4003d]'
                  } pl-3.5 pr-10 py-2.5 text-sm focus:outline-none focus:ring-2 transition-all duration-200`}
                />
                <button
                  type="button"
                  onClick={() => setMostrarSenha(!mostrarSenha)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none transition-colors"
                  aria-label={mostrarSenha ? 'Ocultar senha' : 'Exibir senha'}
                >
                  {mostrarSenha ? (
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-7 0-10-7-10-7a19.04 19.04 0 014.288-5.322m3.226-2.18A9.972 9.972 0 0112 4c7 0 10 7 10 7a18.98 18.98 0 01-3.21 4.28M15 12a3 3 0 11-6 0 3 3 0 016 0zM3 3l18 18" />
                    </svg>
                  ) : (
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                  )}
                </button>
              </div>
              {erros.senha && <p className="mt-1 text-xs text-red-500 font-medium">{erros.senha}</p>}
            </div>

            <div className="flex items-center justify-between text-xs">
              <label className="flex items-center space-x-2 cursor-pointer text-slate-600 select-none">
                <input
                  type="checkbox"
                  checked={lembrarMe}
                  onChange={(e) => setLembrarMe(e.target.value ? e.target.checked : false)}
                  className="w-4 h-4 rounded text-[#c4003d] focus:ring-[#c4003d] border-slate-300 transition"
                />
                <span>Lembrar-me</span>
              </label>

              <button
                type="button"
                onClick={onNavigateToEsqueciSenha}
                className="font-semibold text-[#c4003d] hover:underline cursor-pointer"
              >
                Esqueceu sua senha?
              </button>
            </div>

            <button
              type="submit"
              disabled={carregando}
              className="w-full bg-gradient-to-r from-[#c4003d] to-[#e60039] hover:from-[#ab0035] hover:to-[#cd0033] active:scale-[0.99] text-white font-bold py-3 rounded-xl shadow-md transition-all duration-200 cursor-pointer text-sm flex items-center justify-center space-x-2 disabled:opacity-75 disabled:cursor-not-allowed"
            >
              {carregando ? (
                <>
                  <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span>Entrando...</span>
                </>
              ) : (
                <span>Entrar</span>
              )}
            </button>
          </form>

          <div className="text-center border-t border-slate-100 pt-4">
            <p className="text-xs text-slate-600">
              Ainda não tem uma conta?{' '}
              <button
                type="button"
                onClick={onNavigateToCadastro}
                className="font-bold text-[#c4003d] hover:underline cursor-pointer"
              >
                Cadastre-se
              </button>
            </p>
          </div>
        </div>

        {/* Rodapé */}
        <p className="text-center text-xs text-white/60">
          © 2026 Gestão Financeira. Todos os direitos reservados.
        </p>
      </div>
    </div>
  );
};