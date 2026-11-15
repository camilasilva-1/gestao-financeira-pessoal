
package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.InsightDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaEmergenciaIdealTest {

    @Mock
    TransacaoService transacaoService;

    @Mock
    OrcamentoService orcamentoService;

    @Mock
    CategoriaService categoriaService;

    @InjectMocks
    InsightService insightService;

    @Test
    void deveCalcularReservaDeSeisMeses() {

        // 1. PREPARAR
        int usuarioId = 1;
        int mes = LocalDate.now().getMonthValue();

        when(transacaoService.calcularReceitaMensal(usuarioId, mes))
            .thenReturn(new BigDecimal("2000"));

        when(transacaoService.calcularDespesaMensal(usuarioId, mes))
            .thenReturn(new BigDecimal("500"));

        // 2. EXECUTAR
        InsightDTO resultado =
            insightService.gerarRecomendacaoInvestimento(usuarioId);

        // 3. VERIFICAR
        assertTrue(resultado.getMensagem().contains("R$ 3000.00"));
    }


@Test
void deveRecomendarVintePorCentoDaReceita() {

    // 1. PREPARAR
    int usuarioId = 1;
    int mes = LocalDate.now().getMonthValue();

    when(transacaoService.calcularReceitaMensal(usuarioId, mes))
        .thenReturn(new BigDecimal("2000"));

    when(transacaoService.calcularDespesaMensal(usuarioId, mes))
        .thenReturn(new BigDecimal("500"));

    // 2. EXECUTAR
    InsightDTO resultado =
        insightService.gerarRecomendacaoInvestimento(usuarioId);

    // 3. VERIFICAR
    assertTrue(resultado.getMensagem().contains("R$ 400.00 por mês"));
}


@Test
void deveGerarMetasSugeridas() {

    // 1. PREPARAR
    int usuarioId = 1;
    int mes = LocalDate.now().getMonthValue();

    when(transacaoService.calcularReceitaMensal(usuarioId, mes))
        .thenReturn(new BigDecimal("2000"));

    when(transacaoService.calcularDespesaMensal(usuarioId, mes))
        .thenReturn(new BigDecimal("500"));

    // 2. EXECUTAR
    var metas = insightService.gerarMetasSugeridas(usuarioId);

    // 3. VERIFICAR
    assertTrue(metas.size() == 3);

    assertTrue(metas.get(0).getMensagem()
        .contains("R$ 3000.00"));

    assertTrue(metas.get(1).getMensagem()
        .contains("R$ 400.00"));

    assertTrue(metas.get(2).getMensagem()
        .contains("12 meses da renda"));
}

    @Test
    void naoDeveSugerirMetasZeradasSemDadosFinanceiros() {
        int usuarioId = 1;
        int mes = LocalDate.now().getMonthValue();

        when(transacaoService.calcularReceitaMensal(usuarioId, mes))
            .thenReturn(BigDecimal.ZERO);
        when(transacaoService.calcularDespesaMensal(usuarioId, mes))
            .thenReturn(BigDecimal.ZERO);

        var metas = insightService.gerarMetasSugeridas(usuarioId);

        org.junit.jupiter.api.Assertions.assertEquals(1, metas.size());
        org.junit.jupiter.api.Assertions.assertEquals("Dados insuficientes", metas.get(0).getTipo());
        org.junit.jupiter.api.Assertions.assertEquals("Informativo", metas.get(0).getNivelAlerta());
    }

    @Test
    void naoDeveRecomendarInvestimentoSemReceita() {
        int usuarioId = 1;
        int mes = LocalDate.now().getMonthValue();

        when(transacaoService.calcularReceitaMensal(usuarioId, mes))
            .thenReturn(BigDecimal.ZERO);
        when(transacaoService.calcularDespesaMensal(usuarioId, mes))
            .thenReturn(new BigDecimal("500"));

        var metas = insightService.gerarMetasSugeridas(usuarioId);

        org.junit.jupiter.api.Assertions.assertEquals(1, metas.size());
        assertTrue(metas.get(0).getMensagem().contains("R$ 3000.00"));
    }

}
