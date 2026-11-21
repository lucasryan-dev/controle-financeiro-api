package com.lucasdev.controle_financeiro.transacao;

import com.lucasdev.controle_financeiro.categoria.Categoria;
import com.lucasdev.controle_financeiro.categoria.CategoriaRepository;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.lucasdev.controle_financeiro.usuario.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public TransacaoService(TransacaoRepository repository,
                            CategoriaRepository categoriaRepository,
                            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public TransacaoResponse criar(TransacaoRequest request, Long usuarioId) {
        Categoria categoria = buscarCategoria(request.categoriaId(), usuarioId);

        Transacao transacao = request.toEntity(categoria);
        transacao.setUsuario(usuarioRepository.getReferenceById(usuarioId));

        return TransacaoResponse.from(repository.save(transacao));
    }

    public Page<TransacaoResponse> listar(TipoTransacao tipo, Long categoriaId,
                                          LocalDate inicio, LocalDate fim,
                                          Long usuarioId, Pageable pageable) {
        Specification<Transacao> filtros = Specification.allOf(
                TransacaoSpecifications.doUsuario(usuarioId),
                TransacaoSpecifications.comTipo(tipo),
                TransacaoSpecifications.comCategoria(categoriaId),
                TransacaoSpecifications.aPartirDe(inicio),
                TransacaoSpecifications.ate(fim)
        );

        return repository.findAll(filtros, pageable)
                .map(TransacaoResponse::from);
    }

    public TransacaoResponse buscar(Long id, Long usuarioId) {
        return TransacaoResponse.from(buscarEntidade(id, usuarioId));
    }

    public TransacaoResponse atualizar(Long id, TransacaoRequest dados, Long usuarioId) {
        Transacao existente = buscarEntidade(id, usuarioId);

        existente.setDescricao(dados.descricao());
        existente.setValor(dados.valor());
        existente.setTipo(dados.tipo());
        existente.setCategoria(buscarCategoria(dados.categoriaId(), usuarioId));
        existente.setData(dados.data());

        return TransacaoResponse.from(repository.save(existente));
    }

    public void excluir(Long id, Long usuarioId) {
        repository.delete(buscarEntidade(id, usuarioId));
    }

    private Transacao buscarEntidade(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada"));
    }

    private Categoria buscarCategoria(Long categoriaId, Long usuarioId) {
        return categoriaRepository.findByIdAndUsuarioId(categoriaId, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }
}
