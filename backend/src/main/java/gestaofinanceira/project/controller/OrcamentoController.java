package gestaofinanceira.project.controller;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.service.OrcamentoService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController 
@RequestMapping ("/orcamentos")

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

    // Buscar orçamento por Id
    @GetMapping("/{id}")
    public Orcamento buscar(@PathVariable int id) {
        return service.buscar(id);
    }
   
    // Adicionar novo orçamento
    @PostMapping
    public Orcamento adicionar(@RequestBody Orcamento orcamento) {
        return service.adicionar(orcamento);
    }
    
    // Atualizar orçamento existente
    @PutMapping("/{id}")
    public Orcamento editar(@PathVariable int id,
                        @RequestBody Orcamento orcamento) {

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
