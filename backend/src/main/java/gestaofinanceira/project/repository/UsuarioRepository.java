package gestaofinanceira.project.repository;

import gestaofinanceira.project.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    // Altera para Optional<Usuario> para suportar .orElseThrow() e .ifPresent()
    Optional<Usuario> findByEmail(String email);
}
