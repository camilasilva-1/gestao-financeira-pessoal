package gestaofinanceira.project.service;

import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.OrcamentoRepository;
import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.CategoriaRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoMensalTakeoffTest {
    @Mock OrcamentoRepository orcamentos;
    @Mock TransacaoRepository transacoes;
    @Mock CategoriaRepository categorias;
    @Mock UsuarioRepository usuarios;
    @InjectMocks OrcamentoService service;

    @Test
    void deveSomarApenasDespesasDoMesDoUsuarioECategoria() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(3);
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(2);
        categoria.setNome("Moradia");

        Orcamento orcamento = new Orcamento();
        orcamento.setIdOrcamento(5);
        orcamento.setUsuario(usuario);
        orcamento.setCategoria(categoria);
        orcamento.setLimiteValor(new BigDecimal("1000.00"));

        Transacao despesa = new Transacao();
        despesa.setValor(new BigDecimal("750.00"));
        despesa.setTipo(Tipo.DESPESA);

        when(orcamentos.findById(5)).thenReturn(Optional.of(orcamento));
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());
        when(transacoes.findByCategoriaAndTipoAndDataTransacaoBetweenAndUsuarioIdUsuario(
                eq(categoria), eq(Tipo.DESPESA), eq(inicio), eq(fim), eq(3)))
                .thenReturn(List.of(despesa));

        assertEquals(0, new BigDecimal("750.00").compareTo(service.calcularGasto(5)));
        assertEquals(0, new BigDecimal("250.00").compareTo(service.calcularSaldoRestante(5)));
        assertEquals(0, new BigDecimal("75.00").compareTo(service.calcularPercentual(5)));
        verify(transacoes, org.mockito.Mockito.atLeastOnce())
                .findByCategoriaAndTipoAndDataTransacaoBetweenAndUsuarioIdUsuario(
                        categoria, Tipo.DESPESA, inicio, fim, 3);
    }
}
