package gestaofinanceira.project.service;

import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    private static final List<String> CATEGORIAS_PADRAO = List.of(
            "Alimentação",
            "Moradia",
            "Transporte",
            "Saúde",
            "Educação",
            "Lazer",
            "Compras",
            "Investimentos",
            "Assinaturas",
            "Impostos",
            "Contas e Serviços",
            "Outros"
    );

    private static final Map<String, String> sugestoes = Map.of(
            "McDonald's", "Alimentação",
            "Netflix", "Assinaturas",
            "Posto Shell", "Transporte",
            "Farmácia São João", "Saúde"
    );

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria salvar(Categoria categoria) {
        if (!CATEGORIAS_PADRAO.contains(categoria.getNome())) {
            throw new IllegalArgumentException("Só são permitidas as 12 categorias padrão");
        }

        if (categoriaRepository.findByNome(categoria.getNome()) != null) {
            throw new IllegalArgumentException("Categoria já cadastrada");
        }

        if (categoriaRepository.count() >= 12) {
            throw new IllegalArgumentException("O sistema permite apenas 12 categorias");
        }

        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Integer id, Categoria categoria) {
        Categoria existente = buscarPorId(id);

        if (CATEGORIAS_PADRAO.contains(existente.getNome())) {
            throw new IllegalArgumentException("Categorias padrão não podem ser alteradas");
        }

        existente.setNome(categoria.getNome());
        return categoriaRepository.save(existente);
    }

    public void deletar(Integer id) {
        Categoria categoria = buscarPorId(id);

        if (CATEGORIAS_PADRAO.contains(categoria.getNome())) {
            throw new IllegalArgumentException("Categorias padrão não podem ser excluídas");
        }

        categoriaRepository.deleteById(id);
    }

    public Categoria buscarPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
    }

    public String sugerirCategoria(String estabelecimento) {
        return sugestoes.entrySet().stream()
                .filter(entry -> estabelecimento.toLowerCase().contains(entry.getKey().toLowerCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Outros");
    }
}
