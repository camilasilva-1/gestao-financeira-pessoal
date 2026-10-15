package gestaofinanceira.project.model;

import gestaofinanceira.project.enums.Tipo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacao")
@Getter
@Setter
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transacao")
    private Integer idTransacao;

    private String descricao;

    private BigDecimal valor;

    @Column(name = "data_transacao")
    private LocalDate dataTransacao;

    @Enumerated(EnumType.STRING)
    private Tipo tipo; // Enum para RECEITA ou DESPESA

    private String estabelecimento;

    private Boolean recorrencia;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
}
