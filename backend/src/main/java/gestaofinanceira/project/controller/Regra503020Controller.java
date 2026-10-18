package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.Regra503020DTO;
import gestaofinanceira.project.service.Regra503020Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class Regra503020Controller {

    @Autowired
    private Regra503020Service regraService;

    @GetMapping("/regra-50-30-20/{usuarioId}")
    public ResponseEntity<Regra503020DTO> obterCalculoRegra(@PathVariable Integer usuarioId) {
        Regra503020DTO relatorio = regraService.calcularRegra(usuarioId);
        return ResponseEntity.ok(relatorio);
    }
}