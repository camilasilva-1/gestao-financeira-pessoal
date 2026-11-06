package gestaofinanceira.project.dto;
import java.math.BigDecimal;
import java.util.List;
public record SaldoAcumuladoDTO(String conceito, Integer usuarioId,
    BigDecimal saldoAnterior, List<Mes> meses) {
    public record Mes(String competencia, BigDecimal receitas, BigDecimal despesas,
        BigDecimal saldoMes, BigDecimal saldoAcumulado) {}
}
