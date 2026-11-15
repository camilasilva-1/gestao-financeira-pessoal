
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

        LocalDate mesAtual = LocalDate.now();
        LocalDate mesAnterior = mesAtual.minusMonths(1);

        // Simular nenhuma outra ocorrência financeira
        when(categoriaService.listarTodas())
                .thenReturn(List.of());

        when(orcamentoService.listarPorUsuario(usuarioId))
                .thenReturn(List.of());

        // Receita anterior: R$ 2.000
        when(transacaoService.calcularReceitaMensal(
                usuarioId, mesAnterior))
                .thenReturn(new BigDecimal("2000"));

        // Receita atual: R$ 1.500
        when(transacaoService.calcularReceitaMensal(
                usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1500"));

        // 2. EXECUTAR
        List<InsightDTO> resultados =
                insightService.gerarInsights(usuarioId);

        // 3. VERIFICAR
        assertEquals(1, resultados.size());

        assertEquals("Queda de renda",
                resultados.get(0).getTipo());

        assertEquals("Atenção",
                resultados.get(0).getNivelAlerta());

        assertTrue(resultados.get(0).getMensagem()
                .contains("25.00%"));
    }

    @Test
    void naoDeveAlertarQuandoRendaNaoDiminui() {
        int usuarioId = 1;
        LocalDate mesAtual = LocalDate.now();

        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(usuarioId)).thenReturn(List.of());
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual.minusMonths(1)))
                .thenReturn(new BigDecimal("2000"));
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("2000"));

        List<InsightDTO> resultados = insightService.gerarInsights(usuarioId);

        assertEquals("Sem dados suficientes", resultados.get(0).getTipo());
    }

    @Test
    void naoDeveAlertarQuandoRendaAnteriorForZero() {
        int usuarioId = 1;
        LocalDate mesAtual = LocalDate.now();

        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(usuarioId)).thenReturn(List.of());
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual.minusMonths(1)))
                .thenReturn(BigDecimal.ZERO);
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1500"));

        List<InsightDTO> resultados = insightService.gerarInsights(usuarioId);

        assertEquals("Sem dados suficientes", resultados.get(0).getTipo());
    }

    @Test
    void deveCalcularQuedaDeDezoitoPorCento() {
        int usuarioId = 1;
        LocalDate mesAtual = LocalDate.now();

        when(categoriaService.listarTodas()).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(usuarioId)).thenReturn(List.of());
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual.minusMonths(1)))
                .thenReturn(new BigDecimal("2000"));
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1640"));

        List<InsightDTO> resultados = insightService.gerarInsights(usuarioId);

        assertEquals("Queda de renda", resultados.get(0).getTipo());
        assertTrue(resultados.get(0).getMensagem().contains("18.00%"));
    }

}
