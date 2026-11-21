package com.lucasdev.controle_financeiro.transacao;

import com.lucasdev.controle_financeiro.categoria.Categoria;
import com.lucasdev.controle_financeiro.categoria.CategoriaRepository;
import com.lucasdev.controle_financeiro.categoria.GrupoCategoria;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.lucasdev.controle_financeiro.usuario.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    private static final Long USUARIO_ID = 1L;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    @Test
    void naoDeveCriarTransacaoComCategoriaInexistente() {
        // DADO: a categoria 99 não existe
        TransacaoRequest request = new TransacaoRequest(
                "Mercado", new BigDecimal("450.90"), TipoTransacao.DESPESA, 99L, LocalDate.of(2026, 11, 10));
        when(categoriaRepository.findByIdAndUsuarioId(99L, USUARIO_ID)).thenReturn(Optional.empty());

        // QUANDO / ENTÃO
        assertThatThrownBy(() -> transacaoService.criar(request, USUARIO_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        // ENTÃO: nada foi salvo
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarTransacaoComCategoriaDeOutroUsuario() {
        // DADO: a categoria 1 existe, mas é de outro usuário.
        // Para o usuário logado, a busca filtrada pelo dono volta vazia.
        TransacaoRequest request = new TransacaoRequest(
                "Mercado", new BigDecimal("450.90"), TipoTransacao.DESPESA, 1L, LocalDate.of(2026, 11, 10));
        when(categoriaRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.empty());

        // QUANDO / ENTÃO: responde como se a categoria não existisse
        assertThatThrownBy(() -> transacaoService.criar(request, USUARIO_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria não encontrada");

        // ENTÃO: nada foi salvo
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveCriarTransacaoComCategoriaExistente() {
        // DADO: a categoria 1 é do usuário, e o banco devolve a transação com id
        TransacaoRequest request = new TransacaoRequest(
                "Mercado", new BigDecimal("450.90"), TipoTransacao.DESPESA, 1L, LocalDate.of(2026, 11, 10));

        Categoria alimentacao = new Categoria("Alimentação", GrupoCategoria.NECESSIDADE, null);
        alimentacao.setId(1L);
        when(categoriaRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(alimentacao));

        when(transacaoRepository.save(any())).thenAnswer(invocacao -> {
            Transacao transacao = invocacao.getArgument(0);
            transacao.setId(10L);
            return transacao;
        });

        // QUANDO
        TransacaoResponse resposta = transacaoService.criar(request, USUARIO_ID);

        // ENTÃO
        assertThat(resposta.id()).isEqualTo(10L);
        assertThat(resposta.descricao()).isEqualTo("Mercado");
        assertThat(resposta.categoria().nome()).isEqualTo("Alimentação");
    }

    @Test
    void deveLancarNaoEncontradoAoBuscarTransacaoInexistente() {
        // DADO: a transação 99 não existe para o usuário (ou é de outra pessoa)
        when(transacaoRepository.findByIdAndUsuarioId(99L, USUARIO_ID)).thenReturn(Optional.empty());

        // QUANDO / ENTÃO
        assertThatThrownBy(() -> transacaoService.buscar(99L, USUARIO_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveAtualizarTransacaoMantendoOId() {
        // DADO: a transação 5 é do usuário, com os dados antigos
        Categoria alimentacao = new Categoria("Alimentação", GrupoCategoria.NECESSIDADE, null);
        alimentacao.setId(1L);
        Transacao existente = new Transacao();
        existente.setId(5L);
        existente.setDescricao("Mercado");
        existente.setValor(new BigDecimal("450.90"));
        existente.setTipo(TipoTransacao.DESPESA);
        existente.setCategoria(alimentacao);
        existente.setData(LocalDate.of(2026, 11, 10));
        when(transacaoRepository.findByIdAndUsuarioId(5L, USUARIO_ID)).thenReturn(Optional.of(existente));

        // ...e os dados novos trocam a categoria para Transporte
        Categoria transporte = new Categoria("Transporte", GrupoCategoria.NECESSIDADE, null);
        transporte.setId(2L);
        when(categoriaRepository.findByIdAndUsuarioId(2L, USUARIO_ID)).thenReturn(Optional.of(transporte));
        when(transacaoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        TransacaoRequest dados = new TransacaoRequest(
                "Uber", new BigDecimal("25.00"), TipoTransacao.DESPESA, 2L, LocalDate.of(2026, 11, 12));

        // QUANDO
        TransacaoResponse resposta = transacaoService.atualizar(5L, dados, USUARIO_ID);

        // ENTÃO: os dados mudaram, mas o id continua o mesmo
        assertThat(resposta.id()).isEqualTo(5L);
        assertThat(resposta.descricao()).isEqualTo("Uber");
        assertThat(resposta.valor()).isEqualByComparingTo("25.00");
        assertThat(resposta.categoria().nome()).isEqualTo("Transporte");
    }
}