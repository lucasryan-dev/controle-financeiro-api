package com.lucasdev.controle_financeiro.relatorio;

import com.lucasdev.controle_financeiro.categoria.GrupoCategoria;
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

    public DiscasResponse dicas(Integer ano, Integer mes){
        YearMonth periodo = YearMonth.of(ano, mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();

        BigDecimal renda = transacaoRepository.somarPorTipoEPeriodo(TipoTransacao.RECEITA, inicio, fim);
        BigDecimal totalDespesas = transacaoRepository.somarPorTipoEPeriodo(TipoTransacao.DESPESA, inicio, fim);
        BigDecimal necessidades = transacaoRepository
                .somarPorGrupoEPeriodo(TipoTransacao.DESPESA, GrupoCategoria.NECESSIDADE, inicio, fim);
        BigDecimal desejos =  transacaoRepository
                .somarPorGrupoEPeriodo(TipoTransacao.DESPESA, GrupoCategoria.DESEJO, inicio, fim);
        BigDecimal poupanca = renda.subtract(totalDespesas);

        IndicadorGrupo indicadorNecessidades =
                new IndicadorGrupo(necessidades, calcularPercentual(necessidades, renda), 50);
        IndicadorGrupo indicadorDesejos =
                new IndicadorGrupo(desejos, calcularPercentual(desejos, renda), 30);
        IndicadorGrupo indicadorPoupanca = new IndicadorGrupo(poupanca, calcularPercentual(poupanca, renda), 20);

        List<String> dicas = gerarDicas(renda,
                indicadorNecessidades.percentual(),
                indicadorDesejos.percentual(),
                indicadorPoupanca.percentual());

        return new DiscasResponse(indicadorNecessidades, indicadorDesejos, indicadorPoupanca, dicas);
    }

    private List<String> gerarDicas(BigDecimal renda,
                                    BigDecimal percentualNecessidades,
                                    BigDecimal percentualDesejos,
                                    BigDecimal percentualPoupanca){
        List<String> dicas = new ArrayList<>();

        if (renda.compareTo(BigDecimal.ZERO) == 0){
            dicas.add("Nenhuma receita cadastrada neste mês. Cadastre suas receitas para receber as dicas.");
            return dicas;
        }

        if (percentualNecessidades.compareTo(BigDecimal.valueOf(50)) > 0) {
            dicas.add("Atenção: seus gastos com necessidades estão acima dos 50% recomendados. Revise contas fixas como aluguel, planos e assinaturas.");
        }else {
            dicas.add("Seus gastos com necessidades estão dentro do recomendado.");
        }

        if (percentualDesejos.compareTo(BigDecimal.valueOf(30)) > 0){
            dicas.add("Atenção: seus gastos com desejos estao acima dos 30% recomendados. Revise seus gastos para nao faltar com suas necessidades.");
        }else {
            dicas.add("Seus gastos com desejos estão dentro do recomendado.");
        }

        if (percentualPoupanca.compareTo(BigDecimal.valueOf(20)) >= 0 ){
            dicas.add("Parabéns! Vocẽ está poupando pelo menos 20% da sua renda. Vocẽ está no caminho certo.");
        } else if (percentualPoupanca.compareTo(BigDecimal.ZERO) >= 0){
            dicas.add("Vocẽ está poupando, mas menos que o recomendado. Tente reduzir alguns gastos com desejos para guardar mais.");
        }else {
            dicas.add("Alerta: neste mês você gastou mais do que ganhou. Revise seus gastos com urgência para não entrar em dívidas.");
        }

        return dicas;
    }

}
