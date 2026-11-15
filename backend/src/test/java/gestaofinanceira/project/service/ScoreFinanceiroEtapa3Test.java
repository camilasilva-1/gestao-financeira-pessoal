package gestaofinanceira.project.service;

import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Transacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreFinanceiroEtapa3Test {

    @Mock
    OrcamentoService orcamentoService;

    @Mock
    TransacaoService transacaoService;

    @InjectMocks
    InsightService insightService;

    // Cria uma transação simples no mês informado para os testes.
    private Transacao transacao(YearMonth mes, Tipo tipo, String valor, boolean investimento) {
        Transacao transacao = new Transacao();
        transacao.setDataTransacao(mes.atDay(10));
        transacao.setTipo(tipo);
        transacao.setValor(new BigDecimal(valor));
        if (investimento) {
            Categoria categoria = new Categoria();
            categoria.setNome("Investimentos");
            transacao.setCategoria(categoria);
        }
        return transacao;
    }

    private Orcamento orcamento(int id) {
        Orcamento orcamento = new Orcamento();
        orcamento.setIdOrcamento(id);
        return orcamento;
    }

    // Sem transações recentes, não deve gerar uma pontuação fictícia.
    @Test
    void naoDeveCalcularScoreSemDados() {
        when(transacaoService.listarPorUsuario(1)).thenReturn(List.of());
        var resultado = insightService.calcularScore(1);
        assertEquals(null, resultado.getScore());
        assertEquals("SEM_DADOS", resultado.getNivel());
    }

    // Excelente: economiza, respeita orçamento, evolui e investe nos dois meses.
    @Test
    void deveClassificarComoExcelente() {
        YearMonth atual = YearMonth.now();
        when(transacaoService.listarPorUsuario(1)).thenReturn(List.of(
                transacao(atual.minusMonths(1), Tipo.RECEITA, "500", false),
                transacao(atual.minusMonths(1), Tipo.DESPESA, "100", true),
                transacao(atual, Tipo.RECEITA, "2000", false),
                transacao(atual, Tipo.DESPESA, "100", true),
                transacao(atual, Tipo.DESPESA, "100", false)
        ));
        when(orcamentoService.listarPorUsuario(1)).thenReturn(List.of(orcamento(10)));
        when(orcamentoService.calcularPercentual(10)).thenReturn(new BigDecimal("50"));

        var resultado = insightService.calcularScore(1);

        assertEquals(100, resultado.getScore());
        assertEquals("EXCELENTE", resultado.getNivel());
    }

    // Bom: economiza e melhora o saldo, mesmo sem orçamento ou investimento.
    @Test
    void deveClassificarComoBom() {
        YearMonth atual = YearMonth.now();
        when(transacaoService.listarPorUsuario(1)).thenReturn(List.of(
                transacao(atual.minusMonths(1), Tipo.RECEITA, "500", false),
                transacao(atual, Tipo.RECEITA, "1200", false),
                transacao(atual, Tipo.DESPESA, "200", false)
        ));
        when(orcamentoService.listarPorUsuario(1)).thenReturn(List.of());

        var resultado = insightService.calcularScore(1);

        assertEquals(60, resultado.getScore());
        assertEquals("BOM", resultado.getNivel());
    }

    // Atenção: economiza, mas o saldo diminuiu e há orçamento ultrapassado.
    @Test
    void deveClassificarComoAtencao() {
        YearMonth atual = YearMonth.now();
        when(transacaoService.listarPorUsuario(1)).thenReturn(List.of(
                transacao(atual.minusMonths(1), Tipo.RECEITA, "2000", false),
                transacao(atual, Tipo.RECEITA, "1000", false),
                transacao(atual, Tipo.DESPESA, "100", false)
        ));
        when(orcamentoService.listarPorUsuario(1)).thenReturn(List.of(orcamento(11)));
        when(orcamentoService.calcularPercentual(11)).thenReturn(new BigDecimal("110"));

        var resultado = insightService.calcularScore(1);

        assertEquals(40, resultado.getScore());
        assertEquals("ATENCAO", resultado.getNivel());
    }

    // Crítico: gastos acima da receita, saldo em queda e orçamento estourado.
    @Test
    void deveClassificarComoCritico() {
        YearMonth atual = YearMonth.now();
        when(transacaoService.listarPorUsuario(1)).thenReturn(List.of(
                transacao(atual.minusMonths(1), Tipo.RECEITA, "1000", false),
                transacao(atual, Tipo.RECEITA, "200", false),
                transacao(atual, Tipo.DESPESA, "300", false)
        ));
        when(orcamentoService.listarPorUsuario(1)).thenReturn(List.of(orcamento(12)));
        when(orcamentoService.calcularPercentual(12)).thenReturn(new BigDecimal("120"));

        var resultado = insightService.calcularScore(1);

        assertEquals(0, resultado.getScore());
        assertEquals("CRITICO", resultado.getNivel());
    }

    // Cada usuário deve receber um score calculado com suas próprias transações.
    @Test
    void deveUsarDadosSeparadosPorUsuario() {
        YearMonth atual = YearMonth.now();
        when(transacaoService.listarPorUsuario(1)).thenReturn(List.of(
                transacao(atual, Tipo.RECEITA, "1000", false)
        ));
        when(transacaoService.listarPorUsuario(2)).thenReturn(List.of(
                transacao(atual, Tipo.DESPESA, "1500", false)
        ));
        when(orcamentoService.listarPorUsuario(1)).thenReturn(List.of());
        when(orcamentoService.listarPorUsuario(2)).thenReturn(List.of());

        var usuarioUm = insightService.calcularScore(1);
        var usuarioDois = insightService.calcularScore(2);

        assertEquals(40, usuarioUm.getScore());
        assertEquals(0, usuarioDois.getScore());
    }
}
