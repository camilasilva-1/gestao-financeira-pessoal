// Auditoria do historico da Etapa 1: apenas consultas GET, sem escrita no banco.
const { chromium } = require('@playwright/test');
const API = 'http://localhost:8080';
const SITE = 'http://localhost:5173';
const resultados = [];

(async () => {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: { width: 1366, height: 768 } });
  const page = await context.newPage();
  const verificar = (nome, ok, detalhe='') => {
    resultados.push({ teste: nome, resultado: ok ? 'APROVADO' : 'FALHOU', detalhe });
  };
  try {
    await page.goto(SITE, { waitUntil: 'domcontentloaded' });
    console.log('Entre na janela do Chromium. Nenhum dado sera criado, editado ou excluido.');
    await page.waitForFunction(() => /^\d+$/.test(localStorage.getItem('usuarioId') || '')
      && document.body.innerText.includes('Histórico de Transações'), null, { timeout: 180000 });
    const id = Number(await page.evaluate(() => localStorage.getItem('usuarioId')));
    const get = async path => page.evaluate(async url => {
      try {
        const r = await fetch(url, { credentials: 'include', cache: 'no-store' });
        const texto = await r.text();
        let dados;
        try { dados = JSON.parse(texto); } catch { dados = null; }
        return { status: r.status, dados };
      } catch (e) { return { status: 0, erro: String(e) }; }
    }, API + path);

    const principal = await get('/api/transacoes/usuario/' + id);
    if (principal.status !== 200 || !Array.isArray(principal.dados)) {
      throw new Error('Historico indisponivel: HTTP ' + principal.status);
    }
    const itens = principal.dados;
    verificar('Historico autenticado', true, itens.length + ' transacoes consultadas');
    verificar('Vinculo com usuario', itens.every(t => t.usuario?.idUsuario === id));
    verificar('Relacionamentos', itens.every(t => t.usuario?.idUsuario === id
      && (t.categoria === null || (Number.isInteger(t.categoria?.idCategoria) && !!t.categoria?.nome))));
    verificar('Categoria obrigatoria nas despesas', itens.every(t => t.tipo !== 'DESPESA' || !!t.categoria?.idCategoria));

    const global = await get('/api/transacoes');
    verificar('Consulta geral nao mistura usuarios', global.status === 200 && Array.isArray(global.dados)
      && global.dados.every(t => t.usuario?.idUsuario === id));

    const outroUsuario = await get('/api/transacoes/usuario/' + (id + 1));
    verificar('Isolamento entre usuarios', outroUsuario.status === 403, 'HTTP ' + outroUsuario.status);

    for (const tipo of ['RECEITA', 'DESPESA']) {
      const resp = await get('/api/transacoes/filtro/tipo?tipo=' + tipo);
      const esperados = itens.filter(t => t.tipo === tipo).map(t => t.idTransacao).sort((a,b)=>a-b);
      const recebidos = Array.isArray(resp.dados)
        ? resp.dados.map(t => t.idTransacao).sort((a,b)=>a-b) : [];
      verificar('Filtro ' + tipo, resp.status === 200 && Array.isArray(resp.dados)
        && JSON.stringify(esperados) === JSON.stringify(recebidos));
    }

    const datas = itens.map(t => t.dataTransacao).filter(Boolean).sort();
    if (datas.length) {
      const inicio = datas[0], fim = datas[datas.length-1];
      const resp = await get('/api/transacoes/filtro/periodo?inicio=' + inicio + '&fim=' + fim);
      const esperado = itens.filter(t=>t.dataTransacao>=inicio && t.dataTransacao<=fim).map(t=>t.idTransacao).sort((a,b)=>a-b);
      const recebido = Array.isArray(resp.dados) ? resp.dados.map(t=>t.idTransacao).sort((a,b)=>a-b) : [];
      verificar('Filtro periodo completo', resp.status === 200 && JSON.stringify(esperado) === JSON.stringify(recebido), inicio+' a '+fim);
    } else verificar('Filtro periodo completo', true, 'Sem transacoes: nao ha datas para confrontar');

    const catId = itens.find(t => t.categoria?.idCategoria)?.categoria.idCategoria;
    if (catId) {
      const resp = await get('/api/transacoes/filtro/categoria/' + catId);
      const esperado = itens.filter(t => t.categoria?.idCategoria === catId).map(t => t.idTransacao).sort((a,b)=>a-b);
      const recebido = Array.isArray(resp.dados) ? resp.dados.map(t => t.idTransacao).sort((a,b)=>a-b) : [];
      verificar('Filtro categoria', resp.status === 200 && JSON.stringify(esperado) === JSON.stringify(recebido));
    } else verificar('Filtro categoria', true, 'Sem transacoes categorizadas para comparar');

    // Ordenacao exibida no frontend: datas ISO em ordem decrescente.
    const datasNaTela = await page.locator('table tbody tr').evaluateAll(rows =>
      rows.map(tr => tr.querySelectorAll('td')[2]?.textContent?.trim()).filter(v => /^\d{4}-\d{2}-\d{2}$/.test(v || '')));
    verificar('Ordenacao por data na tela',
      datasNaTela.every((v,i) => i===0 || datasNaTela[i-1]>=v),
      datasNaTela.length + ' linhas conferidas');

    console.log('\n=== HISTORICO ETAPA 1 — SOMENTE LEITURA ===');
    console.table(resultados);
    const falhas = resultados.filter(t => t.resultado === 'FALHOU');
    console.log(falhas.length ? 'PENDENCIAS: ' + falhas.map(f=>f.teste).join(', ') : 'Todos os cenarios verificados passaram.');
    if (falhas.length) process.exitCode = 1;
  } catch (e) {
    console.error('Teste nao concluido:', e.message);
    process.exitCode = 1;
  } finally { await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
