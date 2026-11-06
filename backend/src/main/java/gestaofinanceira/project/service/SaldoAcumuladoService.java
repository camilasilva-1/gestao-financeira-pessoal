package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.SaldoAcumuladoDTO;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class SaldoAcumuladoService {
    private final TransacaoRepository transacoes;
    private final UsuarioRepository usuarios;
    public SaldoAcumuladoService(TransacaoRepository transacoes, UsuarioRepository usuarios) {
        this.transacoes = transacoes;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public SaldoAcumuladoDTO consultar(Integer usuarioId, String inicio, String fim) {
        YearMonth primeiro = IntegracaoBancariaSimuladaService.validarCompetencia(inicio);
        YearMonth ultimo = IntegracaoBancariaSimuladaService.validarCompetencia(fim);
        if (primeiro.isAfter(ultimo) || ChronoUnit.MONTHS.between(primeiro, ultimo) > 119)
            throw new IllegalArgumentException("Informe um intervalo crescente de até 120 meses");
        if (usuarioId == null || usuarioId <= 0 || !usuarios.existsById(usuarioId))
            throw new IllegalArgumentException("Usuário não encontrado");
        List<Transacao> lista = transacoes
            .findByUsuario_IdUsuarioAndDataTransacaoLessThanEqualOrderByDataTransacaoAsc(usuarioId, ultimo.atEndOfMonth());
        BigDecimal anterior = BigDecimal.ZERO;
        Map<YearMonth, BigDecimal[]> totais = new HashMap<>();
        for (Transacao t : lista) {
            if (t.getDataTransacao() == null || t.getTipo() == null || t.getValor() == null)
                throw new IllegalArgumentException("Há transação sem data, tipo ou valor; corrija antes de calcular o saldo");
            YearMonth mes = YearMonth.from(t.getDataTransacao());
            BigDecimal valor = t.getValor();
            if (mes.isBefore(primeiro)) {
                anterior = t.getTipo() == Tipo.RECEITA ? anterior.add(valor) : anterior.subtract(valor);
            } else {
                BigDecimal[] total = totais.computeIfAbsent(mes, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                int indice = t.getTipo() == Tipo.RECEITA ? 0 : 1;
                total[indice] = total[indice].add(valor);
            }
        }
        List<SaldoAcumuladoDTO.Mes> meses = new ArrayList<>();
        BigDecimal acumulado = anterior;
        for (long i = 0, n = ChronoUnit.MONTHS.between(primeiro, ultimo); i <= n; i++) {
            YearMonth mes = primeiro.plusMonths(i);
            BigDecimal[] total = totais.getOrDefault(mes, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal saldo = total[0].subtract(total[1]);
            acumulado = acumulado.add(saldo);
            meses.add(new SaldoAcumuladoDTO.Mes(mes.toString(), total[0], total[1], saldo, acumulado));
        }
        return new SaldoAcumuladoDTO("Saldo acumulado de receitas menos despesas; não inclui bens, dívidas ou saldo inicial externo",
            usuarioId, anterior, meses);
    }
}
