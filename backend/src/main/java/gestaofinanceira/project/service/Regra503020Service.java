package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.Regra503020DTO;
import gestaofinanceira.project.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class Regra503020Service {

    @Autowired
    private TransacaoRepository transacaoRepository;

    public Regra503020DTO calcularRegra(Integer usuarioId) {
        // A regra 50/30/20 acompanha a renda e os gastos do mês atual.
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());
        BigDecimal rendaTotal = transacaoRepository.somaReceitasPorPeriodo(usuarioId.longValue(), inicio, fim);

        // 2. Despesas com Necessidades Básicas (50%)
        BigDecimal realNecessidades = transacaoRepository.somaDespesasPorCategoriasNoPeriodo(
                usuarioId, List.of("Alimentação", "Transporte", "Moradia", "Saúde", "Educação", "Impostos", "Contas e Serviços"), inicio, fim
        );

        // 3. Despesas com Desejos Pessoais (30%)
        BigDecimal realDesejos = transacaoRepository.somaDespesasPorCategoriasNoPeriodo(
                usuarioId, List.of("Lazer", "Outros", "Compras", "Assinaturas"), inicio, fim
        );

        // 4. Saldo disponível no mês, como estimativa do potencial de poupança
        // A sobra considera todas as despesas, não só as duas listas acima.
        BigDecimal totalDespesas = transacaoRepository.somaDespesasPorPeriodo(usuarioId.longValue(), inicio, fim);
        BigDecimal realPoupanca = rendaTotal.subtract(totalDespesas);
        if (realPoupanca.compareTo(BigDecimal.ZERO) < 0) {
            realPoupanca = BigDecimal.ZERO;
        }

        return new Regra503020DTO(rendaTotal, realNecessidades, realDesejos, realPoupanca);
    }
}