package gestaofinanceira.project.controller;

import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.service.TransacaoService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // Listar transações de um usuário específico (chamado pelo api.ts)
    @GetMapping("/usuario/{usuarioId}")
    public List<Transacao> listarPorUsuario(@PathVariable Integer usuarioId) {
        return service.listar().stream()
                .filter(t -> t.getUsuario() != null && usuarioId.equals(t.getUsuario().getIdUsuario()))
                .collect(Collectors.toList());
    }

    // Buscar transação por ID
    @GetMapping("/{id}")
    public Transacao buscar(@PathVariable int id) {
        return service.buscar(id);
    }

    // Adicionar nova transação (aceita o JSON enviado pelo api.ts)
    @PostMapping
    public Transacao adicionar(@RequestBody Map<String, Object> payload) {
        Transacao t = new Transacao();
        t.setDescricao((String) payload.get("descricao"));
        t.setValor(new BigDecimal(payload.get("valor").toString()));
        t.setTipo(Tipo.valueOf(payload.get("tipo").toString().toUpperCase()));

        if (payload.get("dataTransacao") != null) {
            t.setDataTransacao(LocalDate.parse(payload.get("dataTransacao").toString()));
        } else {
            t.setDataTransacao(LocalDate.now());
        }

        if (payload.get("estabelecimento") != null) {
            t.setEstabelecimento(payload.get("estabelecimento").toString());
        }

        // Vincula o Usuário pelo ID
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(Integer.parseInt(payload.get("usuarioId").toString()));
        t.setUsuario(usuario);

        // Vincula a Categoria pelo ID
        Categoria categoria = new Categoria();
        int catId = payload.get("categoriaId") != null
                ? Integer.parseInt(payload.get("categoriaId").toString())
                : 1;
        categoria.setIdCategoria(catId);
        t.setCategoria(categoria);

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