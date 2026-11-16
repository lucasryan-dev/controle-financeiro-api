package com.lucasdev.controle_financeiro.categoria;

import com.lucasdev.controle_financeiro.exception.ConflitoException;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.lucasdev.controle_financeiro.transacao.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final TransacaoRepository transacaoRepository;

    public CategoriaService(CategoriaRepository repository,
                            TransacaoRepository transacaoRepository) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
    }

    public CategoriaResponse criar (CategoriaRequest request){
        if(repository.existsByNomeIgnoreCase(request.nome())){
            throw new ConflitoException("Já existe uma categoria com esse nome");
        }

        Categoria salva = repository.save(request.toEntity());
        return CategoriaResponse.from(salva);
    }

    public List<CategoriaResponse> listarTodas(){
        return repository.findAll()
                .stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    private Categoria buscarid(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest request){
        Categoria existente = buscarid(id);

        if(repository.existsByNomeIgnoreCaseAndIdNot(request.nome(), id)) {
            throw new ConflitoException("Já existe uma categoria com esse nome");
        }

        existente.setNome(request.nome());
        return CategoriaResponse.from(repository.save(existente));
    }

    public void excluir(Long id){
        Categoria existente = buscarid(id);

        if(transacaoRepository.existsByCategoriaId(id)){
            throw new ConflitoException("Categoria em uso por transações e nao pode ser excluida");
        }

        repository.delete(existente);
    }

}
