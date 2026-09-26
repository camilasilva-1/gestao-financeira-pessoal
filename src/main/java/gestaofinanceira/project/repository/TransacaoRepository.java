package gestaofinanceira.project.repository;

import gestaofinanceira.project.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoRepository extends JpaRepository<Transacao, Integer> {
}

