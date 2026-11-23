package com.lucasdev.controle_financeiro.relatorio;

import com.lucasdev.controle_financeiro.auth.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RelatorioController.class)
@Import(SecurityConfig.class)
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RelatorioService service;

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/relatorios/resumo")
                        .param("ano", "2026")
                        .param("mes", "11"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarResumoDoMes() throws Exception {
        when(service.resumoMensal(2026, 11, 1L)).thenReturn(new ResumoMensalResponse(
                2026, 11,
                new BigDecimal("3000.00"),
                new BigDecimal("1650.00"),
                new BigDecimal("1350.00"),
                new BigDecimal("55.00")));

        mockMvc.perform(get("/relatorios/resumo")
                        .param("ano", "2026")
                        .param("mes", "11")
                        .with(jwt().jwt(token -> token.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(1350.00))
                .andExpect(jsonPath("$.percentualGasto").value(55.00));
    }

    @Test
    void deveRetornarDicasDoMes() throws Exception {
        when(service.dicas(2026, 11, 1L)).thenReturn(new DicasResponse(
                new IndicadorGrupo(new BigDecimal("1200.00"), new BigDecimal("40.00"), 50),
                new IndicadorGrupo(new BigDecimal("450.00"), new BigDecimal("15.00"), 30),
                new IndicadorGrupo(new BigDecimal("1350.00"), new BigDecimal("45.00"), 20),
                List.of("Parabéns! Você está poupando mais de 20% da sua renda.")));

        mockMvc.perform(get("/relatorios/dicas")
                        .param("ano", "2026")
                        .param("mes", "11")
                        .with(jwt().jwt(token -> token.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.necessidades.recomendado").value(50))
                .andExpect(jsonPath("$.dicas[0]").value("Parabéns! Você está poupando mais de 20% da sua renda."));
    }

    @Test
    void deveRetornar400QuandoMesNaoForNumero() throws Exception {
        mockMvc.perform(get("/relatorios/resumo")
                        .param("ano", "2026")
                        .param("mes", "abc")
                        .with(jwt().jwt(token -> token.subject("1"))))
                .andExpect(status().isBadRequest());

        verify(service, never()).resumoMensal(anyInt(), anyInt(), anyLong());
    }

    @Test
    void deveRetornar400QuandoFaltarOMes() throws Exception {
        mockMvc.perform(get("/relatorios/resumo")
                        .param("ano", "2026")
                        .with(jwt().jwt(token -> token.subject("1"))))
                .andExpect(status().isBadRequest());

        verify(service, never()).resumoMensal(anyInt(), anyInt(), anyLong());
    }
}
