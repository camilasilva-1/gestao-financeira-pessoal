package gestaofinanceira.project.config;

import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.OrcamentoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SessionOwnershipFilterTest {
    private final SessionOwnershipFilter filter = new SessionOwnershipFilter(
        mock(TransacaoRepository.class), mock(OrcamentoRepository.class));

    private MockHttpServletResponse call(String method, String path, Integer sessionUserId) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRequestURI(path);
        if (sessionUserId != null) {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute(SessionOwnershipFilter.USER_ID, sessionUserId);
            request.setSession(session);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    void exigeLoginParaHistorico() throws Exception {
        assertEquals(401, call("GET", "/api/transacoes/usuario/1", null).getStatus());
    }

    @Test
    void bloqueiaHistoricoDeOutroUsuario() throws Exception {
        assertEquals(403, call("GET", "/api/transacoes/usuario/2", 1).getStatus());
    }

    @Test
    void permiteHistoricoDoProprioUsuario() throws Exception {
        assertEquals(200, call("GET", "/api/transacoes/usuario/1", 1).getStatus());
    }

    @Test
    void bloqueiaListagemGlobalDeUsuarios() throws Exception {
        assertEquals(403, call("GET", "/api/usuarios", 1).getStatus());
    }

    @Test
    void permiteConsultaPublicaDeCategorias() throws Exception {
        assertEquals(200, call("GET", "/categorias", null).getStatus());
    }

    @Test
    void bloqueiaAlteracaoSemCabecalhoAnticsrf() throws Exception {
        assertEquals(403, call("POST", "/api/transacoes", 1).getStatus());
    }

    @Test
    void bloqueiaRelatorioDeOutroUsuario() throws Exception {
        assertEquals(403, call("GET", "/api/relatorios/regra-50-30-20/2", 1).getStatus());
    }

    @Test
    void bloqueiaInsightsDeOutroUsuario() throws Exception {
        assertEquals(403, call("GET", "/insights/2/alertas", 1).getStatus());
    }
}
