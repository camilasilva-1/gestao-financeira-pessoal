package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.TransacaoDTO;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.service.TransacaoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@CrossOrigin(origins = "*")
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

    // Rota esperada pelo api.ts para buscar transações do usuário logado
    @GetMapping("/usuario/{usuarioId}")
    public List<Transacao> listarPorUsuario(@PathVariable int usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    // Buscar transação por ID
    @GetMapping("/{id}")
    public Transacao buscar(@PathVariable int id) {
        return service.buscar(id);
    }

    // Adicionar nova transação recebendo o DTO do React
    @PostMapping
    public Transacao adicionar(@RequestBody TransacaoDTO dto) {
        return service.adicionarDTO(dto);
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