package gestaofinanceira.project.controller;

import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.service.TransacaoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    // Listar todas as transações
    @GetMapping
    public List<Transacao> listar() {
        return service.listar();
    }

    // Buscar transação por índice (simulação de ID)
    @GetMapping("/{id}")
    public Transacao buscar(@PathVariable int id) {
        return service.buscar(id);
    }

    // Adicionar nova transação
    @PostMapping
    public Transacao adicionar(@RequestBody Transacao t) {
        return service.adicionar(t);
    }

    // Editar transação existente
    @PutMapping("/{id}")
    public Transacao editar(@PathVariable int id, @RequestBody Transacao t) {
        return service.editar(id, t);
    }

    // Remover transação
    @DeleteMapping("/{id}")
    public String remover(@PathVariable int id) {
        service.remover(id);
        return "Transação removida com sucesso!";
    }
}
