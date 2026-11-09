# Etapa 2: nao altera MySQL nem main. Testes de escrita usam mocks ou H2 temporario.
$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
Set-Location $raiz
Write-Host "=== Conferencia GitHub ==="
git pull
if ($LASTEXITCODE -ne 0) { throw "Falha ao atualizar branch; nada mais sera executado" }
Write-Host "=== Testes Java (inclui historico em H2 isolado) ==="
Set-Location (Join-Path $raiz "backend")
.\mvnw.cmd test
if ($LASTEXITCODE -ne 0) { throw "Testes Java falharam: nao prosseguir" }
Write-Host "=== Build do frontend ==="
Set-Location (Join-Path $raiz "frontend")
npm run build
if ($LASTEXITCODE -ne 0) { throw "Build do frontend falhou" }
Set-Location $raiz
Write-Host "=== Testes e build APROVADOS ==="
Write-Host "Para a auditoria de navegador, reinicie o backend atualizado e use:"
Write-Host "node qa/auditoria-etapa2.cjs"
