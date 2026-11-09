package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.InsightDTO;
import gestaofinanceira.project.dto.ScoreFinanceiroDTO;
import gestaofinanceira.project.service.InsightService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/insights")
public class InsightController {

    private final InsightService service;

    public InsightController(InsightService service) {
        this.service = service;
    }

    @GetMapping("/{usuarioId}")
    public List<InsightDTO> listarInsights(
            @PathVariable Integer usuarioId) {

        return service.gerarInsights(usuarioId);
    }

    @GetMapping("/{usuarioId}/alertas")
    public List<InsightDTO> listarAlertas(
            @PathVariable Integer usuarioId) {

        return service.gerarAlertas(usuarioId);
    }

    @GetMapping("/{usuarioId}/score")
    public ScoreFinanceiroDTO obterScore(
            @PathVariable Integer usuarioId) {

        return service.calcularScore(usuarioId);
    }

    @GetMapping("/{usuarioId}/recomendacao-investimento")
    public InsightDTO recomendacaoInvestimento(
            @PathVariable Integer usuarioId) {

        return service.gerarRecomendacaoInvestimento(usuarioId);
    }

    @GetMapping("/{usuarioId}/metas-sugeridas")
    public List<InsightDTO> metasSugeridas(
            @PathVariable Integer usuarioId) {

        return service.gerarMetasSugeridas(usuarioId);
    }

    @GetMapping("/{usuarioId}/reserva-insuficiente")
    public InsightDTO alertaReservaInsuficiente(
        @PathVariable Integer usuarioId) {

    return service.gerarAlertaReservaInsuficiente(usuarioId);
}



}
