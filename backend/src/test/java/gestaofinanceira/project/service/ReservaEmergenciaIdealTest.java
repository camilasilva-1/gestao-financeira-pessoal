package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.InsightDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaEmergenciaIdealTest {

    @Mock
    private TransacaoService transacaoService;
    @Mock
    private OrcamentoService orcamentoService;
    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private InsightService insightService;

    @Test
    void deveRecomendarReservaEquivalenteASeisMesesDeDespesas() {
        // Preparar: renda mensal de R$ 3.000 e despesas de R$ 1.000.
        int usuarioId = 1;
        int mesAtual = LocalDate.now().getMonthValue();
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoService.calcularDespesaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1000.00"));

        // Executar a regra real do backend.
        InsightDTO recomendacao = insightService.gerarRecomendacaoInvestimento(usuarioId);

        // Verificar: 20% da receita = R$ 600; 6 meses de despesas = R$ 6.000.
        assertEquals("Recomendação de Investimento", recomendacao.getTipo());
        assertTrue(recomendacao.getMensagem().contains("R$ 600.00 por mês"));
        assertTrue(recomendacao.getMensagem().contains("R$ 6000.00"));
    }

    @Test
    void deveInformarFaltaDeReceitaAntesDeRecomendarInvestimento() {
        // Preparar: usuário sem receitas, mas com despesas no mês.
        int usuarioId = 2;
        int mesAtual = LocalDate.now().getMonthValue();
        when(transacaoService.calcularReceitaMensal(usuarioId, mesAtual))
                .thenReturn(BigDecimal.ZERO);
        when(transacaoService.calcularDespesaMensal(usuarioId, mesAtual))
                .thenReturn(new BigDecimal("1000.00"));

        InsightDTO recomendacao = insightService.gerarRecomendacaoInvestimento(usuarioId);

        assertEquals("Informativo", recomendacao.getNivelAlerta());
        assertTrue(recomendacao.getMensagem().contains("Não há receita suficiente"));
    }
}
