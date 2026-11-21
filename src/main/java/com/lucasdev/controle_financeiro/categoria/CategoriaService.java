package com.lucasdev.controle_financeiro.categoria;

import com.lucasdev.controle_financeiro.exception.ConflitoException;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.lucasdev.controle_financeiro.transacao.TransacaoRepository;
import com.lucasdev.controle_financeiro.usuario.Usuario;
import com.lucasdev.controle_financeiro.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public CategoriaService(CategoriaRepository repository,
                            TransacaoRepository transacaoRepository,
                            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public CategoriaResponse criar(CategoriaRequest request, Long usuarioId) {
        if (repository.existsByNomeIgnoreCaseAndUsuarioId(request.nome(), usuarioId)) {
            throw new ConflitoException("Já existe uma categoria com esse nome");
        }
        Usuario dono = usuarioRepository.getReferenceById(usuarioId);
        Categoria salva = repository.save(request.toEntity(dono));
        return CategoriaResponse.from(salva);
    }

    public List<CategoriaResponse> listarTodas(Long usuarioId) {
        return repository.findAllByUsuarioIdOrderByNomeAsc(usuarioId)
                .stream()
                .map(CategoriaResponse::from)
                .toList();
    }

    public CategoriaResponse atualizar(Long id, CategoriaRequest request, Long usuarioId) {
        Categoria existente = buscarEntidade(id, usuarioId);

        if (repository.existsByNomeIgnoreCaseAndIdNotAndUsuarioId(request.nome(), id, usuarioId)) {
            throw new ConflitoException("Já existe uma categoria com esse nome");
        }

        existente.setNome(request.nome());
        existente.setGrupo(request.grupo());
        return CategoriaResponse.from(repository.save(existente));
    }

    public void excluir(Long id, Long usuarioId) {
        Categoria existente = buscarEntidade(id, usuarioId);

        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new ConflitoException("Categoria em uso por transações e não pode ser excluída");
        }

        repository.delete(existente);
    }

    private Categoria buscarEntidade(Long id, Long usuarioId) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));
    }
}
