package gestaofinanceira.project.service;

import gestaofinanceira.project.exception.TransacaoNotFoundException;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.repository.CategoriaRepository;
import gestaofinanceira.project.repository.TransacaoRepository;
import gestaofinanceira.project.repository.UsuarioRepository;

import gestaofinanceira.project.dto.TransacaoDTO;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Usuario;
import gestaofinanceira.project.model.Categoria;
import java.time.LocalDate;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
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

    @Test
    void naoPermiteTransacaoSemData() {
        TransacaoDTO dto = new TransacaoDTO();
        dto.setDescricao("Teste");
        dto.setValor(20.0);
        dto.setTipo("RECEITA");
        dto.setUsuarioId(1);
        assertThrows(IllegalArgumentException.class, () -> transacaoService.adicionarDTO(dto));
    }

    @Test
    void permiteReceitaSemCategoriaComCamposOpcionais() {
        TransacaoDTO dto = new TransacaoDTO();
        dto.setDescricao("Salario");
        dto.setValor(200.0);
        dto.setTipo("RECEITA");
        dto.setUsuarioId(1);
        dto.setDataTransacao(LocalDate.of(2026, 11, 8));
        dto.setEstabelecimento("Empresa");
        dto.setRecorrencia(true);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(a -> a.getArgument(0));

        Transacao salva = transacaoService.adicionarDTO(dto);
        assertEquals(null, salva.getCategoria());
        assertEquals("Empresa", salva.getEstabelecimento());
        assertEquals(true, salva.getRecorrencia());
    }

    @Test
    void exigeCategoriaParaDespesa() {
        TransacaoDTO dto = new TransacaoDTO();
        dto.setDescricao("Almoco");
        dto.setValor(40.0);
        dto.setTipo("DESPESA");
        dto.setUsuarioId(1);
        dto.setDataTransacao(LocalDate.of(2026, 11, 8));
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        assertThrows(IllegalArgumentException.class, () -> transacaoService.adicionarDTO(dto));
    }


    @Test
    void rejeitaValorZero() {
        Transacao t = new Transacao();
        t.setDescricao("Teste");
        t.setValor(java.math.BigDecimal.ZERO);
        t.setTipo(Tipo.RECEITA);
        t.setDataTransacao(LocalDate.of(2026, 11, 8));
        assertThrows(IllegalArgumentException.class, () -> transacaoService.adicionar(t));
    }

    @Test
    void rejeitaValorNegativo() {
        Transacao t = new Transacao();
        t.setDescricao("Teste");
        t.setValor(new java.math.BigDecimal("-1"));
        t.setTipo(Tipo.RECEITA);
        t.setDataTransacao(LocalDate.of(2026, 11, 8));
        assertThrows(IllegalArgumentException.class, () -> transacaoService.adicionar(t));
    }

    @Test
    void rejeitaDescricaoEmBranco() {
        Transacao t = new Transacao();
        t.setDescricao("   ");
        t.setValor(new java.math.BigDecimal("5"));
        t.setTipo(Tipo.RECEITA);
        t.setDataTransacao(LocalDate.of(2026, 11, 8));
        assertThrows(IllegalArgumentException.class, () -> transacaoService.adicionar(t));
    }

    @Test
    void rejeitaDespesaSemCategoria() {
        Transacao t = new Transacao();
        t.setDescricao("Despesa");
        t.setValor(new java.math.BigDecimal("5"));
        t.setTipo(Tipo.DESPESA);
        t.setDataTransacao(LocalDate.of(2026, 11, 8));
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        t.setUsuario(usuario);
        assertThrows(IllegalArgumentException.class, () -> transacaoService.adicionar(t));
    }

    @Test
    void naoEditaTransacaoDeOutroUsuario() {
        Usuario proprietario = new Usuario();
        proprietario.setIdUsuario(1);
        Transacao existente = new Transacao();
        existente.setUsuario(proprietario);
        when(transacaoRepository.findById(9)).thenReturn(Optional.of(existente));
        assertThrows(RuntimeException.class, () -> transacaoService.editar(9, 2, new Transacao()));
    }

    @Test
    void naoExcluiTransacaoDeOutroUsuario() {
        Usuario proprietario = new Usuario();
        proprietario.setIdUsuario(1);
        Transacao existente = new Transacao();
        existente.setUsuario(proprietario);
        when(transacaoRepository.findById(9)).thenReturn(Optional.of(existente));
        assertThrows(RuntimeException.class, () -> transacaoService.remover(9, 2));
    }

}
