package gestaofinanceira.project.controller;

import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") // Libera as chamadas do React (localhost:5173)
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    
@PostMapping("/login")
    public Usuario login(@RequestBody Usuario credentials) {
        return usuarioService.login(credentials.getEmail(), credentials.getSenha());
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