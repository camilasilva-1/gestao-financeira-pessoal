package gestaofinanceira.project.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    private String senha; // Atributo necessário para login e cadastro

    @Column(name = "salario_liquido")
    private BigDecimal salarioLiquido;
}