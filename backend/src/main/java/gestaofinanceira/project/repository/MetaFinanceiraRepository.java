package gestaofinanceira.project.repository;

import gestaofinanceira.project.model.MetaFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira, Integer> {

    List<MetaFinanceira> findByUsuario_IdUsuario(Integer usuarioId);
}
