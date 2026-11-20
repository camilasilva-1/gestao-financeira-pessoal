package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.InsightDTO;
import gestaofinanceira.project.model.MetaFinanceira;
import gestaofinanceira.project.repository.MetaFinanceiraRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AlertaReservaEtapa3Test {

        @Mock
        TransacaoService transacaoService;
        @Mock
        OrcamentoService orcamentoService;
        @Mock
        CategoriaService categoriaService;
        @Mock
        MetaFinanceiraRepository metaFinanceiraRepository;
        @InjectMocks
        InsightService insightService;

        private MetaFinanceira reserva(double valor) {
                MetaFinanceira meta = new MetaFinanceira();
                meta.setNome("Reserva de Emergência");
                meta.setValorAcumulado(valor);
                return meta;
        }

        @Test
        void deveAlertarQuandoReservaGuardadaForInsuficiente() {
                int usuarioId = 1;

                when(transacaoService.calcularDespesaPorPeriodo(
                                eq(usuarioId),
                                any(LocalDate.class),
                                any(LocalDate.class)))
                                .thenReturn(new BigDecimal("3000"));

                when(metaFinanceiraRepository.findByUsuario_IdUsuario(usuarioId))
                                .thenReturn(List.of(reserva(1000)));

                InsightDTO resultado = insightService.gerarAlertaReservaInsuficiente(usuarioId);

                assertEquals("Reserva insuficiente", resultado.getTipo());
                assertEquals("Atenção", resultado.getNivelAlerta());
                assertTrue(resultado.getMensagem().contains("R$ 3000.00"));
        }

        @Test
        void deveReconhecerReservaAdequada() {
                int usuarioId = 1;
                when(transacaoService.calcularDespesaPorPeriodo(
                                eq(usuarioId),
                                any(LocalDate.class),
                                any(LocalDate.class)))
                                .thenReturn(new BigDecimal("3000"));
                when(metaFinanceiraRepository.findByUsuario_IdUsuario(usuarioId))
                                .thenReturn(List.of(reserva(3000)));

                InsightDTO resultado = insightService.gerarAlertaReservaInsuficiente(usuarioId);

                assertEquals("Reserva adequada", resultado.getTipo());
        }

        @Test
        void naoDeveInventarReservaQuandoNaoHaMetaCadastrada() {
                int usuarioId = 1;
                when(transacaoService.calcularDespesaPorPeriodo(
                                eq(usuarioId),
                                any(LocalDate.class),
                                any(LocalDate.class)))
                                .thenReturn(new BigDecimal("3000"));
                when(metaFinanceiraRepository.findByUsuario_IdUsuario(usuarioId))
                                .thenReturn(List.of());

                InsightDTO resultado = insightService.gerarAlertaReservaInsuficiente(usuarioId);

                assertEquals("Dados insuficientes", resultado.getTipo());
                assertEquals("Informativo", resultado.getNivelAlerta());
        }
}
