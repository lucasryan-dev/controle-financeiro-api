package com.lucasdev.controle_financeiro.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request){
        return authService.registrar(request);
    }

    @PostMapping("/login")
       public TokenResponse login(@Valid @RequestBody LoginRequest request){
        return authService.login(request);
    }
    }

