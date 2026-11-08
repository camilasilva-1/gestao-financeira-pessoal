
package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.TransacaoDTO;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.service.TransacaoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import gestaofinanceira.project.config.SessionOwnershipFilter;

@RestController
@RequestMapping("/api/transacoes")

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

    // Listar transações do usuário
    @GetMapping("/usuario/{usuarioId}")
    public List<Transacao> listarPorUsuario(@PathVariable int usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    // Filtrar por tipo
    @GetMapping("/filtro/tipo")
    public List<Transacao> filtrarPorTipo(@RequestParam Tipo tipo) {
        return service.filtrarPorTipo(tipo);
    }

    // Filtrar por período
    @GetMapping("/filtro/periodo")
    public List<Transacao> filtrarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.filtrarPorPeriodo(inicio, fim);
    }

    // Filtrar por categoria
    @GetMapping("/filtro/categoria/{idCategoria}")
    public List<Transacao> filtrarPorCategoria(@PathVariable Integer idCategoria) {
        return service.filtrarPorCategoria(idCategoria);
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public Transacao buscar(@PathVariable int id) {
        return service.buscar(id);
    }

    // Adicionar transação recebendo DTO do React
    @PostMapping
    public Transacao adicionar(@RequestBody TransacaoDTO dto, HttpSession session) {
        if (!session.getAttribute(SessionOwnershipFilter.USER_ID).equals(dto.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario nao autorizado");
        }
        return service.adicionarDTO(dto);
    }

    // Editar transação verificando o usuário
    @PutMapping("/{id}/usuario/{usuarioId}")
    public Transacao editar(
            @PathVariable int id,
            @PathVariable int usuarioId,
            @RequestBody Transacao t) {
        return service.editar(id, usuarioId, t);
    }

    // Remover transação verificando o usuário
    @DeleteMapping("/{id}/usuario/{usuarioId}")
    public String remover(
            @PathVariable int id,
            @PathVariable int usuarioId) {
        service.remover(id, usuarioId);
        return "Transação removida com sucesso!";
    }

    // Atualizar categoria verificando o usuário
    @PutMapping("/{id}/categoria/{idCategoria}/usuario/{usuarioId}")
    public Transacao atualizarCategoria(
            @PathVariable int id,
            @PathVariable int idCategoria,
            @PathVariable int usuarioId) {
        return service.atualizarCategoria(id, idCategoria, usuarioId);
    }
}
