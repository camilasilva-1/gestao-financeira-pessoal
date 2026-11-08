package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.InsightDTO;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Usuario;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsightServiceTest {
    @Mock TransacaoService transacaoService;
    @Mock OrcamentoService orcamentoService;
    @Mock CategoriaService categoriaService;
    @InjectMocks InsightService service;

    @Test
    void faixaDeConsumoDeveSinalizarRiscoEEstouro() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");

        Orcamento risco = new Orcamento();
        risco.setIdOrcamento(10);
        risco.setUsuario(usuario);
        risco.setCategoria(categoria);

        Orcamento estourado = new Orcamento();
        estourado.setIdOrcamento(11);
        estourado.setUsuario(usuario);
        estourado.setCategoria(categoria);

        when(orcamentoService.listarPorUsuario(1))
                .thenReturn(List.of(risco, estourado));
        when(orcamentoService.calcularPercentual(10))
                .thenReturn(new BigDecimal("90.00"));
        when(orcamentoService.calcularPercentual(11))
                .thenReturn(new BigDecimal("110.00"));

        List<InsightDTO> alertas = service.gerarAlertas(1);
        assertEquals(2, alertas.size());
        assertEquals("Risco", alertas.get(0).getNivelAlerta());
        assertEquals("Estourado", alertas.get(1).getNivelAlerta());
    }

    @Test
    void aumentoDeGastosDeveUsarMesAnteriorComoBase() {
        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");
        LocalDate agora = LocalDate.now();
        when(categoriaService.listarTodas()).thenReturn(List.of(categoria));
        when(transacaoService.calcularGastoPorCategoria(
                eq(1), eq("Alimentação"), eq(agora)))
                .thenReturn(new BigDecimal("200.00"));
        when(transacaoService.calcularGastoPorCategoria(
                eq(1), eq("Alimentação"), eq(agora.minusMonths(1))))
                .thenReturn(new BigDecimal("100.00"));
        when(orcamentoService.listarPorUsuario(1)).thenReturn(List.of());

        List<InsightDTO> insights = service.gerarInsights(1);
        assertTrue(insights.stream().anyMatch(i -> i.getMensagem().contains("100.00%")));
    }
}
