package com.lucasdev.controle_financeiro.transacao;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService service;


    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponse criar(@Valid @RequestBody TransacaoRequest request) {
        return service.criar(request);
 }


    @GetMapping
    public PagedModel<TransacaoResponse> listar(
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @PageableDefault(size = 20, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(service.listar(tipo, categoriaId, inicio, fim, pageable));
    }

    @GetMapping("/{id}")
    public TransacaoResponse buscar(@PathVariable Long id){
        return service.buscar(id);
    }

    @PutMapping("/{id}")
    public TransacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TransacaoRequest dados){
        return service.atualizar(id,dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir (@PathVariable Long id){
        service.excluir(id);
    }
}