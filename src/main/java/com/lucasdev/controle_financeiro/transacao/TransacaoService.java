package com.lucasdev.controle_financeiro.transacao;

import jakarta.persistence.Id;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public Transacao buscaporId(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transação não encontrada"));
    }

    public Transacao atualizar (Long id, Transacao dados){
        Transacao existente = buscaporId(id);

        existente.setDescricao(dados.getDescricao());
        existente.setValor(dados.getValor());
        existente.setTipo(dados.getTipo());
        existente.setCategoria(dados.getCategoria());
        existente.setData(dados.getData());

        return repository.save(existente);
    }

    public void excluir(Long id){
        Transacao existente = buscaporId(id);
        repository.delete(existente);
    }

}
