package gestaofinanceira.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gestaofinanceira.project.dto.RelatorioMensalDTO;
import gestaofinanceira.project.model.HistoricoRelatorioMensal;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.HistoricoRelatorioMensalRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class HistoricoRelatorioMensalServiceTest {
    @Mock HistoricoRelatorioMensalRepository historico;
    @Mock UsuarioRepository usuarios;
    @Mock RelatorioService relatorios;
    @Spy ObjectMapper json = new ObjectMapper();
    @InjectMocks HistoricoRelatorioMensalService service;

    @Test
    void salvaSnapshotSemAlterarTransacoes() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(7);
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 30);
        RelatorioMensalDTO relatorio = new RelatorioMensalDTO(
            new BigDecimal("4000.00"), new BigDecimal("1500.00"),
            new BigDecimal("2500.00"), List.of(), null);

        when(usuarios.findById(7)).thenReturn(Optional.of(usuario));
        when(relatorios.gerarRelatorioMensal(7L,inicio,fim)).thenReturn(relatorio);
        when(historico.save(any(HistoricoRelatorioMensal.class)))
          .thenAnswer(call -> call.getArgument(0));

        HistoricoRelatorioMensal salvo = service.salvar(7, inicio, fim);
        assertEquals(7, salvo.getUsuario().getIdUsuario());
        assertEquals(inicio, salvo.getInicio());
        assertEquals(fim, salvo.getFim());
        assertEquals(0, new BigDecimal("2500.00").compareTo(salvo.getSaldoFinal()));
        assertTrue(salvo.getRelatorioJson().contains("4000.00"));
        verify(historico, times(1)).save(any(HistoricoRelatorioMensal.class));
    }

    @Test
    void rejeitaPeriodoParcialOuComMesesDiferentesSemSalvar() {
        assertThrows(IllegalArgumentException.class,
            () -> service.salvar(7, LocalDate.of(2026,11,2),LocalDate.of(2026,11,30)));
        assertThrows(IllegalArgumentException.class,
            () -> service.salvar(7, LocalDate.of(2026,11,1),LocalDate.of(2026,12,31)));
        verifyNoInteractions(historico);
    }

    @Test
    void consultaHistoricoFiltradoPorUsuario() {
        when(historico.findByUsuarioIdUsuarioOrderByGeradoEmDesc(7)).thenReturn(List.of());
        assertTrue(service.listar(7).isEmpty());
        verify(historico).findByUsuarioIdUsuarioOrderByGeradoEmDesc(7);
    }
}
