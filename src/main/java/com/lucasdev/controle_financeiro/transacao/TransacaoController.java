package com.lucasdev.controle_financeiro.transacao;

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
    public Transacao criar(@RequestBody Transacao transacao) {
        return service.criar(transacao);
 }
    @GetMapping
    public List<Transacao> listar(){
        return service.listarTodas();
    }
}