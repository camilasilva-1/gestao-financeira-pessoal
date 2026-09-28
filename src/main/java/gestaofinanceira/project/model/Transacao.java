package gestaofinanceira.project.model;

import gestaofinanceira.project.enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@AllArgsConstructor
public class Transacao {

    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;

    @Positive(message = "Valor deve ser maior que zero")
    private double valor;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    private String tipo;
    private Categoria categoria;
}
