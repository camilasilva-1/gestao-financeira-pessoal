package gestaofinanceira.project.controller;

import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.service.CategoriaService;
import gestaofinanceira.project.service.TransacaoService;
import gestaofinanceira.project.dto.RankingCategoriaDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final TransacaoService transacaoService;

    public CategoriaController(CategoriaService categoriaService, TransacaoService transacaoService) {
        this.categoriaService = categoriaService;
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public List<Categoria> listarCategorias() {
        return categoriaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Categoria buscarPorId(@PathVariable Integer id) {
        return categoriaService.buscarPorId(id);
    }

    @PostMapping
    public Categoria criarCategoria(@RequestBody Categoria categoria) {
        return categoriaService.salvar(categoria);
    }

    @PutMapping("/{id}")
    public Categoria atualizarCategoria(@PathVariable Integer id, @RequestBody Categoria categoria) {
        return categoriaService.atualizar(id, categoria);
    }

    @DeleteMapping("/{id}")
    public void deletarCategoria(@PathVariable Integer id) {
        categoriaService.deletar(id);
    }

    @GetMapping("/sugerir")
    public String sugerirCategoria(@RequestParam String estabelecimento) {
        return categoriaService.sugerirCategoria(estabelecimento);
    }

    // ============================
    // RANKING DE CATEGORIAS
    // ============================
    @GetMapping("/ranking/{usuarioId}")
    public List<RankingCategoriaDTO> rankingCategorias(
            @PathVariable Integer usuarioId,
            @RequestParam int mes) {
        return transacaoService.gerarRankingCategorias(usuarioId, mes);
    }
}
