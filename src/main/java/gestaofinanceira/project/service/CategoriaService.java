package gestaofinanceira.project.service;

import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Mapeamento simples para sugestão automática
    private static final Map<String, String> sugestoes = Map.of(
            "McDonald's", "Alimentação",
            "Netflix", "Assinaturas",
            "Posto Shell", "Transporte",
            "Farmácia São João", "Saúde"
    );

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // Inicializa categorias padrão no banco
    @PostConstruct
    public void inicializarCategoriasPadrao() {
        if (categoriaRepository.count() == 0) {
            List<Categoria> categoriasPadrao = List.of(
                criar("Alimentação"),
                criar("Moradia"),
                criar("Transporte"),
                criar("Saúde"),
                criar("Educação"),
                criar("Lazer"),
                criar("Compras"),
                criar("Investimentos"),
                criar("Assinaturas"),
                criar("Impostos"),
                criar("Contas e Serviços"),
                criar("Outros")
            );
            categoriaRepository.saveAll(categoriasPadrao);
            System.out.println("✅ Categorias padrão inseridas!");

        }
    }

    private Categoria criar(String nome) {
        Categoria c = new Categoria();
        c.setNome(nome);
        return c;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria salvar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public Categoria atualizar(Integer id, Categoria categoria) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        existente.setNome(categoria.getNome());
        return categoriaRepository.save(existente);
    }

    public void deletar(Integer id) {
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
