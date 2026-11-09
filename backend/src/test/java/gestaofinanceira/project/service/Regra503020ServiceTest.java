package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.Regra503020DTO;
import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import gestaofinanceira.project.model.Usuario;
import java.util.Optional;
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

    @Mock
    private UsuarioRepository usuarioRepository;

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
    @Test
    void deveUsarSalarioQuandoNaoHaReceitasRegistradas() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setSalarioLiquido(new BigDecimal("2000.00"));

        when(transacaoRepository.somaReceitasPorPeriodo(1L, inicio, fim))
                .thenReturn(BigDecimal.ZERO);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.somaDespesasPorCategoriasNoPeriodo(
                eq(1), anyList(), eq(inicio), eq(fim)))
                .thenReturn(BigDecimal.ZERO);
        when(transacaoRepository.somaDespesasPorPeriodo(1L, inicio, fim))
                .thenReturn(BigDecimal.ZERO);

        Regra503020DTO resultado = service.calcularRegra(1);
        assertEquals(0, new BigDecimal("2000.00").compareTo(resultado.getRendaTotal()));
        assertEquals(0, new BigDecimal("1000.00").compareTo(resultado.getIdealNecessidades()));
        assertEquals(0, new BigDecimal("600.00").compareTo(resultado.getIdealDesejos()));
        assertEquals(0, new BigDecimal("400.00").compareTo(resultado.getIdealPoupanca()));
    }

    @Test
    void naoSomaSalarioNovamenteQuandoJaExisteReceitaNoMes() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());
        when(transacaoRepository.somaReceitasPorPeriodo(1L, inicio, fim))
                .thenReturn(new BigDecimal("2500.00"));
        when(transacaoRepository.somaDespesasPorCategoriasNoPeriodo(
                eq(1), anyList(), eq(inicio), eq(fim)))
                .thenReturn(BigDecimal.ZERO);
        when(transacaoRepository.somaDespesasPorPeriodo(1L, inicio, fim))
                .thenReturn(BigDecimal.ZERO);

        Regra503020DTO resultado = service.calcularRegra(1);
        assertEquals(0, new BigDecimal("2500.00").compareTo(resultado.getRendaTotal()));
        org.mockito.Mockito.verifyNoInteractions(usuarioRepository);
    }

}
