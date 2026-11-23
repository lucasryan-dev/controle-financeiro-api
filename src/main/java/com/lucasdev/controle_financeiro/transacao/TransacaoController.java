package com.lucasdev.controle_financeiro.transacao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Tag(name = "Transações", description = "Cadastro, consulta e filtros de receitas e despesas")
@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra uma nova transação")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponse criar(@Valid @RequestBody TransacaoRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.criar(request, usuarioId(jwt));
    }

    @Operation(summary = "Lista as transações com filtros por tipo, categoria e período, com paginação")
    @GetMapping
    public PagedModel<TransacaoResponse> listar(
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @ParameterObject @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal Jwt jwt) {
        return new PagedModel<>(service.listar(tipo, categoriaId, inicio, fim, usuarioId(jwt), pageable));
    }

    @Operation(summary = "Busca uma transação pelo id")
    @GetMapping("/{id}")
    public TransacaoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.buscar(id, usuarioId(jwt));
    }

    @Operation(summary = "Atualiza uma transação")
    @PutMapping("/{id}")
    public TransacaoResponse atualizar(@PathVariable Long id,
                                       @Valid @RequestBody TransacaoRequest dados,
                                       @AuthenticationPrincipal Jwt jwt) {
        return service.atualizar(id, dados, usuarioId(jwt));
    }

    @Operation(summary = "Exclui uma transação")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        service.excluir(id, usuarioId(jwt));
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}