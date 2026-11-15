package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.MetaFinanceiraDTO;
import gestaofinanceira.project.model.MetaFinanceira;
import gestaofinanceira.project.service.MetaFinanceiraService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import gestaofinanceira.project.config.SessionOwnershipFilter;

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

    // Guardar ou resgatar valores.
    @PutMapping("/{metaId}/usuario/{usuarioId}")
    public MetaFinanceira atualizarValorAcumulado(
            @PathVariable Integer metaId,
            @PathVariable Integer usuarioId,
            @RequestBody Map<String, Double> dados) {
        return service.atualizarValorAcumulado(
                usuarioId, metaId, dados.get("valorAcumulado"));
    }

    @DeleteMapping("/{metaId}/usuario/{usuarioId}")
    public ResponseEntity<Void> excluirMeta(
            @PathVariable Integer metaId,
            @PathVariable Integer usuarioId) {
        service.excluirMeta(usuarioId, metaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/calcular/{usuarioId}")
    public MetaFinanceiraDTO calcular(@PathVariable Integer usuarioId,
                                      @RequestBody MetaFinanceiraDTO meta) {
        return service.calcularMeta(usuarioId, meta);
    }
}
