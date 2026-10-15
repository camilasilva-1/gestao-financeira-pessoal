package gestaofinanceira.project.dto;

import java.math.BigDecimal;

public class RankingCategoriaDTO {
    private String categoria;
    private BigDecimal valor;
    private double percentual;

    public RankingCategoriaDTO(String categoria, BigDecimal valor, double percentual) {
        this.categoria = categoria;
        this.valor = valor;
        this.percentual = percentual;
    }

    public String getCategoria() { return categoria; }
    public BigDecimal getValor() { return valor; }
    public double getPercentual() { return percentual; }
}
