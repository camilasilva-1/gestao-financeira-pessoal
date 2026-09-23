package gestaofinanceira.project.service;

import gestaofinanceira.project.enums.Categoria;
import gestaofinanceira.project.model.Transacao;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransacaoService {

    private final List<Transacao> transacoes = new ArrayList<>();

    // Listar todas
    public List<Transacao> listar() {
        return transacoes;
    }

    // Buscar por índice (simulação de ID)
    public Transacao buscar(int id) {
        if (id < 0 || id >= transacoes.size()) {
            throw new IllegalArgumentException("Transação não encontrada");
        }
        return transacoes.get(id);
    }

    // Adicionar nova
    public Transacao adicionar(Transacao t) {
        if (t.getValor() <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        transacoes.add(t);
        return t;
    }

    // Editar existente
    public Transacao editar(int id, Transacao nova) {
        if (id < 0 || id >= transacoes.size()) {
            throw new IllegalArgumentException("Transação não encontrada");
        }
        if (nova.getValor() <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        transacoes.set(id, nova);
        return nova;
    }

    // Remover
    public void remover(int id) {
        if (id < 0 || id >= transacoes.size()) {
            throw new IllegalArgumentException("Transação não encontrada");
        }
        transacoes.remove(id);
    }

    // NOVO: Atualizar categoria
    public Transacao atualizarCategoria(int id, Categoria categoria) {
        Transacao t = buscar(id);
        t.setCategoria(categoria);
        return t;
    }
}
