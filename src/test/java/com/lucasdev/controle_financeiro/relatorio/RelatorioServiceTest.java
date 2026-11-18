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

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void deveCalcularSaldoEPercentualGastoDoMes() {

        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal("1250.90"));

        ResumoMensalResponse resumo = relatorioService.resumoMensal(2026, 11);

        assertThat(resumo.totalReceitas()).isEqualByComparingTo("3000.00");
        assertThat(resumo.totalDespesas()).isEqualByComparingTo("1250.90");
        assertThat(resumo.saldo()).isEqualByComparingTo("1749.10");
        assertThat(resumo.percentualGasto()).isEqualByComparingTo("41.70");
    }

    @Test
    void deveRetornarPercentualZeroQuandoNaoHaReceitas(){
        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal("200.00"));

        ResumoMensalResponse resumo = relatorioService.resumoMensal(2026, 11);

        assertThat(resumo.saldo()).isEqualByComparingTo("-200.00");
        assertThat(resumo.percentualGasto()).isEqualByComparingTo("0");
    }

    @Test
    void deveGerarDicasComDesejosAcimaDoRecomendado() {
        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal("2350.00"));
        when(transacaoRepository.somarPorGrupoEPeriodo(any(), eq(GrupoCategoria.NECESSIDADE), any(), any()))
                .thenReturn(new BigDecimal("1350.00"));
        when(transacaoRepository.somarPorGrupoEPeriodo(any(), eq(GrupoCategoria.DESEJO), any(), any()))
                .thenReturn(new BigDecimal("1000.00"));


        DiscasResponse resposta = relatorioService.dicas(2026, 11);

        assertThat(resposta.necessidades().percentual()).isEqualByComparingTo("45.00");
        assertThat(resposta.desejos().percentual()).isEqualByComparingTo("33.33");
        assertThat(resposta.poupanca().percentual()).isEqualByComparingTo("21.67");

        assertThat(resposta.dicas()).hasSize(3);
        assertThat(resposta.dicas().get(0)).contains("necessidades estão dentro");
        assertThat(resposta.dicas().get(1)).contains("desejos").contains("acima");
        assertThat(resposta.dicas().get(2)).startsWith("Parabéns");
    }

    @Test
    void deveAvisarQuandoPoupaMenosDe20PorCento() {

        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal("2750.00"));
        when(transacaoRepository.somarPorGrupoEPeriodo(any(), eq(GrupoCategoria.NECESSIDADE), any(), any()))
                .thenReturn(new BigDecimal("1350.00"));
        when(transacaoRepository.somarPorGrupoEPeriodo(any(), eq(GrupoCategoria.DESEJO), any(), any()))
                .thenReturn(new BigDecimal("1400.00"));


        DiscasResponse resposta = relatorioService.dicas(2026, 11);

        assertThat(resposta.poupanca().percentual()).isEqualByComparingTo("8.33");
        assertThat(resposta.dicas().get(2)).contains("menos que o recomendado");
    }

    @Test
    void deveAlertarQuandoPoupancaNegativa() {

        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(new BigDecimal("3000.00"));
        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal("3250.00"));
        when(transacaoRepository.somarPorGrupoEPeriodo(any(), eq(GrupoCategoria.NECESSIDADE), any(), any()))
                .thenReturn(new BigDecimal("1350.00"));
        when(transacaoRepository.somarPorGrupoEPeriodo(any(), eq(GrupoCategoria.DESEJO), any(), any()))
                .thenReturn(new BigDecimal("1900.00"));


        DiscasResponse resposta = relatorioService.dicas(2026, 11);

        assertThat(resposta.poupanca().valor()).isEqualByComparingTo("-250.00");
        assertThat(resposta.dicas().get(2)).startsWith("Alerta");
    }

    @Test
    void deveAvisarQuandoRendaZero() {

        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(transacaoRepository.somarPorTipoEPeriodo(eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(new BigDecimal("500.00"));
        when(transacaoRepository.somarPorGrupoEPeriodo(any(), eq(GrupoCategoria.NECESSIDADE), any(), any()))
                .thenReturn(new BigDecimal("300.00"));
        when(transacaoRepository.somarPorGrupoEPeriodo(any(), eq(GrupoCategoria.DESEJO), any(), any()))
                .thenReturn(new BigDecimal("200.00"));

        DiscasResponse resposta = relatorioService.dicas(2026, 11);


        assertThat(resposta.dicas()).hasSize(1);
        assertThat(resposta.dicas().get(0)).contains("Nenhuma receita");
    }
}