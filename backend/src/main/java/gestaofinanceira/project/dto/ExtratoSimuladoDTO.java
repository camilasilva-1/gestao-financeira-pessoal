package gestaofinanceira.project.dto;
import java.util.List;
public record ExtratoSimuladoDTO(String modo, String instituicao, String conta,
    String competencia, List<MovimentacaoSimuladaDTO> movimentacoes) {}
