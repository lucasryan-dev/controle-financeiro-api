package com.lucasdev.controle_financeiro.categoria;

import com.lucasdev.controle_financeiro.exception.ConflitoException;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.lucasdev.controle_financeiro.transacao.TransacaoRepository;
import com.lucasdev.controle_financeiro.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    private static final Long USUARIO_ID = 1L;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void naoDeveCriarCategoriaComNomeRepetido() {
        // DADO: o usuário já tem uma categoria com esse nome
        CategoriaRequest request = new CategoriaRequest("Lazer", GrupoCategoria.DESEJO);
        when(categoriaRepository.existsByNomeIgnoreCaseAndUsuarioId("Lazer", USUARIO_ID)).thenReturn(true);

        // QUANDO / ENTÃO: lança o conflito
        assertThatThrownBy(() -> categoriaService.criar(request, USUARIO_ID))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe uma categoria com esse nome");

        // ENTÃO: nada foi salvo
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void deveCriarCategoriaQuandoNomeNaoExiste() {
        // DADO: o nome ainda não existe para o usuário, e o banco devolve a categoria com id
        CategoriaRequest request = new CategoriaRequest("Pet", GrupoCategoria.NECESSIDADE);
        when(categoriaRepository.existsByNomeIgnoreCaseAndUsuarioId("Pet", USUARIO_ID)).thenReturn(false);

        Categoria salva = new Categoria("Pet", GrupoCategoria.NECESSIDADE, null);
        salva.setId(9L);
        when(categoriaRepository.save(any())).thenReturn(salva);

        // QUANDO
        CategoriaResponse resposta = categoriaService.criar(request, USUARIO_ID);

        // ENTÃO
        assertThat(resposta.id()).isEqualTo(9L);
        assertThat(resposta.nome()).isEqualTo("Pet");
        verify(categoriaRepository).save(any());
    }

    @Test
    void naoDeveExcluirCategoriaEmUso() {
        // DADO: a categoria 6 é do usuário...
        Categoria lazer = new Categoria("Lazer", GrupoCategoria.DESEJO, null);
        when(categoriaRepository.findByIdAndUsuarioId(6L, USUARIO_ID)).thenReturn(Optional.of(lazer));

        // ...e está em uso por alguma transação
        when(transacaoRepository.existsByCategoriaId(6L)).thenReturn(true);

        // QUANDO / ENTÃO: lança o conflito
        assertThatThrownBy(() -> categoriaService.excluir(6L, USUARIO_ID))
                .isInstanceOf(ConflitoException.class);

        // ENTÃO: a categoria não foi apagada
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void deveLancarNaoEncontradoAoExcluirCategoriaInexistente() {
        // DADO: a categoria 99 não existe para o usuário (ou é de outra pessoa)
        when(categoriaRepository.findByIdAndUsuarioId(99L, USUARIO_ID)).thenReturn(Optional.empty());

        // QUANDO / ENTÃO: lança o "não encontrado"
        assertThatThrownBy(() -> categoriaService.excluir(99L, USUARIO_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        // ENTÃO: nada foi apagado
        verify(categoriaRepository, never()).delete(any());
    }
}