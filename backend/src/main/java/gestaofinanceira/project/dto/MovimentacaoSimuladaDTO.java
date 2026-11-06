package gestaofinanceira.project.dto;

import gestaofinanceira.project.enums.Tipo;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimentacaoSimuladaDTO(String idExterno, String descricao,
    String estabelecimento, BigDecimal valor, LocalDate dataTransacao,
    Tipo tipo, String categoriaSugerida) {}
