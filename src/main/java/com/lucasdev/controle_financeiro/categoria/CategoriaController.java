package com.lucasdev.controle_financeiro.categoria;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Categorias", description = "Categorias do usuário, classificadas em necessidade, desejo ou renda")
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra uma nova categoria")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@Valid @RequestBody CategoriaRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.criar(request, usuarioId(jwt));
    }

    @Operation(summary = "Lista as categorias do usuário em ordem alfabética")
    @GetMapping
    public List<CategoriaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listarTodas(usuarioId(jwt));
    }

    @Operation(summary = "Atualiza o nome e o grupo de uma categoria")
    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable Long id,
                                       @Valid @RequestBody CategoriaRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return service.atualizar(id, request, usuarioId(jwt));
    }

    @Operation(summary = "Exclui uma categoria que não está em uso")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.excluir(id, usuarioId(jwt));
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
