package gestaofinanceira.project.model;

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
    private String categoria;
}
