import React, { useState } from 'react';

interface CadastroProps {
  onCadastroSuccess: () => void;
  onNavigateToLogin: () => void;
}

export const Cadastro: React.FC<CadastroProps> = ({ onCadastroSuccess, onNavigateToLogin }) => {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [erros, setErros] = useState<{ nome?: string; email?: string; senha?: string; confirmarSenha?: string }>({});

  const validar = () => {
    const novosErros: typeof erros = {};
    const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    // Regex de senha forte: mínimo 8 caracteres, pelo menos 1 letra e 1 número
    const regexSenhaForte = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!\%*#?&]{8,}$/;

    if (!nome.trim()) novosErros.nome = 'O nome completo é obrigatório.';
    
    if (!email) {
      novosErros.email = 'O e-mail é obrigatório.';
    } else if (!regexEmail.test(email)) {
      novosErros.email = 'Informe um e-mail válido.';
    }

    if (!senha) {
      novosErros.senha = 'A senha é obrigatória.';
    } else if (!regexSenhaForte.test(senha)) {
      novosErros.senha = 'A senha deve ter no mínimo 8 caracteres, contendo letras e números.';
    }

    if (confirmarSenha !== senha) {
      novosErros.confirmarSenha = 'As senhas não coincidem.';
    }

    setErros(novosErros);
    return Object.keys(novosErros).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validar()) {
      // Simulação de cadastro bem-sucedido
      onCadastroSuccess();
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <h2 className="text-3xl font-extrabold text-red-600">Bradesco Finanças</h2>
        <p className="mt-2 text-sm text-slate-600">Crie sua conta gratuita em instantes</p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow-sm border border-slate-200 sm:rounded-xl sm:px-10">
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div>
              <label className="block text-sm font-medium text-slate-700">Nome Completo *</label>
              <input
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                placeholder="Ex: João da Silva"
                className={`mt-1 block w-full rounded-md border ${
                  erros.nome ? 'border-red-500' : 'border-slate-300'
                } px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-red-600`}
              />
              {erros.nome && <p className="mt-1 text-xs text-red-500">{erros.nome}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700">E-mail *</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="seu.email@exemplo.com"
                className={`mt-1 block w-full rounded-md border ${
                  erros.email ? 'border-red-500' : 'border-slate-300'
                } px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-red-600`}
              />
              {erros.email && <p className="mt-1 text-xs text-red-500">{erros.email}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700">Senha *</label>
              <input
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                placeholder="Mínimo 8 caracteres (letras e números)"
                className={`mt-1 block w-full rounded-md border ${
                  erros.senha ? 'border-red-500' : 'border-slate-300'
                } px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-red-600`}
              />
              {erros.senha && <p className="mt-1 text-xs text-red-500">{erros.senha}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700">Confirmar Senha *</label>
              <input
                type="password"
                value={confirmarSenha}
                onChange={(e) => setConfirmarSenha(e.target.value)}
                placeholder="Repita sua senha"
                className={`mt-1 block w-full rounded-md border ${
                  erros.confirmarSenha ? 'border-red-500' : 'border-slate-300'
                } px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-red-600`}
              />
              {erros.confirmarSenha && <p className="mt-1 text-xs text-red-500">{erros.confirmarSenha}</p>}
            </div>

            <button
              type="submit"
              className="w-full py-2.5 px-4 border border-transparent rounded-lg shadow-sm text-sm font-semibold text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 mt-2"
            >
              Criar Conta
            </button>
          </form>

          <div className="mt-6 text-center border-t border-slate-200 pt-4">
            <p className="text-xs text-slate-600">
              Já possui uma conta?{' '}
              <button
                onClick={onNavigateToLogin}
                className="font-semibold text-red-600 hover:text-red-500"
              >
                Fazer Login
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};