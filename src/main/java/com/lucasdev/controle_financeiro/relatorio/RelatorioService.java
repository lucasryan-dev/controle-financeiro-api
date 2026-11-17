package com.lucasdev.controle_financeiro.relatorio;

import com.lucasdev.controle_financeiro.transacao.TipoTransacao;
import com.lucasdev.controle_financeiro.transacao.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class RelatorioService {
    private final TransacaoRepository transacaoRepository;

    public RelatorioService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public ResumoMensalResponse resumoMensal(Integer ano, Integer mes) {
        YearMonth periodo = YearMonth.of(ano, mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();

        BigDecimal receitas = transacaoRepository
                .somarPorTipoEPeriodo(TipoTransacao.RECEITA, inicio, fim);
        BigDecimal despesas = transacaoRepository.somarPorTipoEPeriodo(TipoTransacao.DESPESA, inicio, fim);

        BigDecimal saldo = receitas.subtract(despesas);
        BigDecimal percentualGasto = calcularPercentual(despesas, receitas);

        return  new ResumoMensalResponse(ano, mes, receitas, despesas, saldo, percentualGasto);
    }

    private BigDecimal calcularPercentual(BigDecimal parte, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal multiplicado = parte.multiply(BigDecimal.valueOf(100));
        BigDecimal resultado = multiplicado.divide(total, 2, RoundingMode.HALF_UP);
        return resultado;
    }

    public List<GastoPorCategoriaResponse> gastosPorCategoria(Integer ano, Integer mes){
        YearMonth periodo = YearMonth.of(ano, mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();

        List<TotalPorCategoria> totais = transacaoRepository
                .somarPorCategoria(TipoTransacao.DESPESA, inicio, fim);

        BigDecimal totalGeral = transacaoRepository
                .somarPorTipoEPeriodo(TipoTransacao.DESPESA, inicio, fim);

        List<GastoPorCategoriaResponse> resposta = new ArrayList<>();
        for (TotalPorCategoria item : totais){
            BigDecimal percentual = calcularPercentual(item.total(), totalGeral);
            resposta.add(new GastoPorCategoriaResponse(item.categoria(), item.total(), percentual));
        }

        return resposta;
    }

}
