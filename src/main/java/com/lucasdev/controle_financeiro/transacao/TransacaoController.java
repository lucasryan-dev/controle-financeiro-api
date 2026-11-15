package com.lucasdev.controle_financeiro.transacao;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public List<TransacaoResponse> listar(){
        return service.listarTodas();
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