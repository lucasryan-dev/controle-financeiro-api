package com.lucasdev.controle_financeiro.categoria;

import com.lucasdev.controle_financeiro.exception.ConflitoException;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
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


}
