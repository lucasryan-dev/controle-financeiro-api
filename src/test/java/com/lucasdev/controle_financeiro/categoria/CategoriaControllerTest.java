package com.lucasdev.controle_financeiro.categoria;

import com.lucasdev.controle_financeiro.auth.SecurityConfig;
import com.lucasdev.controle_financeiro.exception.ConflitoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.List;

import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@WebMvcTest(CategoriaController.class)
@Import(SecurityConfig.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService service;

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/categorias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveListarCategoriasDoUsuarioLogado() throws Exception {
        when(service.listarTodas(1L)).thenReturn(List.of(
                new CategoriaResponse(1L, "Alimentação", GrupoCategoria.NECESSIDADE)));

        mockMvc.perform(get("/categorias")
                        .with(jwt().jwt(token -> token.subject("1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Alimentação"))
                .andExpect(jsonPath("$[0].grupo").value("NECESSIDADE"));
    }

    @Test
    void deveRetornar400QuandoNomeEstiverVazio() throws Exception {
        mockMvc.perform(post("/categorias")
                        .with(jwt().jwt(token -> token.subject("1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "nome": "", "grupo": "DESEJO" }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists());

        verify(service, never()).criar(any(), any());
    }

    @Test
    void deveRetornar409QuandoNomeJaExistir() throws Exception {
        when(service.criar(any(CategoriaRequest.class), eq(1L)))
                .thenThrow(new ConflitoException("Já existe uma categoria com esse"));
    }
}

