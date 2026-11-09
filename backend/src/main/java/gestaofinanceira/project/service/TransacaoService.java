
package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.RankingCategoriaDTO;
import gestaofinanceira.project.dto.TransacaoDTO;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.exception.TransacaoNotFoundException;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.CategoriaRepository;
import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public TransacaoService(
            TransacaoRepository transacaoRepository,
            CategoriaRepository categoriaRepository,
            UsuarioRepository usuarioRepository) {
        this.transacaoRepository = transacaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // CRUD básico

    public List<Transacao> listar() {
        return transacaoRepository.findAll();
    }

    public List<Transacao> listarPorUsuario(int usuarioId) {
        return transacaoRepository.findByUsuarioIdUsuario(usuarioId);
    }

    // Filtros de consulta

    public List<Transacao> filtrarPorTipo(Tipo tipo) {
        return transacaoRepository.findByTipo(tipo);
    }

    public List<Transacao> filtrarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return transacaoRepository.findByDataTransacaoBetween(inicio, fim);
    }

    public List<Transacao> filtrarPorCategoria(Integer idCategoria) {
        return transacaoRepository.findByCategoria_IdCategoria(idCategoria);
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

    // Cadastro usando DTO enviado pelo React

    public Transacao adicionarDTO(TransacaoDTO dto) {
        if (dto.getDataTransacao() == null) {
            throw new IllegalArgumentException("Data é obrigatória");
        }
        Transacao t = new Transacao();
        t.setDescricao(dto.getDescricao());

        if (dto.getValor() != null) {
            t.setValor(BigDecimal.valueOf(dto.getValor()));
        }

        if (dto.getTipo() != null) {
            t.setTipo(Tipo.valueOf(dto.getTipo().toUpperCase()));
        }

        t.setDataTransacao(dto.getDataTransacao());
        t.setEstabelecimento(dto.getEstabelecimento());
        t.setRecorrencia(dto.getRecorrencia() != null ? dto.getRecorrencia() : false);

        if (dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Categoria categoria = dto.getCategoriaId() == null ? null : categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        t.setUsuario(usuario);
        t.setCategoria(categoria);

        validarTransacao(t);
        return transacaoRepository.save(t);
    }

    // Editar verificando o proprietário

    public Transacao editar(int id, int usuarioId, Transacao nova) {
        Transacao existente = buscar(id);

        if (!existente.getUsuario().getIdUsuario().equals(usuarioId)) {
            throw new RuntimeException("Transação não pertence a este usuário.");
        }

        nova.setUsuario(existente.getUsuario());
        validarTransacao(nova);

        Categoria categoria = null;
        if (nova.getCategoria() != null && nova.getCategoria().getIdCategoria() != null) {
            categoria = categoriaRepository.findById(nova.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        }

        existente.setDescricao(nova.getDescricao());
        existente.setValor(nova.getValor());
        existente.setDataTransacao(nova.getDataTransacao());
        existente.setTipo(nova.getTipo());
        existente.setEstabelecimento(nova.getEstabelecimento());
        existente.setRecorrencia(nova.getRecorrencia());
        existente.setCategoria(categoria);

        return transacaoRepository.save(existente);
    }

    // Remover verificando o proprietário

    public void remover(int id, int usuarioId) {
        Transacao existente = buscar(id);

        if (!existente.getUsuario().getIdUsuario().equals(usuarioId)) {
            throw new RuntimeException("Transação não pertence a este usuário.");
        }

        transacaoRepository.delete(existente);
    }

    // Atualizar categoria verificando o proprietário

    public Transacao atualizarCategoria(int idTransacao, int idCategoria, int usuarioId) {
        Transacao transacao = buscar(idTransacao);

        if (!transacao.getUsuario().getIdUsuario().equals(usuarioId)) {
            throw new RuntimeException("Transação não pertence a este usuário.");
        }

        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        transacao.setCategoria(categoria);
        return transacaoRepository.save(transacao);
    }

    // Métodos auxiliares para insights

    public BigDecimal calcularGastoPorCategoria(Integer usuarioId, String nomeCategoria, int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mês inválido");
        }
        return calcularGastoPorCategoria(usuarioId, nomeCategoria,
                LocalDate.of(LocalDate.now().getYear(), mes, 1));
    }

    public BigDecimal calcularGastoPorCategoria(Integer usuarioId, String nomeCategoria, LocalDate referencia) {
        LocalDate inicio = referencia.withDayOfMonth(1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Object[]> gastos = transacaoRepository.gastosPorCategoria(
                usuarioId.longValue(), inicio, fim);

        return gastos.stream()
                .filter(obj -> obj[0].equals(nomeCategoria))
                .map(obj -> (BigDecimal) obj[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularReceitaMensal(Integer usuarioId, int mes) {
        LocalDate inicio = LocalDate.of(LocalDate.now().getYear(), mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        return transacaoRepository.somaReceitasPorPeriodo(
                usuarioId.longValue(), inicio, fim);
    }
    public BigDecimal calcularReceitaPorPeriodo(
        Integer usuarioId,
        LocalDate inicio,
        LocalDate fim) {

    return transacaoRepository.somaReceitasPorPeriodo(
            usuarioId.longValue(),
            inicio,
            fim);
}
    public BigDecimal calcularDespesaMensal(Integer usuarioId, int mes) {
        LocalDate inicio = LocalDate.of(LocalDate.now().getYear(), mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        return transacaoRepository.somaDespesasPorPeriodo(
                usuarioId.longValue(), inicio, fim);
    }

    public BigDecimal calcularDespesaPorPeriodo(
        Integer usuarioId,
        LocalDate inicio,
        LocalDate fim) {

    return transacaoRepository.somaDespesasPorPeriodo(
            usuarioId.longValue(),
            inicio,
            fim);
}
    // Ranking de categorias

    public List<RankingCategoriaDTO> gerarRankingCategorias(Integer usuarioId, int mes) {
        LocalDate inicio = LocalDate.of(LocalDate.now().getYear(), mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Object[]> gastos = transacaoRepository.gastosPorCategoria(
                usuarioId.longValue(), inicio, fim);

        List<String> categoriasPadrao = List.of(
                "Alimentação", "Moradia", "Transporte", "Saúde", "Educação",
                "Lazer", "Compras", "Investimentos", "Assinaturas",
                "Impostos", "Contas e Serviços", "Outros");

        if (gastos == null || gastos.isEmpty()) {
            return categoriasPadrao.stream()
                    .map(nome -> new RankingCategoriaDTO(nome, BigDecimal.ZERO, 0.0))
                    .collect(Collectors.toList());
        }

        BigDecimal total = gastos.stream()
                .map(obj -> (BigDecimal) obj[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<RankingCategoriaDTO> ranking = gastos.stream()
                .map(obj -> {
                    String categoria = (String) obj[0];
                    BigDecimal valor = (BigDecimal) obj[1];

                    double percentual = total.compareTo(BigDecimal.ZERO) > 0
                            ? valor.divide(total, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .doubleValue()
                            : 0.0;

                    return new RankingCategoriaDTO(categoria, valor, percentual);
                })
                .collect(Collectors.toList());

        categoriasPadrao.forEach(nome -> {
            boolean existe = ranking.stream()
                    .anyMatch(r -> r.getCategoria().equals(nome));

            if (!existe) {
                ranking.add(new RankingCategoriaDTO(nome, BigDecimal.ZERO, 0.0));
            }
        });

        ranking.sort((a, b) -> b.getValor().compareTo(a.getValor()));

        return ranking;
    }

    // Métodos internos de validação e carregamento

    private void carregarRelacionamentos(Transacao t) {
        Usuario usuario = usuarioRepository.findById(t.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Categoria categoria = null;
        if (t.getCategoria() != null && t.getCategoria().getIdCategoria() != null) {
            categoria = categoriaRepository.findById(t.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        }

        t.setUsuario(usuario);
        t.setCategoria(categoria);
    }

    private void validarTransacao(Transacao t) {
        if (t.getValor() == null || t.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser maior que zero");
        }

        if (t.getDescricao() == null || t.getDescricao().isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        if (t.getDataTransacao() == null || t.getTipo() == null) {
            throw new IllegalArgumentException("Data e tipo são obrigatórios");
        }

        if (t.getUsuario() == null || t.getUsuario().getIdUsuario() == null) {
            throw new IllegalArgumentException("Usuário é obrigatório");
        }

        if (t.getTipo() == Tipo.DESPESA &&
                (t.getCategoria() == null || t.getCategoria().getIdCategoria() == null)) {
            throw new IllegalArgumentException("Categoria é obrigatória para despesas");
        }
    }
}
