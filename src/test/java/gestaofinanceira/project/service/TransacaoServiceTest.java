package gestaofinanceira.project.service;

import gestaofinanceira.project.exception.TransacaoNotFoundException;
import gestaofinanceira.project.model.Transacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransacaoServiceTest {

    private TransacaoService service;

    @BeforeEach
    void configurar() {
        service = new TransacaoService();
    }

    @Test
    void deveAdicionarTransacaoValida() {
        Transacao transacao = new Transacao(
                "Salário",
                2000.0,
                LocalDate.now(),
                "Receita",
                null
        );

        Transacao resultado = service.adicionar(transacao);

        assertEquals(transacao, resultado);
        assertEquals(1, service.listar().size());
    }

    @Test
    void deveRejeitarValorMenorOuIgualAZero() {
        Transacao transacao = new Transacao(
                "Compra",
                -50.0,
                LocalDate.now(),
                "Despesa",
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.adicionar(transacao)
        );
    }

    @Test
    void deveBuscarTransacaoExistente() {
        Transacao transacao = new Transacao(
                "Aluguel",
                1000.0,
                LocalDate.now(),
                "Despesa",
                null
        );

        service.adicionar(transacao);

        Transacao resultado = service.buscar(0);

        assertEquals(transacao, resultado);
    }

    @Test
    void deveLancarErroAoBuscarTransacaoInexistente() {
        assertThrows(
                TransacaoNotFoundException.class,
                () -> service.buscar(999)
        );
    }
}