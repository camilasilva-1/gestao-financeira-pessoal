package gestaofinanceira.project.repository;

import gestaofinanceira.project.model.HistoricoRelatorioMensal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoRelatorioMensalRepository
        extends JpaRepository<HistoricoRelatorioMensal, Integer> {
    List<HistoricoRelatorioMensal> findByUsuarioIdUsuarioOrderByGeradoEmDesc(Integer usuarioId);
}
