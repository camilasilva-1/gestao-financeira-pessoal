package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.*;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.*;
import gestaofinanceira.project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

/** Demonstração local. Não chama APIs bancárias nem utiliza dados de contas reais. */
@Service
public class IntegracaoBancariaSimuladaService {
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final TransacaoRepository transacoes;
    private final ClassificadorCategoria classificador = new ClassificadorCategoria();

    public IntegracaoBancariaSimuladaService(UsuarioRepository usuarios,
            CategoriaRepository categorias, TransacaoRepository transacoes) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.transacoes = transacoes;
    }

    public ExtratoSimuladoDTO extrato(String competencia) {
        YearMonth mes = validarCompetencia(competencia);
        return new ExtratoSimuladoDTO("SIMULADO", "Bradesco — demonstração fictícia",
            "DEMO-001 (sem vínculo bancário)", mes.toString(), List.of(
                movimento(mes, "01", "Crédito fictício", "Empresa Demo", "3000.00", 5, Tipo.RECEITA),
                movimento(mes, "02", "Compra fictícia no mercado", "Supermercado Demo", "250.00", 10, Tipo.DESPESA),
                movimento(mes, "03", "Corrida fictícia", "Uber Demo", "35.00", 15, Tipo.DESPESA)));
    }

    private MovimentacaoSimuladaDTO movimento(YearMonth mes, String sequencia, String descricao,
            String estabelecimento, String valor, int dia, Tipo tipo) {
        return new MovimentacaoSimuladaDTO("DEMO-001-" + mes + "-" + sequencia,
            descricao, estabelecimento, new BigDecimal(valor), mes.atDay(dia), tipo,
            classificador.sugerir(estabelecimento, descricao));
    }

    @Transactional
    public ImportacaoSimuladaDTO importar(Integer usuarioId, String competencia) {
        ExtratoSimuladoDTO extrato = extrato(competencia);
        if (usuarioId == null || usuarioId <= 0) throw new IllegalArgumentException("usuarioId deve ser positivo");
        // Serializa importações do mesmo usuário, inclusive em requisições simultâneas.
        Usuario usuario = usuarios.buscarParaImportacao(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        Map<String, Categoria> disponiveis = new HashMap<>();
        for (MovimentacaoSimuladaDTO m : extrato.movimentacoes()) {
            Categoria categoria = categorias.findByNome(m.categoriaSugerida());
            if (categoria == null) throw new IllegalArgumentException(
                "Cadastre antes a categoria: " + m.categoriaSugerida());
            disponiveis.put(m.categoriaSugerida(), categoria);
        }
        List<Integer> ids = new ArrayList<>();
        int ignoradas = 0;
        for (MovimentacaoSimuladaDTO m : extrato.movimentacoes()) {
            if (transacoes.existsByUsuario_IdUsuarioAndIdExternoSimulado(usuarioId, m.idExterno())) {
                ignoradas++;
                continue;
            }
            Transacao t = new Transacao();
            t.setUsuario(usuario);
            t.setCategoria(disponiveis.get(m.categoriaSugerida()));
            t.setDescricao(m.descricao());
            t.setEstabelecimento(m.estabelecimento());
            t.setValor(m.valor());
            t.setDataTransacao(m.dataTransacao());
            t.setTipo(m.tipo());
            t.setRecorrencia(false);
            t.setIdExternoSimulado(m.idExterno());
            ids.add(transacoes.save(t).getIdTransacao());
        }
        return new ImportacaoSimuladaDTO("SIMULADO", usuarioId, ids.size(), ignoradas, ids);
    }

    public static YearMonth validarCompetencia(String competencia) {
        try {
            if (competencia == null || !competencia.matches("[0-9]{4}-[0-9]{2}"))
                throw new IllegalArgumentException();
            YearMonth mes = YearMonth.parse(competencia);
            if (mes.getYear() < 1900 || mes.getYear() > 9999) throw new IllegalArgumentException();
            return mes;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Competência inválida: use AAAA-MM, entre 1900-01 e 9999-12");
        }
    }
}
