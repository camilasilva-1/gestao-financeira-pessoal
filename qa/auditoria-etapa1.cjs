// Auditoria somente de leitura para a Etapa 1.
// Execute com backend (8080) e frontend (5173) ligados.
// Reutiliza a instalacao local do @playwright/test; nao precisa inserir senhas neste arquivo.
const { chromium } = require('@playwright/test');

const SITE = 'http://localhost:5173';
const API = 'http://localhost:8080';
const widths = [
  { nome: 'Celular', width: 375, height: 812 },
  { nome: 'Tablet', width: 768, height: 1024 },
  { nome: 'Computador', width: 1366, height: 768 }
];
const limiteConsultaMs = 2000;
const totalAmostras = 5;

function status(ok) { return ok ? 'APROVADO' : 'FALHOU'; }
function percentil95(valores) {
  const ordenados = [...valores].sort((a,b) => a-b);
  return ordenados[Math.ceil(ordenados.length * .95) - 1];
}

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: widths[2] });
  const page = await context.newPage();
  const falhas = [];
  const resultado = [];
  const errors = [];
  page.on('pageerror', err => errors.push(err.message));

  try {
    await page.goto(SITE, { waitUntil: 'domcontentloaded' });
    console.log('\nNa janela aberta, entre com sua conta normal, se necessario.');
    console.log('Nao escreva sua senha neste terminal. O teste nao cria nem apaga dados.\n');

    await page.waitForFunction(() =>
      !!localStorage.getItem('usuarioId') &&
      document.body.innerText.includes('Histórico de Transações'),
      { timeout: 180000 }
    );

    // Exige que a API esteja acessivel com a sessao real do login.
    const usuarioId = await page.evaluate(() => localStorage.getItem('usuarioId'));
    if (!/^\d+$/.test(usuarioId)) throw new Error('ID de usuario invalido na sessao');

    for (const tela of widths) {
      await page.setViewportSize({ width: tela.width, height: tela.height });
      await page.waitForTimeout(350);
      const info = await page.evaluate(() => ({
        documento: document.documentElement.scrollWidth,
        janela: document.documentElement.clientWidth,
        historico: document.body.innerText.includes('Histórico de Transações'),
        formulario: document.body.innerText.includes('Nova Transação'),
      }));
      const ok = info.documento <= info.janela + 4 && info.historico && info.formulario;
      resultado.push({ teste: 'Layout '+tela.nome, status: status(ok),
        largura: tela.width, overflowPx: Math.max(0, info.documento-info.janela) });
      if (!ok) falhas.push('Layout '+tela.nome);
    }
    await page.setViewportSize({ width: 1366, height: 768 });

    for (const endpoint of [
      { nome: 'Categorias', url: API + '/categorias' },
      { nome: 'Historico autenticado', url: API + '/api/transacoes/usuario/' + usuarioId },
    ]) {
      const tempos = [];
      let codigo;
      for (let i=0; i<totalAmostras; i++) {
        const resposta = await page.evaluate(async url => {
          const inicio = performance.now();
          try {
            const r = await fetch(url, { credentials: 'include', cache: 'no-store' });
            await r.text();
            return { status: r.status, ms: performance.now()-inicio };
          } catch (erro) {
            return { status: 0, ms: performance.now()-inicio };
          }
        }, endpoint.url);
        codigo = resposta.status;
        tempos.push(Number(resposta.ms.toFixed(1)));
      }
      const maior = Math.max(...tempos);
      const ok = codigo===200 && maior<=limiteConsultaMs;
      resultado.push({ teste: 'Consulta '+endpoint.nome, status: status(ok),
        http: codigo, maiorMs: maior, p95Ms: percentil95(tempos), limiteMs: limiteConsultaMs });
      if (!ok) falhas.push('Consulta '+endpoint.nome);
    }

    if (errors.length) {
      resultado.push({teste:'Erros JavaScript na pagina', status:'FALHOU', quantidade:errors.length});
      falhas.push('Erros JavaScript');
    } else {
      resultado.push({teste:'Erros JavaScript na pagina', status:'APROVADO', quantidade:0});
    }

    console.log('\n=== AUDITORIA ETAPA 1 (SEM ALTERAR BANCO) ===');
    console.table(resultado);
    console.log('\nObservacao: isto NAO testa tempo de cadastro (<500ms), pois exigiria gravar dados.');
    console.log('Tambem nao comprova todos os requisitos de acessibilidade ou usabilidade.');
    if (falhas.length) {
      console.error('\nPENDENCIAS:', falhas.join('; '));
      process.exitCode=1;
    } else {
      console.log('\nTestes executados aprovados. Conferir requisitos restantes no Takeoff.');
    }
  } catch (e) {
    console.error('\nAuditoria nao concluida:', e.message);
    process.exitCode=1;
  } finally {
    await browser.close();
  }
})().catch(e=>{console.error(e); process.exitCode=1;});
