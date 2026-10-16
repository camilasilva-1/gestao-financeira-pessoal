package gestaofinanceira.project.service;

import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Lista fixa de categorias padrão
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

    // Sugestões automáticas de categoria com base no estabelecimento
    private static final Map<String, String> sugestoes = Map.of(
            "McDonald's", "Alimentação",
            "Netflix", "Assinaturas",
            "Posto Shell", "Transporte",
            "Farmácia São João", "Saúde"
    );

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Listar todas as categorias
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    // Salvar nova categoria (apenas se for uma das 12 padrão)
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

    // Atualizar categoria (não permite alterar categorias padrão)
    public Categoria atualizar(Integer id, Categoria categoria) {
        Categoria existente = buscarPorId(id);

        if (CATEGORIAS_PADRAO.contains(existente.getNome())) {
            throw new IllegalArgumentException("Categorias padrão não podem ser alteradas");
        }

        existente.setNome(categoria.getNome());
        return categoriaRepository.save(existente);
    }

    // Deletar categoria (não permite excluir categorias padrão)
    public void deletar(Integer id) {
        Categoria categoria = buscarPorId(id);

        if (CATEGORIAS_PADRAO.contains(categoria.getNome())) {
            throw new IllegalArgumentException("Categorias padrão não podem ser excluídas");
        }

        categoriaRepository.deleteById(id);
    }

    // Buscar categoria por ID
    public Categoria buscarPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
    }

    // Sugere categoria com base no nome do estabelecimento
    public String sugerirCategoria(String estabelecimento) {
        return sugestoes.entrySet().stream()
                .filter(entry -> estabelecimento.toLowerCase().contains(entry.getKey().toLowerCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Outros");
    }
}
