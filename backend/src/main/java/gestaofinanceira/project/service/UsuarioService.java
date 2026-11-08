package gestaofinanceira.project.service;

import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Método de Login necessário para o endpoint de autenticação
    public Usuario login(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("E-mail ou senha incorretos"));

        if (usuario.getSenha() == null ||
                !passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RuntimeException("E-mail ou senha incorretos");
        }

        return usuario;
    }

    public Usuario criar(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().isBlank()
                || usuario.getEmail() == null || usuario.getEmail().isBlank()
                || usuario.getSenha() == null || usuario.getSenha().length() < 8) {
            throw new IllegalArgumentException("Informe nome, e-mail e senha de pelo menos 8 caracteres");
        }
        validarEmailDuplicado(usuario.getEmail(), null);

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

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
        existente.setSalarioLiquido(usuario.getSalarioLiquido());

        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            existente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
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
