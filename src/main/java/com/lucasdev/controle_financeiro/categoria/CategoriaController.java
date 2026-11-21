package com.lucasdev.controle_financeiro.categoria;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@Valid @RequestBody CategoriaRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.criar(request, usuarioId(jwt));
    }

    @GetMapping
    public List<CategoriaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listarTodas(usuarioId(jwt));
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable Long id,
                                       @Valid @RequestBody CategoriaRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return service.atualizar(id, request, usuarioId(jwt));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.excluir(id, usuarioId(jwt));
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
