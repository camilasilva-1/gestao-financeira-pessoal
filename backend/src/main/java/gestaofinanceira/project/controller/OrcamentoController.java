package gestaofinanceira.project.controller;

import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.service.OrcamentoService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import gestaofinanceira.project.config.SessionOwnershipFilter;

@RestController
@RequestMapping("/orcamentos")


public class OrcamentoController {

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    // Listar todos os orçamentos
    @GetMapping
    public List<Orcamento> listar() {
        return service.listar();
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Orcamento> listarPorUsuario(@PathVariable Integer usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    // Buscar orçamento por Id
    @GetMapping("/{id}")
    public Orcamento buscar(@PathVariable int id) {
        return service.buscar(id);
    }

    // Adicionar novo orçamento
    @PostMapping
    public Orcamento adicionar(@RequestBody Orcamento orcamento, HttpSession session) {
        if (orcamento.getUsuario() == null ||
            !session.getAttribute(SessionOwnershipFilter.USER_ID).equals(orcamento.getUsuario().getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario nao autorizado");
        }
        return service.adicionar(orcamento);
    }

    // Atualizar orçamento existente
    @PutMapping("/{id}")
    public Orcamento editar(@PathVariable int id,
            @RequestBody Orcamento orcamento, HttpSession session) {
        if (orcamento.getUsuario() == null ||
            !session.getAttribute(SessionOwnershipFilter.USER_ID).equals(orcamento.getUsuario().getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario nao autorizado");
        }
        return service.editar(id, orcamento);
    }

    // Remover orçamento
    @DeleteMapping("/{id}")
    public String remover(@PathVariable int id) {

        service.remover(id);

        return "Orçamento removido com sucesso!";
    }

    // Cálculo de consumo do orçamento em tempo real
    @GetMapping("/{id}/gasto")
    public BigDecimal calcularGasto(@PathVariable Integer id) {
        return service.calcularGasto(id);
    }

    // Cálculo do saldo restante do orçamento em tempo real
    @GetMapping("/{id}/saldo")
    public BigDecimal calcularSaldoRestante(@PathVariable Integer id) {
        return service.calcularSaldoRestante(id);
    }

    // Cálculo do percentual utilizado do orçamento
    @GetMapping("/{id}/percentual")
    public BigDecimal calcularPercentual(@PathVariable Integer id) {
        return service.calcularPercentual(id);
    }

}
