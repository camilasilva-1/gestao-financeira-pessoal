package gestaofinanceira.project.controller;

import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import gestaofinanceira.project.config.SessionOwnershipFilter;

@RestController
@RequestMapping("/api/usuarios")

public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

@PostMapping("/login")
    public Usuario login(@RequestBody Usuario credentials, HttpServletRequest request) {
        Usuario usuario = usuarioService.login(credentials.getEmail(), credentials.getSenha());
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(SessionOwnershipFilter.USER_ID, usuario.getIdUsuario());
        return usuario;
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }

    @PostMapping
    public Usuario criar(@RequestBody Usuario usuario) {
        return usuarioService.criar(usuario);
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable Integer id, @RequestBody Usuario usuario) {
        return usuarioService.atualizar(id, usuario);
    }

    @DeleteMapping("/{id}")
    public String deletar(@PathVariable Integer id) {
        usuarioService.deletar(id);
        return "Usuário removido com sucesso!";
    }
}
