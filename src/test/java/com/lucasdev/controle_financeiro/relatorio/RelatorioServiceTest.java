package com.lucasdev.controle_financeiro.relatorio;

import com.lucasdev.controle_financeiro.categoria.GrupoCategoria;
import com.lucasdev.controle_financeiro.transacao.TipoTransacao;
import com.lucasdev.controle_financeiro.transacao.TransacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    private static final Long USUARIO_ID = 1L;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    // ---------- resumo do mês ----------

    @Test
    void deveCalcularSaldoEPercentualGastoDoMes() {
        // DADO: receitas de 3.000,00 e despesas de 1.250,90
        when(transacaoRepository.somarPorTipoEPeriodo(eq(USUARIO_ID), eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoRepository.somarPorTipoEPeriodo(eq(USUARIO_ID), eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal("1250.90"));

        // QUANDO
        ResumoMensalResponse resumo = relatorioService.resumoMensal(2026, 11, USUARIO_ID);

        // ENTÃO
        assertThat(resumo.totalReceitas()).isEqualByComparingTo("3000.00");
        assertThat(resumo.totalDespesas()).isEqualByComparingTo("1250.90");
        assertThat(resumo.saldo()).isEqualByComparingTo("1749.10");
        assertThat(resumo.percentualGasto()).isEqualByComparingTo("41.70");
    }

    @Test
    void deveRetornarPercentualZeroQuandoNaoHaReceitas() {
        // DADO: nenhuma receita e despesas de 200,00
        when(transacaoRepository.somarPorTipoEPeriodo(eq(USUARIO_ID), eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transacaoRepository.somarPorTipoEPeriodo(eq(USUARIO_ID), eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal("200.00"));

        // QUANDO
        ResumoMensalResponse resumo = relatorioService.resumoMensal(2026, 11, USUARIO_ID);

        // ENTÃO: saldo negativo e percentual zero, sem erro
        assertThat(resumo.saldo()).isEqualByComparingTo("-200.00");
        assertThat(resumo.percentualGasto()).isEqualByComparingTo("0");
    }

    // ---------- dicas ----------

    @Test
    void deveGerarDicasComDesejosAcimaDoRecomendado() {
        // DADO: renda de 3.000, despesas de 2.350 (1.350 necessidades + 1.000 desejos)
        prepararCenario("3000.00", "2350.00", "1350.00", "1000.00");

        // QUANDO
        DicasResponse resposta = relatorioService.dicas(2026, 11, USUARIO_ID);

        // ENTÃO: os percentuais
        assertThat(resposta.necessidades().percentual()).isEqualByComparingTo("45.00");
        assertThat(resposta.desejos().percentual()).isEqualByComparingTo("33.33");
        assertThat(resposta.poupanca().percentual()).isEqualByComparingTo("21.67");

        // ENTÃO: as dicas
        assertThat(resposta.dicas()).hasSize(3);
        assertThat(resposta.dicas().get(0)).contains("necessidades estão dentro");
        assertThat(resposta.dicas().get(1)).contains("desejos").contains("acima");
        assertThat(resposta.dicas().get(2)).startsWith("Parabéns");
    }

    @Test
    void deveAvisarQuandoPoupaMenosDe20PorCento() {
        // DADO: renda de 3.000 e despesas de 2.750 (sobram 250, ou 8,33%)
        prepararCenario("3000.00", "2750.00", "1350.00", "1400.00");

        // QUANDO
        DicasResponse resposta = relatorioService.dicas(2026, 11, USUARIO_ID);

        // ENTÃO: a poupança é positiva, mas abaixo de 20%
        assertThat(resposta.poupanca().percentual()).isEqualByComparingTo("8.33");
        assertThat(resposta.dicas().get(2)).contains("menos que o recomendado");
    }

    @Test
    void deveAlertarQuandoPoupancaNegativa() {
        // DADO: renda de 3.000 e despesas de 3.250 (faltam 250)
        prepararCenario("3000.00", "3250.00", "1350.00", "1900.00");

        // QUANDO
        DicasResponse resposta = relatorioService.dicas(2026, 11, USUARIO_ID);

        // ENTÃO: a poupança é negativa e aparece o alerta
        assertThat(resposta.poupanca().valor()).isEqualByComparingTo("-250.00");
        assertThat(resposta.dicas().get(2)).startsWith("Alerta");
    }

    @Test
    void deveAvisarQuandoRendaZero() {
        // DADO: nenhuma receita, e despesas de 500
        prepararCenario("0", "500.00", "300.00", "200.00");

        // QUANDO
        DicasResponse resposta = relatorioService.dicas(2026, 11, USUARIO_ID);

        // ENTÃO: só uma dica, pedindo o cadastro das receitas
        assertThat(resposta.dicas()).hasSize(1);
        assertThat(resposta.dicas().get(0)).contains("Nenhuma receita");
    }

    // ---------- método auxiliar ----------

    /** Prepara os quatro valores que o método dicas consulta no banco. */
    private void prepararCenario(String renda, String despesas, String necessidades, String desejos) {
        when(transacaoRepository.somarPorTipoEPeriodo(eq(USUARIO_ID), eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(new BigDecimal(renda));
        when(transacaoRepository.somarPorTipoEPeriodo(eq(USUARIO_ID), eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal(despesas));
        when(transacaoRepository.somarPorGrupoEPeriodo(eq(USUARIO_ID), any(), eq(GrupoCategoria.NECESSIDADE), any(), any()))
                .thenReturn(new BigDecimal(necessidades));
        when(transacaoRepository.somarPorGrupoEPeriodo(eq(USUARIO_ID), any(), eq(GrupoCategoria.DESEJO), any(), any()))
                .thenReturn(new BigDecimal(desejos));
    }
}