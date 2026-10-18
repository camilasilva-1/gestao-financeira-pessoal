package gestaofinanceira.project.dto;

import java.math.BigDecimal;

public class Regra503020DTO {
    private BigDecimal rendaTotal;
    
    // Valores Ideais (50% Necessidades, 30% Desejos, 20% Poupança)
    private BigDecimal idealNecessidades;
    private BigDecimal idealDesejos;
    private BigDecimal idealPoupanca;
    
    // Valores Reais
    private BigDecimal realNecessidades;
    private BigDecimal realDesejos;
    private BigDecimal realPoupanca;

    public Regra503020DTO(BigDecimal rendaTotal, BigDecimal realNecessidades, BigDecimal realDesejos, BigDecimal realPoupanca) {
        this.rendaTotal = rendaTotal != null ? rendaTotal : BigDecimal.ZERO;
        this.idealNecessidades = this.rendaTotal.multiply(new BigDecimal("0.50"));
        this.idealDesejos = this.rendaTotal.multiply(new BigDecimal("0.30"));
        this.idealPoupanca = this.rendaTotal.multiply(new BigDecimal("0.20"));
        
        this.realNecessidades = realNecessidades != null ? realNecessidades : BigDecimal.ZERO;
        this.realDesejos = realDesejos != null ? realDesejos : BigDecimal.ZERO;
        this.realPoupanca = realPoupanca != null ? realPoupanca : BigDecimal.ZERO;
    }

    public BigDecimal getRendaTotal() { return rendaTotal; }
    public BigDecimal getIdealNecessidades() { return idealNecessidades; }
    public BigDecimal getIdealDesejos() { return idealDesejos; }
    public BigDecimal getIdealPoupanca() { return idealPoupanca; }
    public BigDecimal getRealNecessidades() { return realNecessidades; }
    public BigDecimal getRealDesejos() { return realDesejos; }
    public BigDecimal getRealPoupanca() { return realPoupanca; }
}