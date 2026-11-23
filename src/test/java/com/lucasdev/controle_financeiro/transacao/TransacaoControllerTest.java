package com.lucasdev.controle_financeiro.transacao;

import com.lucasdev.controle_financeiro.auth.SecurityConfig;
import com.lucasdev.controle_financeiro.categoria.CategoriaResponse;
import com.lucasdev.controle_financeiro.categoria.GrupoCategoria;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransacaoController.class)
@Import(SecurityConfig.class)
public class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransacaoService service;

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/transacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveCriarTransacao() throws Exception {
        CategoriaResponse lazer = new CategoriaResponse(6L, "Lazer", GrupoCategoria.DESEJO);
        TransacaoResponse resposta = new TransacaoResponse(
                10L, "Cinema", new BigDecimal("50.00"), TipoTransacao.DESPESA,
                lazer, LocalDate.of(2026, 11, 15));

        when(service.criar(any(TransacaoRequest.class), eq(1L))).thenReturn(resposta);

        mockMvc.perform(post("/transacoes")
                        .with(jwt().jwt(token -> token.subject("1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "descricao": "Cinema",
                              "valor": 50.00,
                              "tipo": "DESPESA",
                              "categoriaId": 6,
                              "data": "2026-11-15"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.descricao").value("Cinema"))
                .andExpect(jsonPath("$.categoria.nome").value("Lazer"));
    }

    @Test
    void deveRetornar404QuandoTransacaoNaoExistir() throws Exception {
        when(service.buscar(99L, 1L))
                .thenThrow(new RecursoNaoEncontradoException("Transação não encontrada"));

        mockMvc.perform(get("/transacoes/99")
                        .with(jwt().jwt(token -> token.subject("1"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Transação não encontrada"));
    }

    @Test
    void deveRetornar400QuandoValorForNegativo() throws Exception {
        mockMvc.perform(post("/transacoes")
                        .with(jwt().jwt(token -> token.subject("1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "descricao": "Cinema",
                              "valor": -50.00,
                              "tipo": "DESPESA",
                              "categoriaId": 6,
                              "data": "2026-11-15"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.valor").exists());

        verify(service, never()).criar(any(), any());
    }
}
