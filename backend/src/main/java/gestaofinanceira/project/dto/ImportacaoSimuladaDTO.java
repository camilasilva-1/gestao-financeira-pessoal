package gestaofinanceira.project.dto;
import java.util.List;
public record ImportacaoSimuladaDTO(String modo, Integer usuarioId, int importadas,
    int ignoradas, List<Integer> idsTransacoes) {}
