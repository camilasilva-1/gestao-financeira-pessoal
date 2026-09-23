package gestaofinanceira.project.model;

import gestaofinanceira.project.enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Transacao {
    private String descricao;
    private double valor;
    private LocalDate data;
    private String tipo; // Receita ou Despesa
    private Categoria categoria; 
}
