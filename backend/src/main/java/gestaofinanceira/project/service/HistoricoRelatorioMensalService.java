package gestaofinanceira.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gestaofinanceira.project.dto.RelatorioMensalDTO;
import gestaofinanceira.project.model.HistoricoRelatorioMensal;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.HistoricoRelatorioMensalRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RN009. Salva SOMENTE quando solicitado de forma explicita pelo usuario.
 * Nao modifica transacoes, categorias nem orcamentos.
 */
@Service
public class HistoricoRelatorioMensalService {
    private final HistoricoRelatorioMensalRepository historico;
    private final UsuarioRepository usuarios;
    private final RelatorioService relatorios;
    private final ObjectMapper json;

    public HistoricoRelatorioMensalService(
            HistoricoRelatorioMensalRepository historico,
            UsuarioRepository usuarios,
            RelatorioService relatorios,
            ObjectMapper json) {
        this.historico = historico;
        this.usuarios = usuarios;
        this.relatorios = relatorios;
        this.json = json;
    }

    public HistoricoRelatorioMensal salvar(Integer usuarioId, LocalDate inicio, LocalDate fim)
            throws JsonProcessingException {
        if (inicio == null || fim == null || fim.isBefore(inicio)
                || inicio.getYear() != fim.getYear()
                || inicio.getMonthValue() != fim.getMonthValue()
                || inicio.getDayOfMonth() != 1
                || !fim.equals(inicio.withDayOfMonth(inicio.lengthOfMonth()))) {
            throw new IllegalArgumentException("Informe o primeiro e o ultimo dia do mesmo mes");
        }

        Usuario dono = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
        RelatorioMensalDTO calculado = relatorios.gerarRelatorioMensal(usuarioId.longValue(), inicio, fim);
        HistoricoRelatorioMensal registro = new HistoricoRelatorioMensal();
        registro.setUsuario(dono);
        registro.setInicio(inicio);
        registro.setFim(fim);
        registro.setGeradoEm(LocalDateTime.now());
        registro.setReceitaTotal(calculado.getReceitaTotal());
        registro.setDespesaTotal(calculado.getDespesaTotal());
        registro.setSaldoFinal(calculado.getSaldoFinal());
        registro.setRelatorioJson(json.writeValueAsString(calculado));
        return historico.save(registro);
    }

    public List<HistoricoRelatorioMensal> listar(Integer usuarioId) {
        return historico.findByUsuarioIdUsuarioOrderByGeradoEmDesc(usuarioId);
    }
}
