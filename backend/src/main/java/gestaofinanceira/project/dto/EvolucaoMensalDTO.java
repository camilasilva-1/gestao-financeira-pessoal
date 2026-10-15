package gestaofinanceira.project.dto;

import java.math.BigDecimal;

public class EvolucaoMensalDTO {
    private String mes;
    private BigDecimal receita;
    private BigDecimal despesa;
    private BigDecimal saldo;

    public EvolucaoMensalDTO(String mes, BigDecimal receita, BigDecimal despesa, BigDecimal saldo) {
        this.mes = mes;
        this.receita = receita;
        this.despesa = despesa;
        this.saldo = saldo;
    }

    public String getMes() { return mes; }
    public BigDecimal getReceita() { return receita; }
    public BigDecimal getDespesa() { return despesa; }
    public BigDecimal getSaldo() { return saldo; }
}
