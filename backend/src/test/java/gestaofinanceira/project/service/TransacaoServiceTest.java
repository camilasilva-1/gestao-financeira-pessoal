package gestaofinanceira.project.service;

import gestaofinanceira.project.exception.TransacaoNotFoundException;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.repository.CategoriaRepository;
import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    @Test
    void deveListarTodasAsTransacoes() {

        // cria duas transações para o teste
        List<Transacao> transacoes = List.of(
                new Transacao(),
                new Transacao()
        );

        // define o que o repository deve retornar
        when(transacaoRepository.findAll()).thenReturn(transacoes);

        // chama o método listar
        List<Transacao> resultado = transacaoService.listar();

        // confere a quantidade retornada
        assertEquals(2, resultado.size());
    }

    @Test
    void deveBuscarTransacaoPorId() {

        // cria uma transação para o teste
        Transacao transacao = new Transacao();

        // define que o ID 1 deve retornar essa transação
        when(transacaoRepository.findById(1))
                .thenReturn(Optional.of(transacao));

        // chama o método buscar
        Transacao resultado = transacaoService.buscar(1);

        // confere se encontrou a transação esperada
        assertEquals(transacao, resultado);
    }

    @Test
    void deveDarErroQuandoTransacaoNaoExistir() {

        // define que não existe transação com o ID 99
        when(transacaoRepository.findById(99))
                .thenReturn(Optional.empty());

        // confere se o service lança o erro esperado
        assertThrows(
                TransacaoNotFoundException.class,
                () -> transacaoService.buscar(99)
        );
    }

    @Test
    void deveFiltrarTransacoesPorTipo() {

        // cria duas transações para representar o resultado do filtro
        List<Transacao> transacoes = List.of(
                new Transacao(),
                new Transacao()
        );

        // define o retorno esperado do repository
        when(transacaoRepository.findByTipo(
                gestaofinanceira.project.enums.Tipo.DESPESA
        )).thenReturn(transacoes);

        // chama o filtro por tipo
        List<Transacao> resultado =
                transacaoService.filtrarPorTipo(
                        gestaofinanceira.project.enums.Tipo.DESPESA
                );

        // confere a quantidade retornada
        assertEquals(2, resultado.size());
    }
}