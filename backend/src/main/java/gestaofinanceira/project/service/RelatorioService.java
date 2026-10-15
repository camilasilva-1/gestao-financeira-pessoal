package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.*;
import gestaofinanceira.project.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RelatorioService {

    private final TransacaoRepository transacaoRepository;

    public RelatorioService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    // Relatório mensal completo (com intervalo)
    public RelatorioMensalDTO gerarRelatorioMensal(Long usuarioId, LocalDate inicio, LocalDate fim) {
        BigDecimal receitaTotal = transacaoRepository.somaReceitasPorPeriodo(usuarioId, inicio, fim);
        BigDecimal despesaTotal = transacaoRepository.somaDespesasPorPeriodo(usuarioId, inicio, fim);
        BigDecimal saldoFinal = receitaTotal.subtract(despesaTotal);

        List<DistribuicaoGastosDTO> distribuicao = montarDistribuicao(usuarioId, inicio, fim);
        EvolucaoMensalDTO evolucao = montarEvolucao(usuarioId, inicio, fim);

        return new RelatorioMensalDTO(receitaTotal, despesaTotal, saldoFinal, distribuicao, evolucao);
    }

    // Relatório geral (sem intervalo)
    public RelatorioMensalDTO gerarRelatorioGeral(Long usuarioId) {
        BigDecimal receitaTotal = transacaoRepository.somaReceitas(usuarioId);
        BigDecimal despesaTotal = transacaoRepository.somaDespesas(usuarioId);
        BigDecimal saldoFinal = receitaTotal.subtract(despesaTotal);

        List<Object[]> resultados = transacaoRepository.gastosPorCategoriaGeral(usuarioId);
        List<DistribuicaoGastosDTO> distribuicao = new ArrayList<>();

        BigDecimal total = resultados.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Object[] r : resultados) {
            String categoria = (String) r[0];
            BigDecimal valor = (BigDecimal) r[1];
            double percentual = total.compareTo(BigDecimal.ZERO) > 0
                    ? valor.divide(total, 2, RoundingMode.HALF_UP).doubleValue() * 100
                    : 0.0;
            distribuicao.add(new DistribuicaoGastosDTO(categoria, valor, percentual));
        }

        // Evolução pode ser deixada nula ou adaptada depois
        return new RelatorioMensalDTO(receitaTotal, despesaTotal, saldoFinal, distribuicao, null);
    }

    // Distribuição de gastos por categoria (com intervalo)
    public List<DistribuicaoGastosDTO> montarDistribuicao(Long usuarioId, LocalDate inicio, LocalDate fim) {
        List<Object[]> resultados = transacaoRepository.gastosPorCategoria(usuarioId, inicio, fim);
        List<DistribuicaoGastosDTO> distribuicao = new ArrayList<>();

        BigDecimal total = resultados.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Object[] r : resultados) {
            String categoria = (String) r[0];
            BigDecimal valor = (BigDecimal) r[1];
            double percentual = total.compareTo(BigDecimal.ZERO) > 0
                    ? valor.divide(total, 2, RoundingMode.HALF_UP).doubleValue() * 100
                    : 0.0;
            distribuicao.add(new DistribuicaoGastosDTO(categoria, valor, percentual));
        }

        return distribuicao;
    }

    // Evolução mensal (comparativo mês a mês)
    public EvolucaoMensalDTO montarEvolucao(Long usuarioId, LocalDate inicio, LocalDate fim) {
        BigDecimal receita = transacaoRepository.somaReceitasPorPeriodo(usuarioId, inicio, fim);
        BigDecimal despesa = transacaoRepository.somaDespesasPorPeriodo(usuarioId, inicio, fim);
        BigDecimal saldo = receita.subtract(despesa);

        return new EvolucaoMensalDTO(inicio.getMonth().toString(), receita, despesa, saldo);
    }

    // Ranking de categorias (as mais gastas)
    public List<RankingCategoriaDTO> montarRanking(Long usuarioId, LocalDate inicio, LocalDate fim) {
        List<Object[]> resultados = transacaoRepository.gastosPorCategoria(usuarioId, inicio, fim);
        List<RankingCategoriaDTO> ranking = new ArrayList<>();

        BigDecimal total = resultados.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Object[] r : resultados) {
            String categoria = (String) r[0];
            BigDecimal valor = (BigDecimal) r[1];
            double percentual = total.compareTo(BigDecimal.ZERO) > 0
                    ? valor.divide(total, 2, RoundingMode.HALF_UP).doubleValue() * 100
                    : 0.0;
            ranking.add(new RankingCategoriaDTO(categoria, valor, percentual));
        }

        // Ordena do maior para o menor gasto
        ranking.sort((a, b) -> b.getValor().compareTo(a.getValor()));

        return ranking;
    }
}
