// Testa as faixas visuais com respostas simuladas, SEM alterar os dados da conta.
const { chromium } = require('@playwright/test');
(async () => {
 const browser = await chromium.launch({headless:false});
 const page = await browser.newPage({viewport:{width:1366,height:850}});
 try {
  await page.goto('http://localhost:5173/',{waitUntil:'domcontentloaded'});
  console.log('Faca login no Chromium. Os quatro orcamentos usados neste teste SAO SIMULADOS e nao sao gravados.');
  await page.waitForFunction(() => /^\d+$/.test(localStorage.getItem('usuarioId')||'')
    && document.body.innerText.includes('Histórico de Transações'),null,{timeout:180000});
  const casos=[
   {id:90001,categoria:'QA 70%',gasto:700,esperado:'Normal (70%)'},
   {id:90002,categoria:'QA 71%',gasto:710,esperado:'Atenção (71%)'},
   {id:90003,categoria:'QA 90%',gasto:900,esperado:'Risco (90%)'},
   {id:90004,categoria:'QA 101%',gasto:1010,esperado:'Estourado (101%)'}
  ];
  await page.route('**/orcamentos/usuario/*',route => route.fulfill({
   status:200,contentType:'application/json',
   body:JSON.stringify(casos.map(c=>({
     idOrcamento:c.id,limiteValor:1000,categoria:{idCategoria:c.id,nome:c.categoria}
   })))
  }));
  await page.route(/\/orcamentos\/9000[1-4]\/gasto$/,route=>{
   const id=Number(route.request().url().match(/\/orcamentos\/(\d+)\/gasto$/)?.[1]);
   const c=casos.find(x=>x.id===id);
   return route.fulfill({status:200,contentType:'application/json',body:JSON.stringify(c?.gasto||0)});
  });
  await page.reload({waitUntil:'domcontentloaded'});
  await page.getByText('QA 101%',{exact:true}).waitFor({timeout:15000});
  const resultados=[];
  for (const c of casos) {
    const linha=page.getByText(c.categoria,{exact:true}).locator('..');
    const texto=await linha.innerText();
    resultados.push({faixa:c.categoria,esperado:c.esperado,resultado:texto.includes(c.esperado)?'APROVADO':'FALHOU',visual:texto.replace(/\s+/g,' ').slice(0,110)});
  }
  console.log('\n=== FAIXAS DE ALERTA VISUAIS (DADOS SIMULADOS) ===');
  console.table(resultados);
  if(resultados.some(x=>x.resultado==='FALHOU'))process.exitCode=1;
  console.log('Nenhum orcamento foi criado ou modificado no banco.');
 }catch(e){console.error('Falha na auditoria visual:',e.message);process.exitCode=1}
 finally {await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
