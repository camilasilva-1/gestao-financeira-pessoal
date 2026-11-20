
package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.InsightDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuedaRendaEtapa3Test {

    @Mock
    TransacaoService transacaoService;

    @Mock
    OrcamentoService orcamentoService;

    @Mock
    CategoriaService categoriaService;

    @InjectMocks
    InsightService insightService;

    @Test
    void deveDetectarQuedaDeRenda() {

        // 1. PREPARAR
        int usuarioId = 1;

        // Simular nenhuma outra ocorrência financeira
        when(categoriaService.listarTodas())
                .thenReturn(List.of());

        when(orcamentoService.listarPorUsuario(usuarioId))
                .thenReturn(List.of());

        // Receita do último mês completo: R$ 1.500
        // Total dos três meses anteriores: R$ 6.000
        when(transacaoService.calcularReceitaPorPeriodo(
        eq(usuarioId),
        any(LocalDate.class),
        any(LocalDate.class)))
        .thenReturn(new BigDecimal("1500"))
        .thenReturn(new BigDecimal("2000"));
        // 2. EXECUTAR
        List<InsightDTO> resultados =
                insightService.gerarInsights(usuarioId);

        // 3. VERIFICAR
        assertEquals(1, resultados.size());

        assertEquals("Queda de renda",
                resultados.get(0).getTipo());

        assertEquals("Atenção",
                resultados.get(0).getNivelAlerta());
                
        System.out.println("Mensagem retornada: " + resultados.get(0).getMensagem());
        assertTrue(resultados.get(0).getMensagem()
                .contains("25.00%"));
    }

    @Test
    void naoDeveAlertarQuandoRendaNaoDiminui() {
        int usuarioId = 1;
        int mesAtual = LocalDate.now().getMonthValue();
        int mesAnterior = mesAtual == 1 ? 12 : mesAtual - 1;

        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(usuarioId)).thenReturn(List.of());
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAnterior))
                .thenReturn(new BigDecimal("2000"));
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("2000"));

        List<InsightDTO> resultados = insightService.gerarInsights(usuarioId);

        assertEquals("Sem dados suficientes", resultados.get(0).getTipo());
    }

    @Test
    void naoDeveAlertarQuandoRendaAnteriorForZero() {
        int usuarioId = 1;
        int mesAtual = LocalDate.now().getMonthValue();
        int mesAnterior = mesAtual == 1 ? 12 : mesAtual - 1;

        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(usuarioId)).thenReturn(List.of());
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAnterior))
                .thenReturn(BigDecimal.ZERO);
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1500"));

        List<InsightDTO> resultados = insightService.gerarInsights(usuarioId);

        assertEquals("Sem dados suficientes", resultados.get(0).getTipo());
    }

    @Test
    void deveCalcularQuedaDeDezoitoPorCento() {
        int usuarioId = 1;
        int mesAtual = LocalDate.now().getMonthValue();
        int mesAnterior = mesAtual == 1 ? 12 : mesAtual - 1;

        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(usuarioId)).thenReturn(List.of());
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAnterior))
                .thenReturn(new BigDecimal("2000"));
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1640"));

        List<InsightDTO> resultados = insightService.gerarInsights(usuarioId);

        assertEquals("Queda de renda", resultados.get(0).getTipo());
        assertTrue(resultados.get(0).getMensagem().contains("18.00%"));
    }


    // Confirma que uma queda pequena de 10% não gera alerta.
    @Test
    void naoDeveAlertarQuedaInferiorAQuinzePorCento() {
        int usuarioId = 1;
        int mesAtual = LocalDate.now().getMonthValue();
        int mesAnterior = mesAtual == 1 ? 12 : mesAtual - 1;

        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(usuarioId)).thenReturn(List.of());
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAnterior))
                .thenReturn(new BigDecimal("2000"));
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1800"));

        List<InsightDTO> resultados = insightService.gerarInsights(usuarioId);

        assertTrue(resultados.stream()
                .noneMatch(insight -> "Queda de renda".equals(insight.getTipo())));
    }

    // Confirma que uma queda de exatamente 15% já gera o alerta.
    @Test
    void deveAlertarQuedaDeExatamenteQuinzePorCento() {
        int usuarioId = 1;
        int mesAtual = LocalDate.now().getMonthValue();
        int mesAnterior = mesAtual == 1 ? 12 : mesAtual - 1;

        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(usuarioId)).thenReturn(List.of());
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAnterior))
                .thenReturn(new BigDecimal("2000"));
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1700"));

        List<InsightDTO> resultados = insightService.gerarInsights(usuarioId);

        assertEquals("Queda de renda", resultados.get(0).getTipo());
        assertTrue(resultados.get(0).getMensagem().contains("15.00%"));
    }

}
