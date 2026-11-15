package gestaofinanceira.project.service;

import gestaofinanceira.project.model.MetaFinanceira;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.MetaFinanceiraRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetaFinanceiraEtapa3Test {

    @Mock
    private TransacaoService transacaoService;
    @Mock
    private MetaFinanceiraRepository metaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MetaFinanceiraService service;

    private Usuario usuario(Integer id) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        return u;
    }

    private MetaFinanceira meta(Usuario dono) {
        MetaFinanceira m = new MetaFinanceira();
        m.setIdMeta(10);
        m.setUsuario(dono);
        m.setNome("Viagem");
        m.setValorObjetivo(2000.0);
        m.setValorAcumulado(400.0);
        m.setDataDesejada(LocalDate.now().plusMonths(12));
        return m;
    }

    @Test
    void deveCriarMetaComCalculosDoTakeoff() {
        Usuario dono = usuario(1);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(dono));
        when(metaRepository.save(any(MetaFinanceira.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));

        MetaFinanceira salva = service.criarMeta(1, meta(dono));

        assertEquals(dono, salva.getUsuario());
        assertEquals(20.0, salva.getPercentualAtingido());
        assertTrue(salva.getDiasRestantes() > 0);
        assertTrue(salva.getValorMensalNecessario() > 0);
        verify(metaRepository).save(salva);
    }

    @Test
    void deveAtualizarValorGuardadoEPercentual() {
        MetaFinanceira existente = meta(usuario(1));
        when(metaRepository.findById(10)).thenReturn(Optional.of(existente));
        when(metaRepository.save(existente)).thenReturn(existente);

        MetaFinanceira atualizada =
                service.atualizarValorAcumulado(1, 10, 1000.0);

        assertEquals(1000.0, atualizada.getValorAcumulado());
        assertEquals(50.0, atualizada.getPercentualAtingido());
        verify(metaRepository).save(existente);
    }

    @Test
    void deveExcluirMetaDoUsuario() {
        MetaFinanceira existente = meta(usuario(1));
        when(metaRepository.findById(10)).thenReturn(Optional.of(existente));

        service.excluirMeta(1, 10);

        verify(metaRepository).delete(existente);
    }

    @Test
    void naoDeveAlterarMetaDeOutroUsuario() {
        when(metaRepository.findById(10))
                .thenReturn(Optional.of(meta(usuario(2))));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> service.atualizarValorAcumulado(1, 10, 1000.0));

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verify(metaRepository, never()).save(any());
    }
}
