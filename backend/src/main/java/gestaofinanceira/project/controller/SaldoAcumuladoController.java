package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.SaldoAcumuladoDTO;
import gestaofinanceira.project.service.SaldoAcumuladoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatorios/saldo-acumulado")
public class SaldoAcumuladoController {
    private final SaldoAcumuladoService service;
    public SaldoAcumuladoController(SaldoAcumuladoService service) { this.service = service; }
    @GetMapping
    public SaldoAcumuladoDTO consultar(@RequestParam Integer usuarioId,
            @RequestParam String inicio, @RequestParam String fim) {
        return service.consultar(usuarioId, inicio, fim);
    }
}
