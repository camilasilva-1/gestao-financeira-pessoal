package gestaofinanceira.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gestaofinanceira.project.config.SessionOwnershipFilter;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.repository.HistoricoRelatorioMensalRepository;
import gestaofinanceira.project.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RN009: teste integrado real (Spring Web + Hibernate + banco descartavel H2).
 * NUNCA utiliza o MySQL do usuario: datasource configurado explicitamente abaixo.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:historico_etapa2_integracao;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class HistoricoRelatorioMensalIntegracaoTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired HistoricoRelatorioMensalRepository historico;
    @Autowired ObjectMapper json;

    @Test
    void deveSalvarConsultarEIsolarHistoricoNoBancoTemporario() throws Exception {
        Usuario dono = new Usuario();
        dono.setNome("Dono temporario");
        dono.setEmail("dono-historico@example.invalid");
        dono.setSenha("nao_utilizar");
        dono = usuarios.saveAndFlush(dono);

        Usuario outro = new Usuario();
        outro.setNome("Outro temporario");
        outro.setEmail("outro-historico@example.invalid");
        outro.setSenha("nao_utilizar");
        outro = usuarios.saveAndFlush(outro);

        MockHttpSession sessao = new MockHttpSession();
        sessao.setAttribute(SessionOwnershipFilter.USER_ID, dono.getIdUsuario());

        String periodo = "?usuarioId=" + dono.getIdUsuario()
            + "&inicio=2026-11-01&fim=2026-11-30";

        long antes = historico.count();
        String resposta = mvc.perform(post("/api/relatorios/mensal/arquivar" + periodo)
                .session(sessao)
                .header("X-Requested-With", "XMLHttpRequest"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsonNode salvo = json.readTree(resposta);
        assertNotNull(salvo.get("idHistorico"));
        assertEquals("2026-11-01", salvo.get("inicio").asText());
        assertEquals("2026-11-30", salvo.get("fim").asText());
        assertEquals(antes + 1, historico.count());
        assertTrue(historico.findById(salvo.get("idHistorico").asInt())
            .orElseThrow().getRelatorioJson().contains("receitaTotal"));

        String lista = mvc.perform(get("/api/relatorios/historico?usuarioId=" + dono.getIdUsuario())
                .session(sessao))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        assertTrue(lista.contains("2026-11-01"));

        mvc.perform(get("/api/relatorios/historico?usuarioId=" + outro.getIdUsuario())
                .session(sessao))
            .andExpect(status().isForbidden());

        // Sem autenticacao, nenhuma informacao do historico deve ser exibida.
        mvc.perform(get("/api/relatorios/historico?usuarioId=" + dono.getIdUsuario()))
            .andExpect(status().isUnauthorized());

        // Apenas periodo mensal completo pode ser arquivado.
        mvc.perform(post("/api/relatorios/mensal/arquivar?usuarioId=" + dono.getIdUsuario()
                + "&inicio=2026-11-02&fim=2026-11-30")
                .session(sessao)
                .header("X-Requested-With", "XMLHttpRequest"))
            .andExpect(status().isBadRequest());

        assertEquals(antes + 1, historico.count());
    }
}
