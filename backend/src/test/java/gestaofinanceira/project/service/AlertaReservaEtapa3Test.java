
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
class AlertaReservaEtapa3Test {

    @Mock
    TransacaoService transacaoService;

    @Mock
    OrcamentoService orcamentoService;

    @Mock
    CategoriaService categoriaService;

    @InjectMocks
    InsightService insightService;

    @Test
    void deveAlertarReservaInsuficiente() {

        // 1. PREPARAR
        int usuarioId = 1;
        int mes = LocalDate.now().getMonthValue();

        when(transacaoService.calcularDespesaMensal(usuarioId, mes))
            .thenReturn(new BigDecimal("500"));

        when(transacaoService.calcularGastoPorCategoria(
            usuarioId, "Investimentos", mes))
            .thenReturn(new BigDecimal("1000"));

        // 2. EXECUTAR
        InsightDTO resultado =
            insightService.gerarAlertaReservaInsuficiente(usuarioId);

        // 3. VERIFICAR
        assertEquals("Reserva insuficiente", resultado.getTipo());

        assertEquals("Atenção", resultado.getNivelAlerta());

        assertTrue(resultado.getMensagem()
            .contains("R$ 3000.00"));
    }
}
