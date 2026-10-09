package gestaofinanceira.project.service;

import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import gestaofinanceira.project.exception.TransacaoNotFoundException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(TransacaoRepository transacaoRepository, CategoriaRepository categoriaRepository) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Listar todas as transações
    public List<Transacao> listar() {
        return transacaoRepository.findAll();
    }

    // Buscar transação por ID
    public Transacao buscar(int id) {
    return transacaoRepository.findById(id)
            .orElseThrow(() -> new TransacaoNotFoundException("Transação não encontrada"));
    }

    // Adicionar nova transação
    public Transacao adicionar(Transacao t) {
        validarTransacao(t);
        return transacaoRepository.save(t);
    }

    // Editar transação existente
    public Transacao editar(int id, Transacao nova) {
        Transacao existente = buscar(id);
        validarTransacao(nova);

        // Atualiza os campos
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

    // Remover transação
    public void remover(int id) {
        Transacao existente = buscar(id);
        transacaoRepository.delete(existente);
    }

    // Atualizar categoria de uma transação
    public Transacao atualizarCategoria(int idTransacao, int idCategoria) {
        Transacao transacao = buscar(idTransacao);
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        transacao.setCategoria(categoria);
        return transacaoRepository.save(transacao);
    }

    // Método auxiliar para validação
    private void validarTransacao(Transacao t) {
        if (t.getValor() == null || t.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor da transação deve ser maior que zero");
        }
        if (t.getUsuario() == null) {
            throw new RuntimeException("Usuário é obrigatório");
        }
    }
}
