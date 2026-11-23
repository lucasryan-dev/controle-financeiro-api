package com.lucasdev.controle_financeiro.auth;

import com.lucasdev.controle_financeiro.exception.ConflitoException;
import com.lucasdev.controle_financeiro.exception.CredenciaisInvalidasException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService service;

    @Test
    void deveRegistrarUsuarioSemPrecisarDeToken() throws Exception {
        when(service.registrar(any(RegistroRequest.class)))
                .thenReturn(new UsuarioResponse(1L, "Ana Souza", "ana@email.com"));

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Ana Souza", "email": "ana@email.com", "senha": "senha12345" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveRetornar400QuandoEmailForInvalido() throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Ana Souza", "email": "email-invalido", "senha": "senha12345" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email").exists());

        verify(service, never()).registrar(any());
    }

    @Test
    void deveRetornar409QuandoEmailJaEstiverCadastrado() throws Exception {
        when(service.registrar(any(RegistroRequest.class)))
                .thenThrow(new ConflitoException("Já existe uma conta com esse e-mail"));

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Ana Souza", "email": "ana@email.com", "senha": "senha12345" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe uma conta com esse e-mail"));
    }

    @Test
    void deveFazerLoginEDevolverToken() throws Exception {
        when(service.login(any(LoginRequest.class)))
                .thenReturn(new TokenResponse("token-de-teste", "Bearer", Instant.now()));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "ana@email.com", "senha": "senha12345" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-teste"))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    void deveRetornar401QuandoCredenciaisForemInvalidas() throws Exception {
        when(service.login(any(LoginRequest.class)))
                .thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "ana@email.com", "senha": "senhaErrada" }
                                """))
                .andExpect(status().isUnauthorized());
    }
}