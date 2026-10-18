package gestaofinanceira.project.service;
import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Orcamento;
import gestaofinanceira.project.model.Transacao;
import gestaofinanceira.project.repository.OrcamentoRepository;
import gestaofinanceira.project.repository.TransacaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import gestaofinanceira.project.repository.UsuarioRepository;
import gestaofinanceira.project.repository.CategoriaRepository; 
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.time.LocalDate; 

@Service 

public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public OrcamentoService(OrcamentoRepository orcamentoRepository,
        TransacaoRepository transacaoRepository,UsuarioRepository usuarioRepository,
        CategoriaRepository categoriaRepository) {

    this.orcamentoRepository = orcamentoRepository;
    this.transacaoRepository = transacaoRepository;
    this.usuarioRepository = usuarioRepository;
    this.categoriaRepository = categoriaRepository;
}

    // Listar todos os orçamentos
    public List<Orcamento> listar() {
        return orcamentoRepository.findAll();
    }

    // Buscar orçamento por Id
    public Orcamento buscar(int id) {
    return orcamentoRepository.findById(id)
            .orElseThrow(() ->
                    new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Orçamento não encontrado"));
    }

    // Adicionar um novo orçamento
    public Orcamento adicionar(Orcamento orcamento) {
        validarOrcamento(orcamento);
        return orcamentoRepository.save(orcamento);
    }

    // Editar um orçamento que já existe
    public Orcamento editar(int id, Orcamento novo) {

        Orcamento existente = buscar(id);

        validarOrcamento(novo);

        // Atualiza os campos
        existente.setLimiteValor(novo.getLimiteValor());
        existente.setUsuario(novo.getUsuario());
        existente.setCategoria(novo.getCategoria());

        return orcamentoRepository.save(existente);
    }

    // Remover orçamento
    public void remover(int id) {

        Orcamento existente = buscar(id);

        orcamentoRepository.delete(existente);
    }

   // Calcula o total gasto da categoria vinculada ao orçamento
   // considerando somente o mês atual
 public BigDecimal calcularGasto(Integer idOrcamento) {

     Orcamento orcamento = buscar(idOrcamento);

     LocalDate hoje = LocalDate.now();
     LocalDate inicioMes = hoje.withDayOfMonth(1);
     LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

     List<Transacao> despesas =
             transacaoRepository.findByCategoriaAndTipoAndDataTransacaoBetween(
                     orcamento.getCategoria(),
                     Tipo.DESPESA,
                     inicioMes,
                     fimMes
             );
     BigDecimal totalGasto = BigDecimal.ZERO;

     for (Transacao transacao : despesas) {
         totalGasto = totalGasto.add(transacao.getValor());
     }
     return totalGasto;
   }

    // Calcula quanto resta do limite definido para a categoria
    public BigDecimal calcularSaldoRestante(Integer idOrcamento) {

    Orcamento orcamento = buscar(idOrcamento);

    BigDecimal totalGasto = calcularGasto(idOrcamento);

    return orcamento.getLimiteValor().subtract(totalGasto);
    }

    // Calcula o percentual de consumo do orçamento
    public BigDecimal calcularPercentual(Integer idOrcamento) {

    Orcamento orcamento = buscar(idOrcamento);

    BigDecimal totalGasto = calcularGasto(idOrcamento);

    return totalGasto
            .multiply(BigDecimal.valueOf(100))
            .divide(orcamento.getLimiteValor(), 2, RoundingMode.HALF_UP);
    }


    private void validarOrcamento(Orcamento orcamento) {

    if (orcamento.getLimiteValor() == null ||
            orcamento.getLimiteValor().compareTo(BigDecimal.ZERO) <= 0) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O limite do orçamento deve ser maior que zero");
    }

    if (orcamento.getUsuario() == null) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Usuário é obrigatório");
    }

    Integer idUsuario = orcamento.getUsuario().getIdUsuario();

    if (idUsuario == null) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Usuário é obrigatório");
    }

    if (!usuarioRepository.existsById(idUsuario)) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Usuário não encontrado");
    }

    if (orcamento.getCategoria() == null) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Categoria é obrigatória");
    }

    Integer idCategoria = orcamento.getCategoria().getIdCategoria();

    if (idCategoria == null) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Categoria é obrigatória");
    }

    if (!categoriaRepository.existsById(idCategoria)) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Categoria não encontrada");
    }
    
    }

}
    

