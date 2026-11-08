package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.Regra503020DTO;
import gestaofinanceira.project.repository.TransacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Regra503020ServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private Regra503020Service service;

    @Test
    void deveCalcularLimitesESaldoDoMes() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());

        when(transacaoRepository.somaReceitasPorPeriodo(1L, inicio, fim))
                .thenReturn(new BigDecimal("5000.00"));
        when(transacaoRepository.somaDespesasPorCategoriasNoPeriodo(
                eq(1), anyList(), eq(inicio), eq(fim)))
                .thenReturn(new BigDecimal("1800.00"), new BigDecimal("800.00"));
        when(transacaoRepository.somaDespesasPorPeriodo(1L, inicio, fim))
                .thenReturn(new BigDecimal("3000.00"));

        Regra503020DTO resultado = service.calcularRegra(1);

        assertEquals(0, new BigDecimal("2500.00").compareTo(resultado.getIdealNecessidades()));
        assertEquals(0, new BigDecimal("1500.00").compareTo(resultado.getIdealDesejos()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(resultado.getIdealPoupanca()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(resultado.getRealPoupanca()));
    }
}
