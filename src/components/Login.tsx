import React, { useState } from 'react';

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
  const [erros, setErros] = useState<{ email?: string; senha?: string }>({});

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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validar()) {
      // Simulação de login bem-sucedido
      onLoginSuccess();
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <h2 className="text-3xl font-extrabold text-red-600">ByteBank Finance</h2>
        <p className="mt-2 text-sm text-slate-600">Acesse sua conta para gerenciar suas finanças</p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow-sm border border-slate-200 sm:rounded-xl sm:px-10">
          <form className="space-y-5" onSubmit={handleSubmit}>
            <div>
              <label className="block text-sm font-medium text-slate-700">E-mail *</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="seu.email@exemplo.com"
                className={`mt-1 block w-full rounded-md border ${
                  erros.email ? 'border-red-500 focus:ring-red-500' : 'border-slate-300 focus:ring-red-600'
                } px-3 py-2 text-sm focus:outline-none focus:ring-1`}
              />
              {erros.email && <p className="mt-1 text-xs text-red-500">{erros.email}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700">Senha *</label>
              <input
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                placeholder="••••••••"
                className={`mt-1 block w-full rounded-md border ${
                  erros.senha ? 'border-red-500 focus:ring-red-500' : 'border-slate-300 focus:ring-red-600'
                } px-3 py-2 text-sm focus:outline-none focus:ring-1`}
              />
              {erros.senha && <p className="mt-1 text-xs text-red-500">{erros.senha}</p>}
            </div>

            <div className="flex items-center justify-between">
              <button
                type="button"
                onClick={onNavigateToEsqueciSenha}
                className="text-xs font-medium text-red-600 hover:text-red-500"
              >
                Esqueceu sua senha?
              </button>
            </div>

            <button
              type="submit"
              className="w-full flex justify-center py-2.5 px-4 border border-transparent rounded-lg shadow-sm text-sm font-semibold text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
            >
              Entrar
            </button>
          </form>

          <div className="mt-6 text-center border-t border-slate-200 pt-4">
            <p className="text-xs text-slate-600">
              Ainda não tem uma conta?{' '}
              <button
                onClick={onNavigateToCadastro}
                className="font-semibold text-red-600 hover:text-red-500"
              >
                Cadastre-se
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};