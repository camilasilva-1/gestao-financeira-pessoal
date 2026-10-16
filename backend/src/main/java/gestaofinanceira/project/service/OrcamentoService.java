package gestaofinanceira.project.service;

import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.repository.OrcamentoRepository;
import gestaofinanceira.project.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final TransacaoRepository transacaoRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository, TransacaoRepository transacaoRepository) {
        this.orcamentoRepository = orcamentoRepository;
        this.transacaoRepository = transacaoRepository;
    }

    // Listar todos os orçamentos
    public List<Orcamento> listar() {
        return orcamentoRepository.findAll();
    }

    // Listar orçamentos de um usuário específico
    public List<Orcamento> listarPorUsuario(Integer usuarioId) {
        return orcamentoRepository.findByUsuarioIdUsuario(usuarioId);
    }

    // Buscar orçamento por Id
    public Orcamento buscar(int id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orçamento não encontrado"));
    }

    // Adicionar um novo orçamento
    public Orcamento adicionar(Orcamento orcamento) {
        validarOrcamento(orcamento);
        return orcamentoRepository.save(orcamento);
    }

    // Editar um orçamento existente
    public Orcamento editar(int id, Orcamento novo) {
        Orcamento existente = buscar(id);
        validarOrcamento(novo);

        existente.setLimiteValor(novo.getLimiteValor());
        existente.setUsuario(novo.getUsuario());
        existente.setCategoria(novo.getCategoria());

        return orcamentoRepository.save(existente);
    }

    // Remover orçamento
    public void remover(int id) {
        Orcamento existente = buscar(id);
        orcamentoRepository.delete(existente);
    }

    // Calcula o total gasto da categoria vinculada ao orçamento
    public BigDecimal calcularGasto(Integer idOrcamento) {
        Orcamento orcamento = buscar(idOrcamento);

        List<Transacao> despesas =
                transacaoRepository.findByCategoriaAndTipo(
                        orcamento.getCategoria(),
                        Tipo.DESPESA
                );

        BigDecimal totalGasto = BigDecimal.ZERO;
        for (Transacao transacao : despesas) {
            totalGasto = totalGasto.add(transacao.getValor());
        }

        return totalGasto;
    }

    // Calcula quanto resta do limite definido para a categoria
    public BigDecimal calcularSaldoRestante(Integer idOrcamento) {
        Orcamento orcamento = buscar(idOrcamento);
        BigDecimal totalGasto = calcularGasto(idOrcamento);

        return orcamento.getLimiteValor().subtract(totalGasto);
    }

    // Calcula o percentual de consumo do orçamento
    public BigDecimal calcularPercentual(Integer idOrcamento) {
        Orcamento orcamento = buscar(idOrcamento);
        BigDecimal totalGasto = calcularGasto(idOrcamento);

        if (orcamento.getLimiteValor().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalGasto.divide(orcamento.getLimiteValor(), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private void validarOrcamento(Orcamento orcamento) {
        if (orcamento.getLimiteValor() == null ||
                orcamento.getLimiteValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O limite do orçamento deve ser maior que zero");
        }
        if (orcamento.getUsuario() == null) {
            throw new RuntimeException("Usuário é obrigatório");
        }
        if (orcamento.getCategoria() == null) {
            throw new RuntimeException("Categoria é obrigatória");
        }
    }
}
