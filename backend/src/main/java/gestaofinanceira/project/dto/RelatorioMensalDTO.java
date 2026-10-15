package gestaofinanceira.project.dto;

import java.math.BigDecimal;
import java.util.List;

public class RelatorioMensalDTO {
    private BigDecimal receitaTotal;
    private BigDecimal despesaTotal;
    private BigDecimal saldoFinal;
    private List<DistribuicaoGastosDTO> distribuicao;
    private EvolucaoMensalDTO evolucao;

    public RelatorioMensalDTO(BigDecimal receitaTotal, BigDecimal despesaTotal, BigDecimal saldoFinal,
                              List<DistribuicaoGastosDTO> distribuicao, EvolucaoMensalDTO evolucao) {
        this.receitaTotal = receitaTotal;
        this.despesaTotal = despesaTotal;
        this.saldoFinal = saldoFinal;
        this.distribuicao = distribuicao;
        this.evolucao = evolucao;
    }

    public BigDecimal getReceitaTotal() { return receitaTotal; }
    public BigDecimal getDespesaTotal() { return despesaTotal; }
    public BigDecimal getSaldoFinal() { return saldoFinal; }
    public List<DistribuicaoGastosDTO> getDistribuicao() { return distribuicao; }
    public EvolucaoMensalDTO getEvolucao() { return evolucao; }
}
