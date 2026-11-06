package gestaofinanceira.project;

import gestaofinanceira.project.model.*;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.repository.*;
import gestaofinanceira.project.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.config.location=optional:classpath:demonstracao-test.properties",
    "spring.datasource.url=jdbc:h2:mem:demo;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class FuncionalidadesDemonstracaoTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired TransacaoRepository transacoes;
    @Autowired IntegracaoBancariaSimuladaService integracao;
    @Autowired SaldoAcumuladoService saldos;
    private Usuario usuario;

    @BeforeEach void preparar() {
        transacoes.deleteAll();
        usuario = new Usuario(); usuario.setNome("Teste"); usuario = usuarios.save(usuario);
        for (String nome : new String[]{"Outros", "Alimentação", "Transporte"}) {
            if (categorias.findByNome(nome) == null) {
                Categoria c = new Categoria(); c.setNome(nome); categorias.save(c);
            }
        }
    }

    @Test void sugereSemAcentosSemCorrespondenciaParcialENaoAlteraBanco() throws Exception {
        mvc.perform(get("/categorias/sugerir").param("descricao", "Compra na FARMÁCIA"))
            .andExpect(status().isOk()).andExpect(content().string("Saúde"));
        mvc.perform(get("/categorias/sugerir").param("estabelecimento", "Uberlandia"))
            .andExpect(content().string("Outros"));
        mvc.perform(get("/categorias/sugerir").param("estabelecimento", "Netflix")
            .param("descricao", "pago no mercado"))
            .andExpect(content().string("Assinaturas"));
        assertEquals(0, transacoes.count());
    }

    @Test void importaUmaVezPorUsuarioECompetencia() throws Exception {
        mvc.perform(get("/api/integracao-bancaria/simulada/extrato").param("competencia", "2026-11"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.modo").value("SIMULADO"))
            .andExpect(jsonPath("$.movimentacoes.length()").value(3));
        assertEquals(0, transacoes.count());
        String body = "{\"usuarioId\":" + usuario.getIdUsuario() + ",\"competencia\":\"2026-11\"}";
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/integracao-bancaria/simulada/importar")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.importadas").value(i == 0 ? 3 : 0))
                .andExpect(jsonPath("$.ignoradas").value(i == 0 ? 0 : 3));
        }
        assertEquals(3, transacoes.count());
        Usuario outro = new Usuario(); outro.setNome("Outro"); outro = usuarios.save(outro);
        assertEquals(3, integracao.importar(outro.getIdUsuario(), "2026-11").importadas());
        assertEquals(3, integracao.importar(usuario.getIdUsuario(), "2026-12").importadas());
        assertEquals(9, transacoes.count());
    }

    @Test void saldoIncluiHistoricoMesVazioAnoNovoENaoMisturaUsuarios() throws Exception {
        integracao.importar(usuario.getIdUsuario(), "2025-11");
        integracao.importar(usuario.getIdUsuario(), "2026-01");
        Usuario outro = new Usuario(); outro.setNome("Outro"); outro = usuarios.save(outro);
        integracao.importar(outro.getIdUsuario(), "2025-12");
        var saldo = saldos.consultar(usuario.getIdUsuario(), "2025-12", "2026-01");
        assertEquals(0, new BigDecimal("2715").compareTo(saldo.saldoAnterior()));
        assertEquals(0, saldo.meses().get(0).saldoMes().compareTo(BigDecimal.ZERO));
        assertEquals(0, new BigDecimal("5430").compareTo(saldo.meses().get(1).saldoAcumulado()));
        mvc.perform(get("/api/relatorios/saldo-acumulado").param("usuarioId", usuario.getIdUsuario().toString())
            .param("inicio", "2025-12").param("fim", "2026-01"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.meses[1].competencia").value("2026-01"));
    }

    @Test void rejeitaParametrosInvalidosSemImportacaoParcial() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> integracao.importar(usuario.getIdUsuario(), "2026-13"));
        assertThrows(IllegalArgumentException.class, () -> integracao.importar(-1, "2026-11"));
        assertThrows(IllegalArgumentException.class, () -> saldos.consultar(usuario.getIdUsuario(), "2026-12", "2026-01"));
        assertThrows(IllegalArgumentException.class, () -> saldos.consultar(usuario.getIdUsuario(), "2000-01", "2026-01"));
        categorias.delete(categorias.findByNome("Transporte"));
        assertThrows(IllegalArgumentException.class, () -> integracao.importar(usuario.getIdUsuario(), "2026-11"));
        assertEquals(0, transacoes.count());
        mvc.perform(get("/api/integracao-bancaria/simulada/extrato").param("competencia", "ruim"))
            .andExpect(status().isBadRequest());
    }

    @Test void permiteSaldoNegativoETransportaParaMesSeguinte() {
        Transacao t = new Transacao(); t.setUsuario(usuario); t.setCategoria(categorias.findByNome("Outros"));
        t.setValor(new BigDecimal("100.00")); t.setTipo(Tipo.DESPESA);
        t.setDataTransacao(LocalDate.of(2026, 10, 10)); transacoes.save(t);
        var saldo = saldos.consultar(usuario.getIdUsuario(), "2026-10", "2026-11");
        assertEquals(0, new BigDecimal("-100").compareTo(saldo.meses().get(1).saldoAcumulado()));
    }
}
