package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.EvolucaoPatrimonialDTO;
import gestaofinanceira.project.service.PatrimonioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/insights")
public class PatrimonioController {

    private final PatrimonioService service;

    public PatrimonioController(PatrimonioService service) {
        this.service = service;
    }

    @GetMapping("/{usuarioId}/evolucao-patrimonial")
    public List<EvolucaoPatrimonialDTO> consultar(@PathVariable Integer usuarioId) {
        return service.calcularEvolucao(usuarioId);
    }
}
