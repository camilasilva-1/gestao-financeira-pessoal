package gestaofinanceira.project.controller;

import gestaofinanceira.project.dto.*;
import gestaofinanceira.project.service.RelatorioService;
import gestaofinanceira.project.service.HistoricoRelatorioMensalService;
import gestaofinanceira.project.model.HistoricoRelatorioMensal;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import gestaofinanceira.project.config.SessionOwnershipFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private HistoricoRelatorioMensalService historicoService;

    // RN009: somente a acao explicita do usuario gera uma copia historica.
    @PostMapping("/mensal/arquivar")
    public ResponseEntity<HistoricoRelatorioMensal> arquivar(
            @RequestParam Integer usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            HttpSession session) throws JsonProcessingException {
        if (!usuarioId.equals(session.getAttribute(SessionOwnershipFilter.USER_ID))) {
            return ResponseEntity.status(403).build();
        }
        try {
            return ResponseEntity.ok(historicoService.salvar(usuarioId, inicio, fim));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/historico")
    public ResponseEntity<List<HistoricoRelatorioMensal>> listarHistorico(
            @RequestParam Integer usuarioId,
            HttpSession session) {
        if (!usuarioId.equals(session.getAttribute(SessionOwnershipFilter.USER_ID))) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(historicoService.listar(usuarioId));
    }

    // Relatório mensal (com intervalo)
    @GetMapping("/mensal")
    public ResponseEntity<RelatorioMensalDTO> gerarRelatorioMensal(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        RelatorioMensalDTO relatorio = relatorioService.gerarRelatorioMensal(usuarioId, inicio, fim);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/distribuicao")
    public ResponseEntity<List<DistribuicaoGastosDTO>> getDistribuicao(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        return ResponseEntity.ok(relatorioService.montarDistribuicao(usuarioId, inicio, fim));
    }

    @GetMapping("/evolucao")
    public ResponseEntity<EvolucaoMensalDTO> getEvolucao(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        return ResponseEntity.ok(relatorioService.montarEvolucao(usuarioId, inicio, fim));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<RankingCategoriaDTO>> getRanking(
            @RequestParam Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        return ResponseEntity.ok(relatorioService.montarRanking(usuarioId, inicio, fim));
    }
}
