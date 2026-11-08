package gestaofinanceira.project.service;

import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.OrcamentoRepository;
import gestaofinanceira.project.repository.TransacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {
    @Mock OrcamentoRepository orcamentoRepository;
    @Mock TransacaoRepository transacaoRepository;
    @InjectMocks OrcamentoService service;

    @Test
    void gastoMensalConsideraSomenteUsuarioDoOrcamento() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(2);
        Orcamento orcamento = new Orcamento();
        orcamento.setUsuario(usuario);
        orcamento.setCategoria(categoria);
        orcamento.setLimiteValor(new BigDecimal("500.00"));
        Transacao despesa = new Transacao();
        despesa.setValor(new BigDecimal("120.00"));

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());

        when(orcamentoRepository.findById(9)).thenReturn(Optional.of(orcamento));
        when(transacaoRepository.findByUsuario_IdUsuarioAndCategoriaAndTipoAndDataTransacaoBetween(
                eq(1), eq(categoria), eq(Tipo.DESPESA), eq(inicio), eq(fim)))
                .thenReturn(List.of(despesa));

        assertEquals(new BigDecimal("120.00"), service.calcularGasto(9));
    }
}
