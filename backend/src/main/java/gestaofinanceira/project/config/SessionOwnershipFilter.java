package gestaofinanceira.project.config;

import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.OrcamentoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Restricao de acesso por sessao para o MVP.
 * Nunca confia apenas no usuarioId enviado pelo frontend.
 * Endpoints globais de dados financeiros sao bloqueados por padrao.
 */
@Component
public class SessionOwnershipFilter extends OncePerRequestFilter {
    public static final String USER_ID = "AUTH_USER_ID";
    private final TransacaoRepository transacoes;
    private final OrcamentoRepository orcamentos;

    public SessionOwnershipFilter(TransacaoRepository transacoes, OrcamentoRepository orcamentos) {
        this.transacoes = transacoes;
        this.orcamentos = orcamentos;
    }

    private Matcher match(String path, String pattern) {
        return Pattern.compile(pattern).matcher(path);
    }

    private boolean hasOwnId(String idText, int authenticatedId) {
        try { return Integer.parseInt(idText) == authenticatedId; }
        catch (NumberFormatException ex) { return false; }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String origin = req.getHeader("Origin");
        if ("http://localhost:5173".equals(origin)) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Access-Control-Allow-Credentials", "true");
            res.setHeader("Vary", "Origin");
            res.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With");
            res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        }
        String method = req.getMethod();
        String path = req.getRequestURI();
        if ("OPTIONS".equals(method)) { res.setStatus(204); return; }

        // Cabecalho customizado impede mutacoes disparadas por formularios cross-site.
        if (!"GET".equals(method) && !"HEAD".equals(method)
                && !"XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            res.sendError(403, "Requisicao nao autorizada"); return;
        }

        // Somente cadastro/login e leitura de categorias sao publicos.
        boolean signup = "/api/usuarios".equals(path) && "POST".equals(method);
        boolean login = "/api/usuarios/login".equals(path) && "POST".equals(method);
        boolean publicCategory = "GET".equals(method) && (
            "/categorias".equals(path) || "/categorias/sugerir".equals(path)
            || path.matches("^/categorias/\\d+$"));
        if (signup || login || publicCategory) { chain.doFilter(req, res); return; }

        HttpSession session = req.getSession(false);
        Object sessionId = session == null ? null : session.getAttribute(USER_ID);
        if (!(sessionId instanceof Integer)) {
            res.sendError(401, "Login necessario"); return;
        }
        int userId = (Integer) sessionId;
        if ("POST".equals(method) && "/api/usuarios/logout".equals(path)) {
            chain.doFilter(req, res); return;
        }

        Matcher m;
        boolean authorized = false;
        if ((m = match(path, "^/api/usuarios/(\\d+)$")).matches()) {
            authorized = hasOwnId(m.group(1), userId);
        } else if ((m = match(path, "^/api/transacoes/usuario/(\\d+)$")).matches()) {
            authorized = "GET".equals(method) && hasOwnId(m.group(1), userId);
        } else if ((m = match(path, "^/api/transacoes/(\\d+)/usuario/(\\d+)$")).matches()) {
            authorized = hasOwnId(m.group(2), userId) && ("PUT".equals(method) || "DELETE".equals(method));
        } else if ((m = match(path, "^/api/transacoes/(\\d+)/categoria/(\\d+)/usuario/(\\d+)$")).matches()) {
            authorized = hasOwnId(m.group(3), userId) && "PUT".equals(method);
        } else if ((m = match(path, "^/api/transacoes/(\\d+)$")).matches()) {
            int id = Integer.parseInt(m.group(1));
            authorized = "GET".equals(method) && transacoes.findById(id)
                .map(t -> t.getUsuario().getIdUsuario().equals(userId)).orElse(false);
        } else if ("/api/transacoes".equals(path) && "POST".equals(method)) {
            authorized = true; // O controller compara o usuarioId do corpo com a sessao.
        } else if ((m = match(path, "^/orcamentos/usuario/(\\d+)$")).matches()) {
            authorized = "GET".equals(method) && hasOwnId(m.group(1), userId);
        } else if ((m = match(path, "^/orcamentos/(\\d+)(?:/(?:gasto|saldo|percentual))?$")).matches()) {
            int id = Integer.parseInt(m.group(1));
            authorized = orcamentos.findById(id)
                .map(o -> o.getUsuario().getIdUsuario().equals(userId)).orElse(false);
        } else if ("/orcamentos".equals(path) && "POST".equals(method)) {
            authorized = true; // O controller compara o usuarioId do corpo com a sessao.
        } else if ((m = match(path, "^/metas/(?:usuario|criar|calcular)/(\\d+)$")).matches()) {
            authorized = hasOwnId(m.group(1), userId);
        } else if ((m = match(path, "^/categorias/ranking/(\\d+)$")).matches()) {
            authorized = hasOwnId(m.group(1), userId);
        }
        else if ((m = match(path, "^/insights/(\\d+)(?:/[-a-z0-9]+)?$")).matches()) {
            authorized = "GET".equals(method) && hasOwnId(m.group(1), userId);
        } else if ((m = match(path, "^/api/relatorios/regra-50-30-20/(\\d+)$")).matches()) {
            authorized = "GET".equals(method) && hasOwnId(m.group(1), userId);
        } else if (path.matches("^/api/relatorios/(mensal|distribuicao|evolucao|ranking)$")) {
            authorized = "GET".equals(method)
                && req.getParameter("usuarioId") != null
                && hasOwnId(req.getParameter("usuarioId"), userId);
        }

        // Rotas globais e nao reconhecidas nao devem vazar dados financeiros.
        if (!authorized) { res.sendError(403, "Acesso negado"); return; }
        chain.doFilter(req, res);
    }
}
