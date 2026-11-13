package com.lucasdev.controle_financeiro.transacao;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransacaoService {
    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    public Transacao criar(Transacao transacao){
        return repository.save(transacao);
    }

    public List<Transacao> listarTodas(){
        return repository.findAll();
    }


}
