// Auditoria Etapa 2 — leitura apenas. Nao cria/edita/remove usuario, transacao ou orcamento.
const { chromium } = require('@playwright/test');
const SITE = 'http://localhost:5173';
const API = 'http://localhost:8080';
const checks = [];
const add = (nome, passou, detalhe='') => checks.push({ verificacao:nome, status:passou?'APROVADO':'FALHOU', detalhe });
const quase = (a,b) => Number.isFinite(Number(a)) && Number.isFinite(Number(b))
  && Math.abs(Number(a)-Number(b)) < 0.021;
const mes = new Date();
const yyyy = mes.getFullYear(), mm = String(mes.getMonth()+1).padStart(2,'0');
const inicio = yyyy+'-'+mm+'-01';
const fim = yyyy+'-'+mm+'-'+String(new Date(yyyy,mes.getMonth()+1,0).getDate()).padStart(2,'0');
(async()=>{
 const browser=await chromium.launch({headless:false});
 const context=await browser.newContext({viewport:{width:1366,height:768}});
 const page=await context.newPage();
 try{
   await page.goto(SITE,{waitUntil:'domcontentloaded'});
   console.log('Entre no Chromium aberto. Esta auditoria SOMENTE LE dados da sua conta.');
   await page.waitForFunction(()=>/^\d+$/.test(localStorage.getItem('usuarioId')||'')
      && document.body.innerText.includes('Histórico de Transações'),null,{timeout:180000});
   const id=Number(await page.evaluate(()=>localStorage.getItem('usuarioId')));
   const get=async path=>page.evaluate(async url=>{
     try{
       const r=await fetch(url,{credentials:'include',cache:'no-store'});
       const dados=await r.json().catch(()=>null);
       return {status:r.status,dados};
     }catch(e){return {status:0,erro:String(e)}}
   },API+path);
   const [usuario,historico,orcamentos,relatorio,ranking,regra,alertas]=await Promise.all([
     get('/api/usuarios/'+id),
     get('/api/transacoes/usuario/'+id),
     get('/orcamentos/usuario/'+id),
     get('/api/relatorios/mensal?usuarioId='+id+'&inicio='+inicio+'&fim='+fim),
     get('/api/relatorios/ranking?usuarioId='+id+'&inicio='+inicio+'&fim='+fim),
     get('/api/relatorios/regra-50-30-20/'+id),
     get('/insights/'+id+'/alertas')
   ]);
   for(const [nome,r] of [['Usuario',usuario],['Historico',historico],['Orcamentos',orcamentos],['Relatorio',relatorio],['Ranking',ranking],['Regra 50/30/20',regra],['Alertas',alertas]]){
     add('Endpoint '+nome,r.status===200,'HTTP '+r.status);
   }
   if(checks.some(x=>x.status==='FALHOU')) throw new Error('Um ou mais endpoints estao indisponiveis. Confira se reiniciou o backend apos git pull.');
   const trs=historico.dados.filter(t=>t.dataTransacao>=inicio&&t.dataTransacao<=fim);
   const receitas=trs.filter(t=>t.tipo==='RECEITA').reduce((n,t)=>n+Number(t.valor),0);
   const despesas=trs.filter(t=>t.tipo==='DESPESA').reduce((n,t)=>n+Number(t.valor),0);
   add('Relatorio: receitas mensais',quase(relatorio.dados.receitaTotal,receitas));
   add('Relatorio: despesas mensais',quase(relatorio.dados.despesaTotal,despesas));
   add('Relatorio: saldo mensal',quase(relatorio.dados.saldoFinal,receitas-despesas));
   const rendaBase = receitas > 0 ? receitas : Math.max(0, Number(usuario.dados?.salarioLiquido??0));
   add('Regra: renda mensal',quase(regra.dados.rendaTotal,rendaBase),
      'Receitas: '+receitas+'; salario: '+Number(usuario.dados?.salarioLiquido??0));
   add('Regra: ideal 50%',quase(regra.dados.idealNecessidades,rendaBase*.5));
   add('Regra: ideal 30%',quase(regra.dados.idealDesejos,rendaBase*.3));
   add('Regra: ideal 20%',quase(regra.dados.idealPoupanca,rendaBase*.2));
   add('Regra: saldo poupavel',quase(regra.dados.realPoupanca,Math.max(0,rendaBase-despesas)));

   const grupo={};
   for(const t of trs.filter(t=>t.tipo==='DESPESA')){
     const nome=t.categoria?.nome||'Sem categoria';
     grupo[nome]=(grupo[nome]||0)+Number(t.valor);
   }
   const rank=ranking.dados;
   add('Ranking: ordem decrescente',Array.isArray(rank)&&rank.every((r,i)=>i===0||Number(rank[i-1].valor)>=Number(r.valor)));
   add('Ranking: totais e percentuais',Array.isArray(rank)
     && rank.length===Object.keys(grupo).length
     && rank.every(r=>quase(r.valor,grupo[r.categoria])&&Math.abs(Number(r.percentual)-(despesas>0?100*grupo[r.categoria]/despesas:0))<.06));

   const salario=Number(usuario.dados?.salarioLiquido??0);
   const salarioSalvo=Number(await page.evaluate(()=>localStorage.getItem('salarioLiquido')||'0'));
   add('Salario: sessao corresponde ao backend',quase(salario,salarioSalvo),'R$ '+salario.toFixed(2));

   if(orcamentos.dados.length===0){
     console.log('SEM ORCAMENTOS: calculos e alertas de orcamento nao podem ser comprovados sem registros de teste.');
   }
   for(const o of orcamentos.dados){
     const oid=o.idOrcamento;
     const [gasto,saldo,percentual]=await Promise.all([
       get('/orcamentos/'+oid+'/gasto'),
       get('/orcamentos/'+oid+'/saldo'),
       get('/orcamentos/'+oid+'/percentual')
     ]);
     const total=trs.filter(t=>t.tipo==='DESPESA'&&t.categoria?.idCategoria===o.categoria.idCategoria)
       .reduce((n,t)=>n+Number(t.valor),0);
     const lim=Number(o.limiteValor), pct=lim?100*total/lim:0;
     add('Orcamento '+oid+': gasto mensal',gasto.status===200&&quase(gasto.dados,total));
     add('Orcamento '+oid+': saldo restante',saldo.status===200&&quase(saldo.dados,lim-total));
     add('Orcamento '+oid+': percentual',percentual.status===200&&Math.abs(Number(percentual.dados)-pct)<.51,
       'API: '+percentual.dados+'%; esperado: '+pct.toFixed(2)+'%');
     const alerta=alertas.dados.find(a=>String(a.mensagem||'').includes(o.categoria.nome));
     const nivel=pct>100?'Estourado':pct>=90?'Risco':pct>70?'Atenção':'Normal';
     add('Orcamento '+oid+': alerta',!!alerta && alerta.nivelAlerta===nivel,
       'Recebido: '+(alerta?.nivelAlerta||'sem alerta')+'; esperado: '+nivel);
   }
   // RN009: verifica o endpoint e a secao da interface, SEM clicar em Salvar.
   const historicoRelatorios = await get('/api/relatorios/historico?usuarioId='+id);
   const historicoOutro = await get('/api/relatorios/historico?usuarioId='+(id+1));
   add('RN009: historico isolado por usuario',historicoOutro.status===403,
     'HTTP '+historicoOutro.status);
   const inicioTempo = Date.now();
   const tempoRelatorio = await get('/api/relatorios/mensal?usuarioId='+id+'&inicio='+inicio+'&fim='+fim);
   const demora = Date.now() - inicioTempo;
   add('RNF020: geracao mensal em ate 10 segundos',
     tempoRelatorio.status===200 && demora<10000, demora+' ms');

   add('RN009: consulta historico mensal', historicoRelatorios.status===200
      && Array.isArray(historicoRelatorios.dados),'HTTP '+historicoRelatorios.status);
   add('RN009: historico na interface',
      await page.getByRole('heading',{name:'Histórico de Relatórios Mensais'}).count()>0
      && await page.getByRole('button',{name:'Salvar relatório deste mês'}).count()>0);
   add('50/30/20: recomendacao visivel',
      await page.getByRole('heading',{name:'Planejamento 50/30/20'}).count()>0
      && await page.getByText('50% Necessidades').count()>0
      && await page.getByText('30% Desejos').count()>0
      && await page.getByText('20% Reserva e investimentos').count()>0);

   // Faixas visuais simuladas: o navegador recebe mock GET, sem escrever no MySQL.
   const casos=[
     {id:90001,nome:'QA 70%',gasto:700,esperado:'Normal (70%)'},
     {id:90002,nome:'QA 71%',gasto:710,esperado:'Atenção (71%)'},
     {id:90003,nome:'QA 90%',gasto:900,esperado:'Risco (90%)'},
     {id:90004,nome:'QA 101%',gasto:1010,esperado:'Estourado (101%)'}
   ];
   await page.route('**/orcamentos/usuario/*',route => route.fulfill({
     status:200,contentType:'application/json',
     body:JSON.stringify(casos.map(c=>({
       idOrcamento:c.id,limiteValor:1000,categoria:{idCategoria:c.id,nome:c.nome}
     })))
   }));
   await page.route(/\/orcamentos\/9000[1-4]\/gasto$/,route=>{
     const oid=Number(route.request().url().match(/\/orcamentos\/(\d+)\/gasto$/)?.[1]);
     const item=casos.find(c=>c.id===oid);
     return route.fulfill({status:200,contentType:'application/json',body:JSON.stringify(item?.gasto??0)});
   });
   await page.reload({waitUntil:'domcontentloaded'});
   await page.getByText('QA 101%',{exact:true}).waitFor({timeout:15000});
   for(const caso of casos){
     const texto=await page.getByText(caso.nome,{exact:true}).locator('..').innerText();
     add('Alerta visual '+caso.nome,texto.includes(caso.esperado),
       'Esperado '+caso.esperado+'; exibido '+texto.replace(/\s+/g,' ').slice(0,95));
   }
   console.log('Faixas visuais simuladas sem alterar o banco.');
   console.log('\n=== AUDITORIA ETAPA 2 (APENAS LEITURA) ===');
   console.table(checks);
   const falhas=checks.filter(x=>x.status==='FALHOU');
   console.log('Aprovados: '+(checks.length-falhas.length)+'/'+checks.length);
   if(falhas.length){console.error('PENDENCIAS:',falhas.map(x=>x.verificacao).join('; '));process.exitCode=1}
   else console.log('Cenarios executados aprovados. Validar ainda casos de limite em testes unitarios.');
 }catch(e){console.error('Auditoria nao concluida:',e.message);process.exitCode=1}
 finally{await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
