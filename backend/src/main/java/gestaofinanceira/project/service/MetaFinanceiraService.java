package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.MetaFinanceiraDTO;
import gestaofinanceira.project.model.MetaFinanceira;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.MetaFinanceiraRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        meta.setUsuario(usuario);
        return metaRepository.save(meta);
    }

    public List<MetaFinanceira> listarPorUsuario(Integer usuarioId) {
        return metaRepository.findByUsuario_IdUsuario(usuarioId);
    }

    public List<MetaFinanceira> listarTodas() {
        return metaRepository.findAll();
    }

    public MetaFinanceiraDTO calcularMeta(Integer usuarioId, MetaFinanceiraDTO meta) {

        List<Transacao> transacoes = transacaoService.listar()
                .stream()
                .filter(t -> t.getUsuario().getIdUsuario().equals(usuarioId))
                .filter(t -> t.getTipo().toString().equalsIgnoreCase("RECEITA"))
                .collect(Collectors.toList());

        double acumulado = transacoes.stream()
                .mapToDouble(t -> t.getValor().doubleValue())
                .sum();

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

        double restante = meta.getValorObjetivo() - acumulado;
        double mensal = mesesRestantes > 0 ? restante / mesesRestantes : restante;

        mensal = BigDecimal.valueOf(mensal)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        meta.setValorMensalNecessario(mensal);

        return meta;
    }
}
