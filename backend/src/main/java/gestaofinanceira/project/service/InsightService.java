package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.InsightDTO;
import gestaofinanceira.project.dto.ScoreFinanceiroDTO;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Categoria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InsightService {

    private final TransacaoService transacaoService;
    private final OrcamentoService orcamentoService;
    private final CategoriaService categoriaService;

    public InsightService(
            TransacaoService transacaoService,
            OrcamentoService orcamentoService,
            CategoriaService categoriaService) {

        this.transacaoService = transacaoService;
        this.orcamentoService = orcamentoService;
        this.categoriaService = categoriaService;
    }

    // ============================
    // GERAÇÃO DE INSIGHTS
    // ============================
    public List<InsightDTO> gerarInsights(Integer usuarioId) {

        List<InsightDTO> insights = new ArrayList<>();

        int mesAtual = LocalDate.now().getMonthValue();
        int mesAnterior = LocalDate.now().minusMonths(1).getMonthValue();

        List<Categoria> categorias = categoriaService.listarTodas();
        boolean gerouInsight = false;

        for (Categoria c : categorias) {

            BigDecimal gastoAtual =
                    transacaoService.calcularGastoPorCategoria(
                            usuarioId,
                            c.getNome(),
                            mesAtual);

            BigDecimal gastoAnterior =
                    transacaoService.calcularGastoPorCategoria(
                            usuarioId,
                            c.getNome(),
                            mesAnterior);

            if (gastoAnterior.compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal variacao = gastoAtual
                        .subtract(gastoAnterior)
                        .divide(
                                gastoAtual.add(gastoAnterior),
                                2,
                                RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                if (variacao.compareTo(BigDecimal.valueOf(100)) > 0) {
                    variacao = BigDecimal.valueOf(100);
                }

                if (variacao.compareTo(BigDecimal.valueOf(30)) > 0) {

                    insights.add(new InsightDTO(
                            "Aumento de gastos",
                            "Seus gastos com "
                                    + c.getNome()
                                    + " aumentaram "
                                    + variacao.setScale(2, RoundingMode.HALF_UP)
                                    + "% em relação ao mês anterior.",
                            LocalDate.now(),
                            "Atenção"
                    ));

                    gerouInsight = true;
                }
            }
        }

        List<Orcamento> orcamentos =
                orcamentoService.listarPorUsuario(usuarioId);

        for (Orcamento o : orcamentos) {

            BigDecimal percentual =
                    orcamentoService.calcularPercentual(
                            o.getIdOrcamento());

            if (percentual.compareTo(BigDecimal.valueOf(90)) > 0) {

                insights.add(new InsightDTO(
                        "Orçamento",
                        "Você já utilizou "
                                + percentual.setScale(2, RoundingMode.HALF_UP)
                                + "% do orçamento da categoria "
                                + o.getCategoria().getNome(),
                        LocalDate.now(),
                        "Risco"
                ));

                gerouInsight = true;
            }
        }

        if (!gerouInsight) {

            insights.add(new InsightDTO(
                    "Sem dados suficientes",
                    "Ainda não há transações ou gastos relevantes para gerar insights.",
                    LocalDate.now(),
                    "Informativo"
            ));
        }

        return insights;
    }

    // ============================
    // GERAÇÃO DE ALERTAS
    // ============================
    public List<InsightDTO> gerarAlertas(Integer usuarioId) {

        List<InsightDTO> alertas = new ArrayList<>();

        List<Orcamento> orcamentos =
                orcamentoService.listarPorUsuario(usuarioId);

        boolean gerouAlerta = false;

        for (Orcamento o : orcamentos) {

            BigDecimal percentual =
                    orcamentoService.calcularPercentual(
                            o.getIdOrcamento());

            if (percentual.compareTo(BigDecimal.valueOf(100)) > 0) {

                alertas.add(new InsightDTO(
                        "Orçamento estourado",
                        "Seu gasto na categoria "
                                + o.getCategoria().getNome()
                                + " ultrapassou o limite definido.",
                        LocalDate.now(),
                        "Estourado"
                ));

                gerouAlerta = true;

            } else if (percentual.compareTo(BigDecimal.valueOf(90)) >= 0) {

                alertas.add(new InsightDTO(
                        "Risco de orçamento",
                        "Você já utilizou "
                                + percentual.setScale(2, RoundingMode.HALF_UP)
                                + "% do orçamento da categoria "
                                + o.getCategoria().getNome(),
                        LocalDate.now(),
                        "Risco"
                ));

                gerouAlerta = true;

            } else {

                alertas.add(new InsightDTO(
                        "Orçamento saudável",
                        "Você ainda está abaixo do limite do orçamento da categoria "
                                + o.getCategoria().getNome()
                                + ".",
                        LocalDate.now(),
                        "Informativo"
                ));

                gerouAlerta = true;
            }
        }

        if (!gerouAlerta) {

            alertas.add(new InsightDTO(
                    "Sem orçamentos",
                    "Nenhum orçamento foi definido para este usuário.",
                    LocalDate.now(),
                    "Informativo"
            ));
        }

        return alertas;
    }

    // ============================
    // SCORE FINANCEIRO
    // ============================
    public ScoreFinanceiroDTO calcularScore(Integer usuarioId) {

        int score = 0;

        List<Orcamento> orcamentos =
                orcamentoService.listarPorUsuario(usuarioId);

        int estourados = 0;

        for (Orcamento orcamento : orcamentos) {

            BigDecimal percentual =
                    orcamentoService.calcularPercentual(
                            orcamento.getIdOrcamento());

            if (percentual.compareTo(BigDecimal.valueOf(100)) > 0) {
                estourados++;
            }
        }

        if (estourados == 0) {
            score += 40;
        } else if (estourados <= 2) {
            score += 20;
        } else {
            score += 10;
        }

        List<InsightDTO> insights = gerarInsights(usuarioId);

        long aumentos = insights.stream()
                .filter(i -> i.getTipo()
                        .equalsIgnoreCase("Aumento de gastos"))
                .count();

        if (aumentos == 0) {
            score += 30;
        } else if (aumentos <= 2) {
            score += 20;
        } else {
            score += 10;
        }

        List<InsightDTO> alertas = gerarAlertas(usuarioId);

        long riscos = alertas.stream()
                .filter(a ->
                        a.getTipo().equalsIgnoreCase("Risco")
                                || a.getTipo().equalsIgnoreCase("Estourado"))
                .count();

        if (riscos == 0) {
            score += 30;
        } else if (riscos <= 2) {
            score += 20;
        } else {
            score += 10;
        }

        if (score > 100) {
            score = 100;
        }

        String nivel;
        String descricao;

        if (score <= 39) {
            nivel = "CRITICO";
            descricao = "Sua saúde financeira necessita atenção imediata.";
        } else if (score <= 59) {
            nivel = "ATENCAO";
            descricao = "Existem oportunidades de melhoria financeira.";
        } else if (score <= 79) {
            nivel = "BOM";
            descricao = "Você possui um bom controle financeiro.";
        } else {
            nivel = "EXCELENTE";
            descricao = "Sua saúde financeira está excelente.";
        }

        return new ScoreFinanceiroDTO(
                score,
                nivel,
                descricao
        );
    }


        // ============================
    // RECOMENDAÇÃO DE INVESTIMENTO
    // ============================
    public InsightDTO gerarRecomendacaoInvestimento(Integer usuarioId) {

        int mesAtual = LocalDate.now().getMonthValue();

        BigDecimal receita =
                transacaoService.calcularReceitaMensal(usuarioId, mesAtual);

        BigDecimal despesa =
                transacaoService.calcularDespesaMensal(usuarioId, mesAtual);

        if (receita == null) {
            receita = BigDecimal.ZERO;
        }

        if (despesa == null) {
            despesa = BigDecimal.ZERO;
        }

        if (receita.compareTo(BigDecimal.ZERO) <= 0) {
            return new InsightDTO(
                    "Recomendação de Investimento",
                    "Não há receita suficiente para gerar recomendação de investimento.",
                    LocalDate.now(),
                    "Informativo"
            );
        }

        BigDecimal valorRecomendado =
                receita.multiply(BigDecimal.valueOf(0.20));

        BigDecimal reservaEmergencia =
                despesa.multiply(BigDecimal.valueOf(6));

        String descricao =
                "Com sua renda atual, recomenda-se investir pelo menos R$ "
                        + valorRecomendado.setScale(2, RoundingMode.HALF_UP)
                        + " por mês. Sua reserva de emergência ideal é de R$ "
                        + reservaEmergencia.setScale(2, RoundingMode.HALF_UP)
                        + ".";

        return new InsightDTO(
                "Recomendação de Investimento",
                descricao,
                LocalDate.now(),
                "Investimento"
        );
    }

    // ============================
    // METAS SUGERIDAS DE INVESTIMENTO
    // ============================
    public List<InsightDTO> gerarMetasSugeridas(Integer usuarioId) {

        List<InsightDTO> metas = new ArrayList<>();

        int mesAtual = LocalDate.now().getMonthValue();

        BigDecimal receita =
                transacaoService.calcularReceitaMensal(usuarioId, mesAtual);

        BigDecimal despesa =
                transacaoService.calcularDespesaMensal(usuarioId, mesAtual);

        if (receita == null) {
            receita = BigDecimal.ZERO;
        }

        if (despesa == null) {
            despesa = BigDecimal.ZERO;
        }

        BigDecimal reservaEmergencia =
                despesa.multiply(BigDecimal.valueOf(6));

        metas.add(new InsightDTO(
                "Meta Sugerida",
                "Construir uma reserva de emergência de R$ "
                        + reservaEmergencia.setScale(2, RoundingMode.HALF_UP),
                LocalDate.now(),
                "Meta"
        ));

        metas.add(new InsightDTO(
                "Meta Sugerida",
                "Investir mensalmente pelo menos R$ "
                        + receita.multiply(BigDecimal.valueOf(0.20))
                        .setScale(2, RoundingMode.HALF_UP),
                LocalDate.now(),
                "Meta"
        ));

        metas.add(new InsightDTO(
                "Meta Sugerida",
                "Acumular patrimônio equivalente a 12 meses da renda atual.",
                LocalDate.now(),
                "Meta"
        ));

        return metas;
    }


    // ============================
// ALERTA DE RESERVA INSUFICIENTE
// ============================
public InsightDTO gerarAlertaReservaInsuficiente(Integer usuarioId) {

        int mesAtual = LocalDate.now().getMonthValue();

        BigDecimal despesa =
                transacaoService.calcularDespesaMensal(usuarioId, mesAtual);

        if (despesa == null) {
            despesa = BigDecimal.ZERO;
        }

        BigDecimal reservaIdeal =
                despesa.multiply(BigDecimal.valueOf(6));

        BigDecimal valorGuardado =
                transacaoService.calcularGastoPorCategoria(
                        usuarioId,
                        "Investimentos",
                        mesAtual);

        if (valorGuardado == null) {
            valorGuardado = BigDecimal.ZERO;
        }

        if (valorGuardado.compareTo(reservaIdeal) < 0) {
            return new InsightDTO(
                    "Reserva insuficiente",
                    "Sua reserva está abaixo do recomendado. O ideal é possuir pelo menos R$ "
                            + reservaIdeal.setScale(2, RoundingMode.HALF_UP),
                    LocalDate.now(),
                    "Atenção"
            );
        }

        return new InsightDTO(
                "Reserva adequada",
                "Sua reserva de emergência está dentro do valor recomendado.",
                LocalDate.now(),
                "Informativo"
        );
    }

}
