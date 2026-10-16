package gestaofinanceira.project.repository;

import gestaofinanceira.project.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Integer> {

    // Busca todos os orçamentos de um usuário específico
    List<Orcamento> findByUsuarioIdUsuario(Integer usuarioId);
}
