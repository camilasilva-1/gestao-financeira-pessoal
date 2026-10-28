package gestaofinanceira.project.controller;


import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.service.TransacaoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import gestaofinanceira.project.enums.Tipo;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

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

    // Filtrar transações por tipo: RECEITA ou DESPESA
    @GetMapping("/filtro/tipo")
    public List<Transacao> filtrarPorTipo(@RequestParam Tipo tipo) {
        return service.filtrarPorTipo(tipo);
    }

    // Filtrar transações por período de datas
    @GetMapping("/filtro/periodo")
    public List<Transacao> filtrarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        return service.filtrarPorPeriodo(inicio, fim);
    }

    // Filtrar transações por categoria
    @GetMapping("/filtro/categoria/{idCategoria}")
    public List<Transacao> filtrarPorCategoria(@PathVariable Integer idCategoria) {
        return service.filtrarPorCategoria(idCategoria);
    }

    // Buscar transação por ID
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

    // Atualizar categoria de uma transação
    @PutMapping("/{id}/categoria/{idCategoria}")
    public Transacao atualizarCategoria(@PathVariable int id, @PathVariable int idCategoria) {
        return service.atualizarCategoria(id, idCategoria);
    }
}
