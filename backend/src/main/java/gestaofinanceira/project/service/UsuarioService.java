package gestaofinanceira.project.service;

import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Método de Login necessário para o endpoint de autenticação
    public Usuario login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail ou senha incorretos"));

        if (usuario.getSenha() == null || !usuario.getSenha().equals(senha)) {
            throw new RuntimeException("E-mail ou senha incorretos");
        }

        return usuario;
    }

    public Usuario criar(Usuario usuario) {
        validarEmailDuplicado(usuario.getEmail(), null);
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    public Usuario atualizar(Integer id, Usuario usuario) {
        Usuario existente = buscarPorId(id);
        validarEmailDuplicado(usuario.getEmail(), id);

        existente.setNome(usuario.getNome());
        existente.setEmail(usuario.getEmail());
        existente.setSenha(usuario.getSenha());
        existente.setSalarioLiquido(usuario.getSalarioLiquido());

        return usuarioRepository.save(existente);
    }

    public void deletar(Integer id) {
        Usuario existente = buscarPorId(id);
        usuarioRepository.delete(existente);
    }

    private void validarEmailDuplicado(String email, Integer idAtual) {
        usuarioRepository.findByEmail(email).ifPresent(u -> {
            if (idAtual == null || !u.getIdUsuario().equals(idAtual)) {
                throw new IllegalArgumentException("E-mail já cadastrado");
            }
        });
    }
}