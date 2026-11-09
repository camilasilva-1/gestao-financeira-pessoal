package gestaofinanceira.project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro imutavel do relatorio gerado pelo usuario.
 * Tabela nova: nao altera a estrutura nem o conteudo das transacoes antigas.
 */
@Entity
@Table(name = "historico_relatorio_mensal")
@Getter
@Setter
public class HistoricoRelatorioMensal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historico")
    private Integer idHistorico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate inicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate fim;

    @Column(name = "gerado_em", nullable = false)
    private LocalDateTime geradoEm;

    @Column(name = "total_receitas", nullable = false, precision = 16, scale = 2)
    private BigDecimal receitaTotal;

    @Column(name = "total_despesas", nullable = false, precision = 16, scale = 2)
    private BigDecimal despesaTotal;

    @Column(name = "saldo_final", nullable = false, precision = 16, scale = 2)
    private BigDecimal saldoFinal;

    @Lob
    @Column(name = "relatorio_json", nullable = false)
    private String relatorioJson;
}
