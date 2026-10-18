package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.Regra503020DTO;
import gestaofinanceira.project.repository.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class Regra503020Service {

    @Autowired
    private TransacaoRepository transacaoRepository;

    public Regra503020DTO calcularRegra(Integer usuarioId) {
        // 1. Receitas totais do usuário
        BigDecimal rendaTotal = transacaoRepository.somaReceitasPorUsuarioId(usuarioId);

        // 2. Despesas com Necessidades Básicas (50%)
        BigDecimal realNecessidades = transacaoRepository.somaDespesasPorCategorias(
                usuarioId, List.of("Alimentação", "Transporte", "Moradia", "Saúde")
        );

        // 3. Despesas com Desejos Pessoais (30%)
        BigDecimal realDesejos = transacaoRepository.somaDespesasPorCategorias(
                usuarioId, List.of("Lazer", "Outros", "Viagens", "Vestuário")
        );

        // 4. Poupança (20%) - Valor restante
        BigDecimal totalDespesas = realNecessidades.add(realDesejos);
        BigDecimal realPoupanca = rendaTotal.subtract(totalDespesas);
        if (realPoupanca.compareTo(BigDecimal.ZERO) < 0) {
            realPoupanca = BigDecimal.ZERO;
        }

        return new Regra503020DTO(rendaTotal, realNecessidades, realDesejos, realPoupanca);
    }
}