package gestaofinanceira.project.service;

import gestaofinanceira.project.dto.InsightDTO;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Orcamento;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * RN 3.4 - faixas do Takeoff:
 * ate 70 Normal, de 71 a 89 Atencao, a partir de 90 Risco, acima de 100 Estourado.
 * A lacuna de 90% foi classificada como Risco por precaucao.
 */
@ExtendWith(MockitoExtension.class)
class AlertasOrcamentoFaixasTest {

    @Mock OrcamentoService orcamentoService;
    @Mock TransacaoService transacaoService;
    @Mock CategoriaService categoriaService;
    @InjectMocks InsightService service;

    @ParameterizedTest
    @CsvSource({
        "0,Normal",
        "70,Normal",
        "71,Atenção",
        "89,Atenção",
        "90,Risco",
        "100,Risco",
        "101,Estourado"
    })
    void deveRespeitarFaixasDeAlerta(String percentual, String esperado) {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(3);
        categoria.setNome("Transporte");

        Orcamento orcamento = new Orcamento();
        orcamento.setIdOrcamento(7);
        orcamento.setCategoria(categoria);
        orcamento.setLimiteValor(new BigDecimal("1000"));

        when(orcamentoService.listarPorUsuario(9)).thenReturn(List.of(orcamento));
        when(orcamentoService.calcularPercentual(7)).thenReturn(new BigDecimal(percentual));

        List<InsightDTO> alertas = service.gerarAlertas(9);
        assertEquals(1, alertas.size());
        assertEquals(esperado, alertas.get(0).getNivelAlerta());
    }
}
