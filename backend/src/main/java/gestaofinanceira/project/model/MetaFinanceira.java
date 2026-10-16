package gestaofinanceira.project.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class MetaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMeta;

    private String nome;
    private Double valorObjetivo;
    private Double valorAcumulado;
    private LocalDate dataDesejada;
    private Double percentualAtingido;
    private Double valorMensalNecessario;
    private Long diasRestantes;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Integer getIdMeta() { return idMeta; }
    public void setIdMeta(Integer idMeta) { this.idMeta = idMeta; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getValorObjetivo() { return valorObjetivo; }
    public void setValorObjetivo(Double valorObjetivo) { this.valorObjetivo = valorObjetivo; }

    public Double getValorAcumulado() { return valorAcumulado; }
    public void setValorAcumulado(Double valorAcumulado) { this.valorAcumulado = valorAcumulado; }

    public LocalDate getDataDesejada() { return dataDesejada; }
    public void setDataDesejada(LocalDate dataDesejada) { this.dataDesejada = dataDesejada; }

    public Double getPercentualAtingido() { return percentualAtingido; }
    public void setPercentualAtingido(Double percentualAtingido) { this.percentualAtingido = percentualAtingido; }

    public Double getValorMensalNecessario() { return valorMensalNecessario; }
    public void setValorMensalNecessario(Double valorMensalNecessario) { this.valorMensalNecessario = valorMensalNecessario; }

    public Long getDiasRestantes() { return diasRestantes; }
    public void setDiasRestantes(Long diasRestantes) { this.diasRestantes = diasRestantes; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
