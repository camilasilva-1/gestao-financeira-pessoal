package gestaofinanceira.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import gestaofinanceira.project.config.SessionOwnershipFilter;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RNF018: mede o processamento HTTP de uma transacao nova, incluindo persistencia JPA,
 * em banco H2 TEMPORARIO. Nao se conecta ao MySQL do usuario.
 * Nao extrapolar esta medicao para infraestrutura de producao.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:auditoria_etapa1;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class CadastroTransacaoDesempenhoTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired TransacaoRepository transacoes;
    @Autowired ObjectMapper mapper;

    @Test
    void cadastroDeReceitaEmBancoIsoladoDeveProcessarEmMenosDe500ms() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Conta de teste temporaria");
        usuario.setEmail("auditoria-isolada@example.invalid");
        usuario.setSenha("nao_utilizar");
        usuario = usuarios.saveAndFlush(usuario);
        assertNotNull(usuario.getIdUsuario());

        MockHttpSession sessao = new MockHttpSession();
        sessao.setAttribute(SessionOwnershipFilter.USER_ID, usuario.getIdUsuario());

        // Aquecimento do contexto, serializacao, Hibernate e conexao (fora da medicao).
        executarCadastro(usuario.getIdUsuario(), sessao, 0);
        executarCadastro(usuario.getIdUsuario(), sessao, 1);

        List<Double> temposMs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            long inicio = System.nanoTime();
            executarCadastro(usuario.getIdUsuario(), sessao, i + 2);
            double ms = (System.nanoTime() - inicio) / 1_000_000.0;
            temposMs.add(ms);
        }

        double max = temposMs.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
        System.out.println("RNF018 H2 ISOLADO: temposMs=" + temposMs + " maiorMs=" + max + " limiteMs=500");
        assertEquals(7, transacoes.findByUsuarioIdUsuario(usuario.getIdUsuario()).size(),
                "A persistencia das 7 receitas de teste deve ser confirmada");
        assertTrue(max < 500, "RNF018 nao atingido no ambiente isolado; maior tempo: " + max + " ms");
    }

    private void executarCadastro(int usuarioId, MockHttpSession sessao, int numero) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("descricao", "Teste temporario " + numero);
        payload.put("valor", 19.9);
        payload.put("tipo", "RECEITA");
        payload.put("usuarioId", usuarioId);
        payload.put("dataTransacao", LocalDate.of(2026, 11, 8).toString());
        payload.put("recorrencia", false);

        mvc.perform(post("/api/transacoes")
                .session(sessao)
                .header("X-Requested-With", "XMLHttpRequest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(payload)))
            .andExpect(status().isOk());
    }
}
