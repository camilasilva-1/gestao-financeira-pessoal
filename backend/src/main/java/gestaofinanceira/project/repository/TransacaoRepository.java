package gestaofinanceira.project.repository;

import gestaofinanceira.project.enums.Tipo;
import gestaofinanceira.project.model.Categoria;
import gestaofinanceira.project.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Integer> {

    // Buscar transações por categoria e tipo (usado no OrcamentoService)
    List<Transacao> findByCategoriaAndTipo(Categoria categoria, Tipo tipo);

    // ============================
    // MÉTODOS COM INTERVALO DE DATAS
    // ============================

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t " +
           "WHERE t.tipo = 'RECEITA' AND t.usuario.idUsuario = :usuarioId " +
           "AND t.dataTransacao BETWEEN :inicio AND :fim")
    BigDecimal somaReceitasPorPeriodo(@Param("usuarioId") Long usuarioId,
                                      @Param("inicio") LocalDate inicio,
                                      @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t " +
           "WHERE t.tipo = 'DESPESA' AND t.usuario.idUsuario = :usuarioId " +
           "AND t.dataTransacao BETWEEN :inicio AND :fim")
    BigDecimal somaDespesasPorPeriodo(@Param("usuarioId") Long usuarioId,
                                      @Param("inicio") LocalDate inicio,
                                      @Param("fim") LocalDate fim);

    @Query("SELECT t.categoria.nome, SUM(t.valor) FROM Transacao t " +
           "WHERE t.tipo = 'DESPESA' AND t.usuario.idUsuario = :usuarioId " +
           "AND t.dataTransacao BETWEEN :inicio AND :fim GROUP BY t.categoria.nome")
    List<Object[]> gastosPorCategoria(@Param("usuarioId") Long usuarioId,
                                      @Param("inicio") LocalDate inicio,
                                      @Param("fim") LocalDate fim);

    @Query("SELECT FUNCTION('MONTH', t.dataTransacao), " +
           "SUM(CASE WHEN t.tipo = 'RECEITA' THEN t.valor ELSE 0 END), " +
           "SUM(CASE WHEN t.tipo = 'DESPESA' THEN t.valor ELSE 0 END) " +
           "FROM Transacao t WHERE t.usuario.idUsuario = :usuarioId " +
           "GROUP BY FUNCTION('MONTH', t.dataTransacao) " +
           "ORDER BY FUNCTION('MONTH', t.dataTransacao)")
    List<Object[]> evolucaoMensal(@Param("usuarioId") Long usuarioId);

    // ============================
    // MÉTODOS SEM INTERVALO (GERAL)
    // ============================

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t " +
           "WHERE t.tipo = 'RECEITA' AND t.usuario.idUsuario = :usuarioId")
    BigDecimal somaReceitas(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM Transacao t " +
           "WHERE t.tipo = 'DESPESA' AND t.usuario.idUsuario = :usuarioId")
    BigDecimal somaDespesas(@Param("usuarioId") Long usuarioId);

    @Query("SELECT t.categoria.nome, SUM(t.valor) FROM Transacao t " +
           "WHERE t.tipo = 'DESPESA' AND t.usuario.idUsuario = :usuarioId " +
           "GROUP BY t.categoria.nome")
    List<Object[]> gastosPorCategoriaGeral(@Param("usuarioId") Long usuarioId);

    List<Transacao> findByCategoriaAndTipoAndDataTransacaoBetween(
            Categoria categoria,
            Tipo tipo,
            LocalDate inicio,
            LocalDate fim);

}
