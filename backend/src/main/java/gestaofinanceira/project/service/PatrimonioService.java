package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.EvolucaoPatrimonialDTO;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PatrimonioService {

    private final TransacaoRepository transacaoRepository;

    public PatrimonioService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    // Evolucao do saldo acumulado das transacoes, nao do patrimonio total.
    public List<EvolucaoPatrimonialDTO> calcularEvolucao(Integer usuarioId) {
        List<Transacao> transacoes = transacaoRepository.findByUsuarioIdUsuario(usuarioId);
        Map<YearMonth, BigDecimal> saldosPorMes = new TreeMap<>();

        for (Transacao transacao : transacoes) {
            YearMonth mes = YearMonth.from(transacao.getDataTransacao());
            BigDecimal valor = transacao.getTipo() == Tipo.RECEITA
                    ? transacao.getValor()
                    : transacao.getValor().negate();
            saldosPorMes.merge(mes, valor, BigDecimal::add);
        }

        List<EvolucaoPatrimonialDTO> evolucao = new ArrayList<>();
        if (saldosPorMes.isEmpty()) {
            return evolucao;
        }

        BigDecimal acumulado = BigDecimal.ZERO;
        YearMonth mes = saldosPorMes.keySet().iterator().next();
        YearMonth ultimoMes = ((TreeMap<YearMonth, BigDecimal>) saldosPorMes).lastKey();

        while (!mes.isAfter(ultimoMes)) {
            BigDecimal saldoMes = saldosPorMes.getOrDefault(mes, BigDecimal.ZERO);
            acumulado = acumulado.add(saldoMes);
            evolucao.add(new EvolucaoPatrimonialDTO(mes.toString(), saldoMes, acumulado));
            mes = mes.plusMonths(1);
        }

        return evolucao;
    }
}
