package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.*;
import gestaofinanceira.project.service.IntegracaoBancariaSimuladaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integracao-bancaria/simulada")
public class IntegracaoBancariaSimuladaController {
    private final IntegracaoBancariaSimuladaService service;
    public IntegracaoBancariaSimuladaController(IntegracaoBancariaSimuladaService service) {
        this.service = service;
    }
    @GetMapping("/extrato")
    public ExtratoSimuladoDTO extrato(@RequestParam String competencia) {
        return service.extrato(competencia);
    }
    public record ImportacaoRequest(Integer usuarioId, String competencia) {}
    @PostMapping("/importar")
    public ImportacaoSimuladaDTO importar(@RequestBody ImportacaoRequest request) {
        return service.importar(request.usuarioId(), request.competencia());
    }
}
