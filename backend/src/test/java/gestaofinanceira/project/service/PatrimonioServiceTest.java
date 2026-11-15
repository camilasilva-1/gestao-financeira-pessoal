package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.EvolucaoPatrimonialDTO;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Transacao;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatrimonioServiceTest {

    @Mock
    TransacaoRepository transacaoRepository;

    @InjectMocks
    PatrimonioService service;

    private Transacao transacao(String data, Tipo tipo, String valor) {
        Transacao transacao = new Transacao();
        transacao.setDataTransacao(LocalDate.parse(data));
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        return transacao;
    }

    @Test
    void deveCalcularSaldoAcumuladoMesAMes() {
        when(transacaoRepository.findByUsuarioIdUsuario(1)).thenReturn(List.of(
                transacao("2026-01-10", Tipo.RECEITA, "1000"),
                transacao("2026-01-15", Tipo.DESPESA, "500"),
                transacao("2026-02-10", Tipo.RECEITA, "500"),
                transacao("2026-02-15", Tipo.DESPESA, "200"),
                transacao("2026-03-10", Tipo.DESPESA, "200")
        ));

        List<EvolucaoPatrimonialDTO> resultado = service.calcularEvolucao(1);

        assertEquals(3, resultado.size());
        assertEquals("2026-01", resultado.get(0).getMes());
        assertEquals(0, new BigDecimal("500").compareTo(resultado.get(0).getSaldoAcumulado()));
        assertEquals(0, new BigDecimal("300").compareTo(resultado.get(1).getSaldoMes()));
        assertEquals(0, new BigDecimal("800").compareTo(resultado.get(1).getSaldoAcumulado()));
        assertEquals(0, new BigDecimal("600").compareTo(resultado.get(2).getSaldoAcumulado()));
        verify(transacaoRepository).findByUsuarioIdUsuario(1);
    }

    @Test
    void deveManterSaldoNosMesesSemTransacoes() {
        when(transacaoRepository.findByUsuarioIdUsuario(1)).thenReturn(List.of(
                transacao("2025-12-10", Tipo.RECEITA, "500"),
                transacao("2026-02-10", Tipo.DESPESA, "100")
        ));

        List<EvolucaoPatrimonialDTO> resultado = service.calcularEvolucao(1);

        assertEquals(3, resultado.size());
        assertEquals("2026-01", resultado.get(1).getMes());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.get(1).getSaldoMes()));
        assertEquals(0, new BigDecimal("500").compareTo(resultado.get(1).getSaldoAcumulado()));
        assertEquals(0, new BigDecimal("400").compareTo(resultado.get(2).getSaldoAcumulado()));
    }

    @Test
    void deveRetornarListaVaziaSemTransacoes() {
        when(transacaoRepository.findByUsuarioIdUsuario(1)).thenReturn(List.of());

        assertTrue(service.calcularEvolucao(1).isEmpty());
    }
}
