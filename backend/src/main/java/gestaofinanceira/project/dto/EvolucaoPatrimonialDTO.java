package gestaofinanceira.project.dto;

import java.math.BigDecimal;

public class EvolucaoPatrimonialDTO {

    private String mes;
    private BigDecimal saldoMes;
    private BigDecimal saldoAcumulado;

    public EvolucaoPatrimonialDTO(String mes, BigDecimal saldoMes, BigDecimal saldoAcumulado) {
        this.mes = mes;
        this.saldoMes = saldoMes;
        this.saldoAcumulado = saldoAcumulado;
    }

    public String getMes() {
        return mes;
    }

    public BigDecimal getSaldoMes() {
        return saldoMes;
    }

    public BigDecimal getSaldoAcumulado() {
        return saldoAcumulado;
    }
}
