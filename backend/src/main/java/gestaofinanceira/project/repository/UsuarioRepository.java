package gestaofinanceira.project.repository;

import gestaofinanceira.project.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.idUsuario = :id")
    java.util.Optional<Usuario> buscarParaImportacao(@org.springframework.data.repository.query.Param("id") Integer id);

    Usuario findByEmail(String email);
}
