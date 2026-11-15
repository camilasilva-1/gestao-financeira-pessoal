
package gestaofinanceira.project.service;

import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Transacao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreFinanceiroEtapa3Test {

    @Mock
    OrcamentoService orcamentoService;

    @Mock
    TransacaoService transacaoService;

    @Mock
    CategoriaService categoriaService;

    @InjectMocks
    InsightService insightService;

    // TESTE 1 - EXCELENTE
    @Test
    void deveClassificarScoreComoExcelente() {

        // PREPARAR
        int usuarioId = 1;

        when(orcamentoService.listarPorUsuario(usuarioId))
            .thenReturn(List.of());

        when(transacaoService.listarPorUsuario(usuarioId))
            .thenReturn(List.of(new Transacao()));

        when(categoriaService.listarTodas())
            .thenReturn(List.of());

        // EXECUTAR
        var resultado = insightService.calcularScore(usuarioId);

        // VERIFICAR
        assertEquals(100, resultado.getScore());
        assertEquals("EXCELENTE", resultado.getNivel());
    }

    // Sem transações e orçamentos, não existe base para uma nota.
    @Test
    void naoDeveAtribuirScoreExcelenteSemDados() {
        int usuarioId = 1;

        when(orcamentoService.listarPorUsuario(usuarioId))
            .thenReturn(List.of());
        when(transacaoService.listarPorUsuario(usuarioId))
            .thenReturn(List.of());

        var resultado = insightService.calcularScore(usuarioId);

        assertEquals(null, resultado.getScore());
        assertEquals("SEM_DADOS", resultado.getNivel());
    }

    // TESTE 2 - BOM
    @Test
    void deveClassificarScoreComoBom() {

        // PREPARAR
        int usuarioId = 1;

        Categoria categoria = new Categoria();
        categoria.setNome("Alimentação");

        Orcamento orcamento = new Orcamento();
        orcamento.setIdOrcamento(1);
        orcamento.setCategoria(categoria);

        when(orcamentoService.listarPorUsuario(usuarioId))
            .thenReturn(List.of(orcamento));

        when(orcamentoService.calcularPercentual(1))
            .thenReturn(new BigDecimal("110"));

        when(categoriaService.listarTodas())
            .thenReturn(List.of());

        // EXECUTAR
        var resultado = insightService.calcularScore(usuarioId);

        // VERIFICAR
        assertEquals(70, resultado.getScore());
        assertEquals("BOM", resultado.getNivel());
    }

    // TESTE 3 - ATENÇÃO
    @Test
    void deveClassificarScoreComoAtencao() {

        // PREPARAR
        int usuarioId = 1;

        List<Orcamento> orcamentos = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {

            Categoria categoria = new Categoria();
            categoria.setNome("Categoria " + i);

            Orcamento orcamento = new Orcamento();
            orcamento.setIdOrcamento(i);
            orcamento.setCategoria(categoria);

            orcamentos.add(orcamento);

            when(orcamentoService.calcularPercentual(i))
                .thenReturn(new BigDecimal("110"));
        }

        when(orcamentoService.listarPorUsuario(usuarioId))
            .thenReturn(orcamentos);

        when(categoriaService.listarTodas())
            .thenReturn(List.of());

        // EXECUTAR
        var resultado = insightService.calcularScore(usuarioId);

        // VERIFICAR
        assertEquals(50, resultado.getScore());
        assertEquals("ATENCAO", resultado.getNivel());
    }

    // TESTE 4 - CRÍTICO
    @Test
    void deveClassificarScoreComoCritico() {

        // PREPARAR
        int usuarioId = 1;
        LocalDate hoje = LocalDate.now();

        List<Orcamento> orcamentos = new ArrayList<>();
        List<Categoria> categorias = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {

            Categoria categoria = new Categoria();
            categoria.setNome("Categoria " + i);

            categorias.add(categoria);

            Orcamento orcamento = new Orcamento();
            orcamento.setIdOrcamento(i);
            orcamento.setCategoria(categoria);

            orcamentos.add(orcamento);

            when(orcamentoService.calcularPercentual(i))
                .thenReturn(new BigDecimal("110"));

            when(transacaoService.calcularGastoPorCategoria(
                usuarioId, categoria.getNome(), hoje))
                .thenReturn(new BigDecimal("200"));

            when(transacaoService.calcularGastoPorCategoria(
                usuarioId, categoria.getNome(), hoje.minusMonths(1)))
                .thenReturn(new BigDecimal("100"));
        }

        when(orcamentoService.listarPorUsuario(usuarioId))
            .thenReturn(orcamentos);

        when(categoriaService.listarTodas())
            .thenReturn(categorias);

        // EXECUTAR
        var resultado = insightService.calcularScore(usuarioId);

        // VERIFICAR
        assertEquals(30, resultado.getScore());
        assertEquals("CRITICO", resultado.getNivel());
    }
}
