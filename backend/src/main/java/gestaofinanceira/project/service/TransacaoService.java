package gestaofinanceira.project.service;

import gestaofinanceira.project.exception.TransacaoNotFoundException;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.CategoriaRepository;
import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public TransacaoService(
            TransacaoRepository transacaoRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Transacao> listar() {
        return transacaoRepository.findAll();
    }

    public Transacao buscar(int id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new TransacaoNotFoundException("Transação não encontrada"));
    }

    public Transacao adicionar(Transacao t) {
        validarTransacao(t);
        carregarRelacionamentos(t);
        return transacaoRepository.save(t);
    }

    public Transacao editar(int id, Transacao nova) {
        Transacao existente = buscar(id);
        validarTransacao(nova);
        carregarRelacionamentos(nova);

        existente.setDescricao(nova.getDescricao());
        existente.setValor(nova.getValor());
        existente.setDataTransacao(nova.getDataTransacao());
        existente.setTipo(nova.getTipo());
        existente.setEstabelecimento(nova.getEstabelecimento());
        existente.setRecorrencia(nova.getRecorrencia());
        existente.setUsuario(nova.getUsuario());
        existente.setCategoria(nova.getCategoria());

        return transacaoRepository.save(existente);
    }

    public void remover(int id) {
        Transacao existente = buscar(id);
        transacaoRepository.delete(existente);
    }

    public Transacao atualizarCategoria(int idTransacao, int idCategoria) {
        Transacao transacao = buscar(idTransacao);
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        transacao.setCategoria(categoria);
        return transacaoRepository.save(transacao);
    }

    private void carregarRelacionamentos(Transacao t) {
        Usuario usuario = usuarioRepository.findById(t.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Categoria categoria = categoriaRepository.findById(t.getCategoria().getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        t.setUsuario(usuario);
        t.setCategoria(categoria);
    }

    private void validarTransacao(Transacao t) {
        if (t.getValor() == null || t.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }
        if (t.getUsuario() == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }
        if (t.getCategoria() == null) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
    }
}
