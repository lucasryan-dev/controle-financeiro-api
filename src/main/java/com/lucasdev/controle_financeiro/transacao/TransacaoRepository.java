package com.lucasdev.controle_financeiro.transacao;

import com.lucasdev.controle_financeiro.relatorio.TotalPorCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    boolean existsByCategoriaId(Long categoriaId);
    List<Transacao> id(Long id);

    @Query("""
        SELECT COALESCE(SUM(t.valor), 0)
        FROM Transacao t
        WHERE t.tipo = :tipo
          AND t.data BETWEEN :inicio AND :fim
        """)
    BigDecimal somarPorTipoEPeriodo(@Param("tipo") TipoTransacao tipo,
                                    @Param("inicio") LocalDate inicio,
                                    @Param("fim") LocalDate fim);

    @Query("""
        SELECT new com.lucasdev.controle_financeiro.relatorio.TotalPorCategoria(c.nome, SUM(t.valor))
        FROM Transacao t
        JOIN t.categoria c
        WHERE t.tipo = :tipo
          AND t.data BETWEEN :inicio AND :fim
        GROUP BY c.nome
        ORDER BY SUM(t.valor) DESC
        """)
    List<TotalPorCategoria> somarPorCategoria(@Param("tipo") TipoTransacao tipo,
                                              @Param("inicio") LocalDate inicio,
                                              @Param("fim") LocalDate fim);

    TipoTransacao Tipo(TipoTransacao tipo);
}
