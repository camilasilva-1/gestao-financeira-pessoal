package gestaofinanceira.project.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Regras locais e determinísticas: a sugestão não altera o cadastro. */
public final class ClassificadorCategoria {
    private record Regra(String categoria, List<String> termos) {}
    private static final List<Regra> REGRAS = List.of(
        new Regra("Assinaturas", List.of("netflix", "spotify", "disney", "prime video")),
        new Regra("Alimentação", List.of("mercado", "supermercado", "restaurante", "ifood", "padaria", "mcdonald", "mcdonalds", "mcdonald s", "lanche")),
        new Regra("Transporte", List.of("uber", "99", "onibus", "combustivel", "gasolina", "posto", "estacionamento")),
        new Regra("Saúde", List.of("farmacia", "drogaria", "hospital", "clinica", "consulta", "medicamento")),
        new Regra("Educação", List.of("faculdade", "escola", "curso", "livraria", "mensalidade escolar")),
        new Regra("Moradia", List.of("aluguel", "condominio", "financiamento imobiliario")),
        new Regra("Contas e Serviços", List.of("energia", "internet", "telefone", "agua", "luz")),
        new Regra("Lazer", List.of("cinema", "teatro", "show", "parque")),
        new Regra("Impostos", List.of("iptu", "ipva", "darf", "imposto")),
        new Regra("Investimentos", List.of("tesouro", "cdb", "investimento")),
        new Regra("Compras", List.of("roupa", "calcado", "shopping", "amazon", "shopee"))
    );

    public String sugerir(String estabelecimento, String descricao) {
        // O estabelecimento tem prioridade sobre a descrição em caso de conflito.
        for (String texto : List.of(normalizar(estabelecimento), normalizar(descricao))) {
            for (Regra regra : REGRAS) {
                for (String termo : regra.termos()) {
                    if (Pattern.compile("(?:^| )" + Pattern.quote(termo) + "(?: |$)").matcher(texto).find()) {
                        return regra.categoria();
                    }
                }
            }
        }
        return "Outros";
    }

    private String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", " ").trim();
    }
}
