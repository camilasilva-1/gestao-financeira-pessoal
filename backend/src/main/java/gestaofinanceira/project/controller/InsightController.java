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

    // Retorna todos os insights do usuário
    @GetMapping("/{usuarioId}")
    public List<InsightDTO> listarInsights(@PathVariable Integer usuarioId) {
        return service.gerarInsights(usuarioId);
    }

    // Retorna alertas ativos do usuário
    @GetMapping("/{usuarioId}/alertas")
    public List<InsightDTO> listarAlertas(@PathVariable Integer usuarioId) {
        return service.gerarAlertas(usuarioId);
    }

    // Retorna o score financeiro do usuário
    @GetMapping("/{usuarioId}/score")
    public ScoreFinanceiroDTO obterScore(@PathVariable Integer usuarioId) {
        return service.calcularScore(usuarioId);
    }
}
