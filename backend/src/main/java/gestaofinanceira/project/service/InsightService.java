
package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.InsightDTO;
import gestaofinanceira.project.dto.ScoreFinanceiroDTO;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.MetaFinanceira;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.repository.MetaFinanceiraRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class InsightService {

    private final TransacaoService transacaoService;
    private final OrcamentoService orcamentoService;
    private final CategoriaService categoriaService;
    private final MetaFinanceiraRepository metaFinanceiraRepository;

    public InsightService(
            TransacaoService transacaoService,
            OrcamentoService orcamentoService,
            CategoriaService categoriaService,
            MetaFinanceiraRepository metaFinanceiraRepository) {

        this.transacaoService = transacaoService;
        this.orcamentoService = orcamentoService;
        this.categoriaService = categoriaService;
        this.metaFinanceiraRepository = metaFinanceiraRepository;
    }

    // ============================================
    // GERAÇÃO DE INSIGHTS
    // ============================================

    public List<InsightDTO> gerarInsights(Integer usuarioId) {

        List<InsightDTO> insights = new ArrayList<>();

        LocalDate mesAtual = LocalDate.now();
        LocalDate mesAnterior = mesAtual.minusMonths(1);

        List<Categoria> categorias = categoriaService.listarTodas();

        boolean gerouInsight = false;

        // 1. DETECTAR AUMENTO DE GASTOS

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
                                gastoAnterior,
                                2,
                                RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                if (variacao.compareTo(BigDecimal.valueOf(30)) > 0) {

                    insights.add(new InsightDTO(
                            "Aumento de gastos",
                            "Seus gastos com "
                                    + c.getNome()
                                    + " aumentaram "
                                    + variacao.setScale(
                                            2, RoundingMode.HALF_UP)
                                    + "% em relação ao mês anterior.",
                            LocalDate.now(),
                            "Atenção"
                    ));

                    gerouInsight = true;
                }
            }
        }

        // 2. DETECTAR ORÇAMENTOS EM RISCO

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
                                + percentual.setScale(
                                        2, RoundingMode.HALF_UP)
                                + "% do orçamento da categoria "
                                + o.getCategoria().getNome(),
                        LocalDate.now(),
                        "Risco"
                ));

                gerouInsight = true;
            }
        }

        // 3. ALERTAR APENAS QUANDO A RENDA CAIR 15% OU MAIS.

        BigDecimal receitaAtual =
        transacaoService.calcularReceitaPorPeriodo(
                usuarioId,
                mesAtual.withDayOfMonth(1),
                mesAtual.withDayOfMonth(mesAtual.lengthOfMonth()));

        BigDecimal receitaAnterior =
        transacaoService.calcularReceitaPorPeriodo(
                usuarioId,
                mesAnterior.withDayOfMonth(1),
                mesAnterior.withDayOfMonth(mesAnterior.lengthOfMonth()));

        if (receitaAnterior != null
                && receitaAtual != null
                && receitaAnterior.compareTo(BigDecimal.ZERO) > 0
                && receitaAtual.compareTo(receitaAnterior) < 0) {

            BigDecimal diferenca = receitaAnterior.subtract(receitaAtual);

            // Compara antes de arredondar para respeitar o limite exato de 15%.
            if (diferenca.multiply(BigDecimal.valueOf(100))
                    .compareTo(receitaAnterior.multiply(BigDecimal.valueOf(15))) >= 0) {

                BigDecimal queda = diferenca
                        .multiply(BigDecimal.valueOf(100))
                        .divide(receitaAnterior, 2, RoundingMode.HALF_UP);

                insights.add(new InsightDTO(
                        "Queda de renda",
                        "Sua receita mensal diminuiu "
                                + queda
                                + "% em relação ao mês anterior.",
                        LocalDate.now(),
                        "Atenção"
                ));

                gerouInsight = true;
            }
        }

        // 4. QUANDO NÃO EXISTEM INSIGHTS

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
    // DETECÇÃO DE QUEDA DE RENDA
    // ============================
    public InsightDTO detectarQuedaRenda(Integer usuarioId) {

        // Identifica o último mês completo
        LocalDate fimUltimoMes = LocalDate.now()
                .withDayOfMonth(1)
                .minusDays(1);

        LocalDate inicioUltimoMes = fimUltimoMes
                .withDayOfMonth(1);

        // Identifica os três meses completos anteriores
        LocalDate fimTresMeses = inicioUltimoMes.minusDays(1);

        LocalDate inicioTresMeses = fimTresMeses
                .minusMonths(2)
                .withDayOfMonth(1);

        // Consulta as receitas dos períodos
        BigDecimal receitaUltimoMes =
                transacaoService.calcularReceitaPorPeriodo(
                        usuarioId,
                        inicioUltimoMes,
                        fimUltimoMes);

        BigDecimal totalTresMeses =
                transacaoService.calcularReceitaPorPeriodo(
                        usuarioId,
                        inicioTresMeses,
                        fimTresMeses);

        // Calcula a média de receita dos três meses
        BigDecimal mediaTresMeses = totalTresMeses
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);

        // Verifica se existem dados suficientes
        if (mediaTresMeses.compareTo(BigDecimal.ZERO) <= 0) {
            return new InsightDTO(
                    "Sem dados suficientes",
                    "Não há receitas registradas nos três meses anteriores para comparar.",
                    LocalDate.now(),
                    "Informativo"
            );
        }

        // Calcula o percentual de queda da receita
        BigDecimal percentualQueda = mediaTresMeses
                .subtract(receitaUltimoMes)
                .multiply(BigDecimal.valueOf(100))
                .divide(mediaTresMeses, 2, RoundingMode.HALF_UP);

        // Identifica queda igual ou superior a 20%
        if (percentualQueda.compareTo(BigDecimal.valueOf(20)) >= 0) {

            return new InsightDTO(
                    "Queda de renda",
                    "Sua receita registrada caiu "
                            + percentualQueda
                            + "% em relação à média dos três meses anteriores.",
                    LocalDate.now(),
                    "Atenção"
            );
        }

        return new InsightDTO(
                "Renda estável",
                "Não foi identificada queda de receita igual ou superior a 20%.",
                LocalDate.now(),
                "Informativo"
        );
    }

    private BigDecimal calcularMediaDespesas(Integer usuarioId) {

    LocalDate fim = LocalDate.now()
            .withDayOfMonth(1)
            .minusDays(1);

    LocalDate inicio = fim
            .withDayOfMonth(1)
            .minusMonths(5);

    BigDecimal totalDespesas =
            transacaoService.calcularDespesaPorPeriodo(
                    usuarioId,
                    inicio,
                    fim);

    return totalDespesas.divide(
            BigDecimal.valueOf(6),
            2,
            RoundingMode.HALF_UP);
}

    private BigDecimal calcularReservaIdeal(Integer usuarioId) {

    BigDecimal mediaDespesas = calcularMediaDespesas(usuarioId);

    return mediaDespesas.multiply(BigDecimal.valueOf(6))
            .setScale(2, RoundingMode.HALF_UP);
}

    // ============================

    // ============================================

    // GERAÇÃO DE ALERTAS
    // ============================================

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

            } else if (percentual.compareTo(
                    BigDecimal.valueOf(90)) >= 0) {

                alertas.add(new InsightDTO(
                        "Risco de orçamento",
                        "Você já utilizou "
                                + percentual.setScale(
                                        2, RoundingMode.HALF_UP)
                                + "% do orçamento da categoria "
                                + o.getCategoria().getNome(),
                        LocalDate.now(),
                        "Risco"
                ));

                gerouAlerta = true;

            } else if (percentual.compareTo(
                    BigDecimal.valueOf(70)) > 0) {

                alertas.add(new InsightDTO(
                        "Orçamento em atenção",
                        "Você já utilizou "
                                + percentual.setScale(
                                        2, RoundingMode.HALF_UP)
                                + "% do orçamento da categoria "
                                + o.getCategoria().getNome(),
                        LocalDate.now(),
                        "Atenção"
                ));

                gerouAlerta = true;

            } else {

                alertas.add(new InsightDTO(
                        "Orçamento saudável",
                        "Você ainda está abaixo do limite do orçamento da categoria "
                                + o.getCategoria().getNome()
                                + ".",
                        LocalDate.now(),
                        "Normal"
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

    // ============================================
    // SCORE FINANCEIRO
    // ============================================

    public ScoreFinanceiroDTO calcularScore(Integer usuarioId) {

        // Calcula o score somente com dados financeiros do próprio usuário.
        List<Transacao> transacoes = transacaoService.listarPorUsuario(usuarioId);
        if (transacoes.isEmpty()) {
            return new ScoreFinanceiroDTO(
                    null, "SEM_DADOS",
                    "Cadastre transações para avaliar sua saúde financeira.");
        }

        YearMonth mesAtual = YearMonth.now();
        YearMonth mesAnterior = mesAtual.minusMonths(1);
        BigDecimal receitas = BigDecimal.ZERO;
        BigDecimal despesas = BigDecimal.ZERO;
        BigDecimal saldoAtual = BigDecimal.ZERO;
        BigDecimal saldoAnterior = BigDecimal.ZERO;
        boolean temAtual = false;
        boolean temAnterior = false;
        boolean investimentoAtual = false;
        boolean investimentoAnterior = false;

        for (Transacao transacao : transacoes) {
            if (transacao.getDataTransacao() == null
                    || transacao.getValor() == null || transacao.getTipo() == null) {
                continue;
            }

            YearMonth mes = YearMonth.from(transacao.getDataTransacao());
            BigDecimal valor = transacao.getValor();
            boolean receita = transacao.getTipo() == Tipo.RECEITA;
            BigDecimal saldo = receita ? valor : valor.negate();

            // Separa receitas, despesas e saldos dos dois meses.
            if (mes.equals(mesAtual)) {
                temAtual = true;
                saldoAtual = saldoAtual.add(saldo);
                if (receita) {
                    receitas = receitas.add(valor);
                } else {
                    despesas = despesas.add(valor);
                }
            } else if (mes.equals(mesAnterior)) {
                temAnterior = true;
                saldoAnterior = saldoAnterior.add(saldo);
            }

            // Investimentos são identificados pela categoria já existente.
            if (!receita && transacao.getCategoria() != null
                    && "Investimentos".equalsIgnoreCase(transacao.getCategoria().getNome())) {
                if (mes.equals(mesAtual)) {
                    investimentoAtual = true;
                } else if (mes.equals(mesAnterior)) {
                    investimentoAnterior = true;
                }
            }
        }

        if (!temAtual && !temAnterior) {
            return new ScoreFinanceiroDTO(
                    null, "SEM_DADOS",
                    "Não há transações recentes para avaliar sua saúde financeira.");
        }

        int score = 0;

        // 1. Economia: guardar 20% ou mais da receita vale 20 pontos.
        if (receitas.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal economia = receitas.subtract(despesas)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(receitas, 2, RoundingMode.HALF_UP);
            if (economia.compareTo(BigDecimal.valueOf(20)) >= 0) {
                score += 20;
            } else if (economia.compareTo(BigDecimal.ZERO) > 0) {
                score += 10;
            }
        }

        // 2. Orçamentos: avalia quantos ultrapassaram o limite.
        List<Orcamento> orcamentos = orcamentoService.listarPorUsuario(usuarioId);
        if (!orcamentos.isEmpty()) {
            int estourados = 0;
            for (Orcamento orcamento : orcamentos) {
                if (orcamentoService.calcularPercentual(orcamento.getIdOrcamento())
                        .compareTo(BigDecimal.valueOf(100)) > 0) {
                    estourados++;
                }
            }
            if (estourados == 0) {
                score += 20;
            } else if (estourados < orcamentos.size()) {
                score += 10;
            }
        }

        // 3. Evolução: compara o saldo do mês atual com o anterior.
        if (temAtual && temAnterior) {
            if (saldoAtual.compareTo(saldoAnterior) > 0) {
                score += 20;
            } else if (saldoAtual.compareTo(saldoAnterior) == 0) {
                score += 10;
            }
        }

        // 4. Constância: observa lançamentos de investimentos nos dois meses.
        if (investimentoAtual && investimentoAnterior) {
            score += 20;
        } else if (investimentoAtual || investimentoAnterior) {
            score += 10;
        }

        // 5. Gastos: não ultrapassar a receita no mês vale 20 pontos.
        if (receitas.compareTo(BigDecimal.ZERO) > 0
                && despesas.compareTo(receitas) <= 0) {
            score += 20;
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

        return new ScoreFinanceiroDTO(score, nivel, descricao);
    }

    // ============================================
    // RECOMENDAÇÃO DE INVESTIMENTO
    // ============================================

    public InsightDTO gerarRecomendacaoInvestimento(
            Integer usuarioId) {

        int mesAtual = LocalDate.now().getMonthValue();

        BigDecimal receita =
                transacaoService.calcularReceitaMensal(
                        usuarioId, mesAtual);

        BigDecimal despesa =
                transacaoService.calcularDespesaMensal(
                        usuarioId, mesAtual);

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
                        + valorRecomendado.setScale(
                                2, RoundingMode.HALF_UP)
                        + " por mês. Sua reserva de emergência ideal é de R$ "
                        + reservaEmergencia.setScale(
                                2, RoundingMode.HALF_UP)
                        + ".";

        return new InsightDTO(
                "Recomendação de Investimento",
                descricao,
                LocalDate.now(),
                "Investimento"
        );
    }

    // ============================================
    // METAS SUGERIDAS DE INVESTIMENTO
    // ============================================

    public List<InsightDTO> gerarMetasSugeridas(
            Integer usuarioId) {

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

        // RN006: não gerar metas de valor zero sem dados que sustentem o cálculo.
        if (receita.compareTo(BigDecimal.ZERO) <= 0
                && despesa.compareTo(BigDecimal.ZERO) <= 0) {
            metas.add(new InsightDTO(
                    "Dados insuficientes",
                    "Cadastre receitas e despesas para receber metas sugeridas.",
                    LocalDate.now(),
                    "Informativo"
            ));
            return metas;
        }

        if (despesa.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal reservaEmergencia =
                    despesa.multiply(BigDecimal.valueOf(6));
            metas.add(new InsightDTO(
                    "Meta Sugerida",
                    "Construir uma reserva de emergência de R$ "
                            + reservaEmergencia.setScale(
                                    2, RoundingMode.HALF_UP),
                    LocalDate.now(),
                    "Meta"
            ));
        }

        // RN007: recomendações de investimento dependem de renda positiva.
        if (receita.compareTo(BigDecimal.ZERO) > 0) {
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
        }

        return metas;
    }

    
    // ============================================
    // ALERTA DE RESERVA INSUFICIENTE
    // ============================================

    public InsightDTO gerarAlertaReservaInsuficiente(Integer usuarioId) {

        // Calcula a reserva ideal com base na média das despesas
        // dos últimos seis meses.
        BigDecimal reservaIdeal = calcularReservaIdeal(usuarioId);

        if (reservaIdeal.compareTo(BigDecimal.ZERO) <= 0) {
            return new InsightDTO(
                    "Dados insuficientes",
                    "Não há despesas registradas nos últimos seis meses para calcular sua reserva de emergência.",
                    LocalDate.now(),
                    "Informativo"
            );
        }

        // Busca as metas financeiras identificadas como reserva.
        List<MetaFinanceira> reservas = metaFinanceiraRepository
                .findByUsuario_IdUsuario(usuarioId).stream()
                .filter(meta -> meta.getNome() != null
                        && meta.getNome().toLowerCase(Locale.ROOT).contains("reserva"))
                .toList();

        if (reservas.isEmpty()) {
            return new InsightDTO(
                    "Dados insuficientes",
                    "Cadastre uma meta de reserva de emergência para acompanhar o valor guardado.",
                    LocalDate.now(),
                    "Informativo"
            );
        }

        // Soma o valor acumulado nas metas de reserva.
        BigDecimal valorGuardado = reservas.stream()
                .map(meta -> BigDecimal.valueOf(
                        meta.getValorAcumulado() == null
                                ? 0.0
                                : meta.getValorAcumulado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Verifica se o valor acumulado está abaixo do recomendado.
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
                "Sua reserva de emergência atingiu o valor recomendado de R$ "
                        + reservaIdeal.setScale(2, RoundingMode.HALF_UP) + ".",
                LocalDate.now(),
                "Informativo"
        );
   }
}
