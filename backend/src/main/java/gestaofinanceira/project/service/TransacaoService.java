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
import java.time.LocalDate;
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

    // ============================
    // CRUD BÁSICO
    // ============================

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

    // ============================
    // MÉTODOS AUXILIARES PARA INSIGHTS
    // ============================

    // Calcular gasto por categoria em determinado mês
    public BigDecimal calcularGastoPorCategoria(Integer usuarioId, String nomeCategoria, int mes) {
        LocalDate inicio = LocalDate.of(LocalDate.now().getYear(), mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Object[]> gastos = transacaoRepository.gastosPorCategoria(usuarioId.longValue(), inicio, fim);

        return gastos.stream()
                .filter(obj -> obj[0].equals(nomeCategoria))
                .map(obj -> (BigDecimal) obj[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Calcular receita total do mês
    public BigDecimal calcularReceitaMensal(Integer usuarioId, int mes) {
        LocalDate inicio = LocalDate.of(LocalDate.now().getYear(), mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        return transacaoRepository.somaReceitasPorPeriodo(usuarioId.longValue(), inicio, fim);
    }

    // Calcular despesa total do mês
    public BigDecimal calcularDespesaMensal(Integer usuarioId, int mes) {
        LocalDate inicio = LocalDate.of(LocalDate.now().getYear(), mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        return transacaoRepository.somaDespesasPorPeriodo(usuarioId.longValue(), inicio, fim);
    }

    // ============================
    // MÉTODOS INTERNOS DE VALIDAÇÃO E CARREGAMENTO
    // ============================

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
