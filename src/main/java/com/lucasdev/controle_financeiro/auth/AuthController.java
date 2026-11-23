package com.lucasdev.controle_financeiro.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Cadastro de usuários e login")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Cadastra um novo usuário")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request){
        return authService.registrar(request);
    }

    @Operation(summary = "Faz login e devolve o token JWT")
    @PostMapping("/login")
       public TokenResponse login(@Valid @RequestBody LoginRequest request){
        return authService.login(request);
    }
    }

