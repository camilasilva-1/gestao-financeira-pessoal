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
    <div className="min-h-screen bg-slate-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <h2 className="text-3xl font-extrabold text-red-600">Bradesco Finanças</h2>
        <p className="mt-2 text-sm text-slate-600">Recuperação de Acesso</p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow-sm border border-slate-200 sm:rounded-xl sm:px-10">
          {sucesso ? (
            <div className="text-center space-y-4">
              <div className="text-4xl">📩</div>
              <h3 className="text-lg font-bold text-slate-800">E-mail de recuperação enviado!</h3>
              <p className="text-xs text-slate-600">
                Enviamos as instruções para <span className="font-semibold text-slate-800">{email}</span>. Verifique sua caixa de entrada.
              </p>
              <button
                onClick={onNavigateToLogin}
                className="w-full mt-4 py-2 px-4 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-semibold transition-colors"
              >
                Voltar para o Login
              </button>
            </div>
          ) : (
            <form className="space-y-4" onSubmit={handleSubmit}>
              <p className="text-xs text-slate-600 mb-2">
                Digite o e-mail cadastrado na sua conta para enviarmos o link de redefinição de senha.
              </p>

              <div>
                <label className="block text-sm font-medium text-slate-700">E-mail *</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="seu.email@exemplo.com"
                  className={`mt-1 block w-full rounded-md border ${
                    erro ? 'border-red-500' : 'border-slate-300'
                  } px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-red-600`}
                />
                {erro && <p className="mt-1 text-xs text-red-500">{erro}</p>}
              </div>

              <button
                type="submit"
                className="w-full py-2.5 px-4 rounded-lg text-sm font-semibold text-white bg-red-600 hover:bg-red-700 shadow-sm transition-colors"
              >
                Enviar E-mail de Recuperação
              </button>

              <div className="text-center border-t border-slate-200 pt-4">
                <button
                  type="button"
                  onClick={onNavigateToLogin}
                  className="text-xs font-semibold text-red-600 hover:text-red-500"
                >
                  ← Voltar para o Login
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};