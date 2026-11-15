package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.MetaFinanceiraDTO;
import gestaofinanceira.project.model.MetaFinanceira;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.MetaFinanceiraRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MetaFinanceiraService {

    private final TransacaoService transacaoService;
    private final MetaFinanceiraRepository metaRepository;
    private final UsuarioRepository usuarioRepository;

    public MetaFinanceiraService(
            TransacaoService transacaoService,
            MetaFinanceiraRepository metaRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.transacaoService = transacaoService;
        this.metaRepository = metaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public MetaFinanceira criarMeta(Integer usuarioId, MetaFinanceira meta) {
        if (meta.getNome() == null || meta.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome da meta é obrigatório");
        }
        if (meta.getValorObjetivo() == null || meta.getValorObjetivo() <= 0) {
            throw new IllegalArgumentException("Valor objetivo deve ser maior que zero");
        }
        if (meta.getDataDesejada() == null) {
            throw new IllegalArgumentException("Prazo da meta é obrigatório");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Usuário não encontrado"));

        meta.setUsuario(usuario);
        atualizarIndicadores(meta);
        return metaRepository.save(meta);
    }

    public List<MetaFinanceira> listarPorUsuario(Integer usuarioId) {
        return metaRepository.findByUsuario_IdUsuario(usuarioId);
    }

    public List<MetaFinanceira> listarTodas() {
        return metaRepository.findAll();
    }


    // Atualiza apenas o valor guardado; os demais campos permanecem os mesmos.
    public MetaFinanceira atualizarValorAcumulado(Integer usuarioId, Integer metaId, Double valor) {
        MetaFinanceira meta = buscarMetaDoUsuario(usuarioId, metaId);
        if (valor == null || !Double.isFinite(valor) || valor < 0) {
            throw new IllegalArgumentException("Valor acumulado deve ser maior ou igual a zero");
        }
        meta.setValorAcumulado(valor);
        atualizarIndicadores(meta);
        return metaRepository.save(meta);
    }

    public void excluirMeta(Integer usuarioId, Integer metaId) {
        MetaFinanceira meta = buscarMetaDoUsuario(usuarioId, metaId);
        metaRepository.delete(meta);
    }

    private MetaFinanceira buscarMetaDoUsuario(Integer usuarioId, Integer metaId) {
        MetaFinanceira meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Meta não encontrada"));
        if (meta.getUsuario() == null
                || !usuarioId.equals(meta.getUsuario().getIdUsuario())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Meta não pertence a este usuário");
        }
        return meta;
    }

    // Indicadores previstos no Takeoff: percentual, prazo e valor mensal necessário.
    private void atualizarIndicadores(MetaFinanceira meta) {
        if (meta.getNome() == null || meta.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome da meta é obrigatório");
        }
        if (meta.getValorObjetivo() == null
                || !Double.isFinite(meta.getValorObjetivo())
                || meta.getValorObjetivo() <= 0) {
            throw new IllegalArgumentException("Valor objetivo deve ser maior que zero");
        }
        if (meta.getDataDesejada() == null) {
            throw new IllegalArgumentException("Prazo da meta é obrigatório");
        }
        double guardado = meta.getValorAcumulado() == null ? 0.0 : meta.getValorAcumulado();
        if (!Double.isFinite(guardado) || guardado < 0) {
            throw new IllegalArgumentException("Valor acumulado não pode ser negativo");
        }
        meta.setValorAcumulado(guardado);
        double percentual = BigDecimal.valueOf(guardado / meta.getValorObjetivo() * 100)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        meta.setPercentualAtingido(percentual);
        LocalDate hoje = LocalDate.now();
        long dias = Math.max(0, ChronoUnit.DAYS.between(hoje, meta.getDataDesejada()));
        long meses = Math.max(0, ChronoUnit.MONTHS.between(hoje, meta.getDataDesejada()));
        meta.setDiasRestantes(dias);
        double restante = Math.max(0, meta.getValorObjetivo() - guardado);
        double mensal = meses > 0 ? restante / meses : restante;
        meta.setValorMensalNecessario(
                BigDecimal.valueOf(mensal).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    public MetaFinanceiraDTO calcularMeta(Integer usuarioId, MetaFinanceiraDTO meta) {

        if (meta.getValorObjetivo() == null || meta.getValorObjetivo() <= 0) {
            throw new IllegalArgumentException("Valor objetivo deve ser maior que zero");
        }
        if (meta.getDataDesejada() == null) {
            throw new IllegalArgumentException("Data desejada é obrigatória");
        }
        // Valor guardado informado na própria meta; receitas não são automaticamente poupança.
        double acumulado = meta.getValorAcumulado() != null ? meta.getValorAcumulado() : 0.0;
        if (acumulado < 0) {
            throw new IllegalArgumentException("Valor acumulado não pode ser negativo");
        }

        meta.setValorAcumulado(acumulado);

        double percentual = (acumulado / meta.getValorObjetivo()) * 100;
        percentual = BigDecimal.valueOf(percentual)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        meta.setPercentualAtingido(percentual);

        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), meta.getDataDesejada());
        if (diasRestantes < 0) diasRestantes = 0;
        meta.setDiasRestantes(diasRestantes);

        long mesesRestantes = ChronoUnit.MONTHS.between(LocalDate.now(), meta.getDataDesejada());
        if (mesesRestantes < 0) mesesRestantes = 0;

        double restante = Math.max(0, meta.getValorObjetivo() - acumulado);
        double mensal = mesesRestantes > 0 ? restante / mesesRestantes : restante;

        mensal = BigDecimal.valueOf(mensal)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        meta.setValorMensalNecessario(mensal);

        return meta;
    }
}
