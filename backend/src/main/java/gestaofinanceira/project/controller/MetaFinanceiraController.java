package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.MetaFinanceiraDTO;
import gestaofinanceira.project.model.MetaFinanceira;
import gestaofinanceira.project.service.MetaFinanceiraService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metas")
public class MetaFinanceiraController {

    private final MetaFinanceiraService service;

    public MetaFinanceiraController(MetaFinanceiraService service) {
        this.service = service;
    }

    @PostMapping("/criar/{usuarioId}")
    public MetaFinanceira criarMeta(@PathVariable Integer usuarioId,
                                    @RequestBody MetaFinanceira meta) {
        return service.criarMeta(usuarioId, meta);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<MetaFinanceira> listarPorUsuario(@PathVariable Integer usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    @GetMapping
    public List<MetaFinanceira> listarTodas() {
        return service.listarTodas();
    }

    @PostMapping("/calcular/{usuarioId}")
    public MetaFinanceiraDTO calcular(@PathVariable Integer usuarioId,
                                      @RequestBody MetaFinanceiraDTO meta) {
        return service.calcularMeta(usuarioId, meta);
    }
}
