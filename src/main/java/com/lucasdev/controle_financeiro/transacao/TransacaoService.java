package com.lucasdev.controle_financeiro.transacao;

import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransacaoService {
    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    public TransacaoResponse criar(TransacaoRequest request){
        Transacao salva = repository.save(request.toEntity());
        return TransacaoResponse.from(salva);
    }

    public List<TransacaoResponse> listarTodas(){
        return repository.findAll()
                .stream()
                .map(TransacaoResponse::from)
                .toList();
    }

    public TransacaoResponse buscar(Long id){
        return TransacaoResponse.from(buscaporId(id));
    }

    private Transacao buscaporId(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada"));
    }

    public TransacaoResponse atualizar (Long id, TransacaoRequest dados){
        Transacao existente = buscaporId(id);

        existente.setDescricao(dados.descricao());
        existente.setValor(dados.valor());
        existente.setTipo(dados.tipo());
        existente.setCategoria(dados.categoria());
        existente.setData(dados.data());

        return TransacaoResponse.from(repository.save(existente));
    }

    public void excluir(Long id){
        Transacao existente = buscaporId(id);
        repository.delete(existente);
    }

}
