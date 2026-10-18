package gestaofinanceira.project.repository;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Integer> {

    List<Transacao> findByCategoriaAndTipo(
            Categoria categoria,
            Tipo tipo
    );

    List<Transacao> findByCategoriaAndTipoAndDataTransacaoBetween(
        Categoria categoria,
        Tipo tipo,
        LocalDate inicio,
        LocalDate fim
    );
}



