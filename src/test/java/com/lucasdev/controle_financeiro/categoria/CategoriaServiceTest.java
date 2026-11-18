package com.lucasdev.controle_financeiro.categoria;

import com.lucasdev.controle_financeiro.exception.ConflitoException;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.lucasdev.controle_financeiro.transacao.TransacaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void naoDeveCriarCategoriaComNomeRepetido() {
        CategoriaRequest request = new CategoriaRequest("Lazer", GrupoCategoria.DESEJO);
        when(categoriaRepository.existsByNomeIgnoreCase("Lazer")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.criar(request))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe uma categoria com esse nome");

        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void deveCriarCategoriaQuandoNomeNaoExiste() {
        CategoriaRequest request = new CategoriaRequest("Pet", GrupoCategoria.NECESSIDADE);
        when(categoriaRepository.existsByNomeIgnoreCase("Pet")).thenReturn(false);

        Categoria salva = new Categoria("Pet", GrupoCategoria.NECESSIDADE);
        salva.setId(9L);
        when(categoriaRepository.save(any())).thenReturn(salva);


        CategoriaResponse resposta = categoriaService.criar(request);

        assertThat(resposta.id()).isEqualTo(9L);
        assertThat(resposta.nome()).isEqualTo("Pet");
        verify(categoriaRepository).save(any());
    }

    @Test
    void NaoDeveExcluirCategoeiaEmUso(){
        Categoria lazer = new Categoria("Lazer", GrupoCategoria.DESEJO);
        when(categoriaRepository.findById(6L)).thenReturn(Optional.of(lazer));
        when(transacaoRepository.existsByCategoriaId(6L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.excluir(6L))
                .isInstanceOf(ConflitoException.class);

        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void DeveLancarNaoEncontradoAoExcluirCategoriaInexistente(){
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(categoriaRepository, never()).delete(any());
    }
}