import React, { useState } from 'react';
import { cadastrarUsuario } from '../Services/api';

interface CadastroProps {
  onCadastroSuccess: () => void;
  onNavigateToLogin: () => void;
}

export const Cadastro: React.FC<CadastroProps> = ({ onCadastroSuccess, onNavigateToLogin }) => {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [carregando, setCarregando] = useState(false);
  const [erros, setErros] = useState<{ 
    nome?: string; 
    email?: string; 
    senha?: string; 
    confirmarSenha?: string; 
    geral?: string 
  }>({});

  const validar = () => {
    const novosErros: typeof erros = {};
    const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const regexSenhaForte = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;

    if (!nome.trim()) novosErros.nome = 'O nome completo é obrigatório.';
    
    if (!email) {
      novosErros.email = 'O e-mail é obrigatório.';
    } else if (!regexEmail.test(email)) {
      novosErros.email = 'Informe um e-mail válido.';
    }

    if (!senha) {
      novosErros.senha = 'A senha é obrigatória.';
    } else if (!regexSenhaForte.test(senha)) {
      novosErros.senha = 'Mínimo 8 caracteres (com letras e números).';
    }

    if (confirmarSenha !== senha) {
      novosErros.confirmarSenha = 'As senhas não coincidem.';
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
        // 1. Chamada assíncrona ao Spring Boot
        await cadastrarUsuario({ nome, email, senha });
        
        setCarregando(false);
        // 2. Notifica o componente pai sobre o sucesso do cadastro
        onCadastroSuccess();
      } catch (err: any) {
        setCarregando(false);
        setErros({ 
          geral: err.message || 'Erro ao cadastrar usuário. Verifique se o e-mail já está em uso.' 
        });
      }
    }
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
            Crie sua conta gratuita em instantes
          </p>
        </div>

        {/* Card do Formulário */}
        <div className="bg-white/95 backdrop-blur-md p-6 sm:p-8 rounded-2xl shadow-2xl space-y-4 border border-white/20">
          <div className="text-center border-b border-slate-100 pb-3">
            <h2 className="text-lg font-bold text-slate-800">Criar nova conta</h2>
            <p className="text-xs text-slate-500">Preencha seus dados para começar</p>
          </div>

          {erros.geral && (
            <div className="bg-red-50 border border-red-200 text-red-600 px-3 py-2 rounded-xl text-xs font-medium text-center">
              {erros.geral}
            </div>
          )}

          <form className="space-y-3" onSubmit={handleSubmit}>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Nome Completo *
              </label>
              <input
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Ex: João da Silva"
                className={`w-full rounded-xl border ${
                  erros.nome ? 'border-red-500 focus:ring-red-500' : 'border-slate-300 focus:ring-[#c4003d]'
                } px-3.5 py-2 text-sm focus:outline-none focus:ring-2 transition`}
              />
              {erros.nome && <p className="mt-1 text-xs text-red-500 font-medium">{erros.nome}</p>}
            </div>

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
                } px-3.5 py-2 text-sm focus:outline-none focus:ring-2 transition`}
              />
              {erros.email && <p className="mt-1 text-xs text-red-500 font-medium">{erros.email}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Senha *
              </label>
              <input
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                placeholder="Mínimo 8 caracteres (letras e números)"
                className={`w-full rounded-xl border ${
                  erros.senha ? 'border-red-500 focus:ring-red-500' : 'border-slate-300 focus:ring-[#c4003d]'
                } px-3.5 py-2 text-sm focus:outline-none focus:ring-2 transition`}
              />
              {erros.senha && <p className="mt-1 text-xs text-red-500 font-medium">{erros.senha}</p>}
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Confirmar Senha *
              </label>
              <input
                type="password"
                value={confirmarSenha}
                onChange={(e) => setConfirmarSenha(e.target.value)}
                placeholder="Repita sua senha"
                className={`w-full rounded-xl border ${
                  erros.confirmarSenha ? 'border-red-500 focus:ring-red-500' : 'border-slate-300 focus:ring-[#c4003d]'
                } px-3.5 py-2 text-sm focus:outline-none focus:ring-2 transition`}
              />
              {erros.confirmarSenha && <p className="mt-1 text-xs text-red-500 font-medium">{erros.confirmarSenha}</p>}
            </div>

            <button
              type="submit"
              disabled={carregando}
              className="w-full bg-gradient-to-r from-[#c4003d] to-[#e60039] text-white font-bold py-3 rounded-xl shadow-md hover:opacity-95 transition-all duration-200 cursor-pointer text-sm mt-2 flex items-center justify-center space-x-2 disabled:opacity-75 disabled:cursor-not-allowed"
            >
              {carregando ? (
                <>
                  <svg className="animate-spin h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span>Criando Conta...</span>
                </>
              ) : (
                <span>Criar Conta</span>
              )}
            </button>
          </form>

          <div className="text-center border-t border-slate-100 pt-4">
            <p className="text-xs text-slate-600">
              Já possui uma conta?{' '}
              <button
                type="button"
                onClick={onNavigateToLogin}
                className="font-bold text-[#c4003d] hover:underline cursor-pointer"
              >
                Fazer Login
              </button>
            </p>
          </div>
        </div>

        {/* Rodapé */}
        <p className="text-center text-xs text-white/60">
          © 2026 Bradesco Finanças. Todos os direitos reservados.
        </p>
      </div>
    </div>
  );
};