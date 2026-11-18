package com.lucasdev.controle_financeiro.transacao;

import com.lucasdev.controle_financeiro.categoria.Categoria;
import com.lucasdev.controle_financeiro.categoria.CategoriaRepository;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
public class TransacaoService {
    private final TransacaoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(TransacaoRepository repository,
                            CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    public TransacaoResponse criar(TransacaoRequest request){
        Categoria categoria = buscarCategoria(request.categoriaId());
        Transacao salva = repository.save(request.toEntity(categoria));
        return TransacaoResponse.from(salva);
    }

    public Page<TransacaoResponse> listar(TipoTransacao tipo, Long categoriaId,
                                          LocalDate inicio, LocalDate fim,
                                          Pageable pageable) {
        Specification<Transacao> filtros = Specification.allOf(
                TransacaoSpecifications.comTipo(tipo),
                TransacaoSpecifications.comCategoria(categoriaId),
                TransacaoSpecifications.aPartirDe(inicio),
                TransacaoSpecifications.ate(fim)
        );

        return repository.findAll(filtros, pageable)
                .map(TransacaoResponse::from);
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
        existente.setCategoria(buscarCategoria(dados.categoriaId()));
        existente.setData(dados.data());

        return TransacaoResponse.from(repository.save(existente));
    }

    public void excluir(Long id){
        Transacao existente = buscaporId(id);
        repository.delete(existente);
    }

    private Categoria buscarCategoria(Long id){
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));

    }
}
