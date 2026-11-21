package com.lucasdev.controle_financeiro.transacao;

import com.lucasdev.controle_financeiro.categoria.Categoria;
import com.lucasdev.controle_financeiro.categoria.CategoriaRepository;
import com.lucasdev.controle_financeiro.categoria.GrupoCategoria;
import com.lucasdev.controle_financeiro.exception.RecursoNaoEncontradoException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private TransacaoService transacaoService;

    @Test
    void naoDeveCriarTransacaoComCategoriaInexistente() {
        // DADO: a categoria 99 não existe
        TransacaoRequest request = new TransacaoRequest(
                "Mercado", new BigDecimal("450.90"), TipoTransacao.DESPESA, 99L, LocalDate.of(2026, 11, 10));
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        // QUANDO / ENTÃO
        assertThatThrownBy(() -> transacaoService.criar(request))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        // ENTÃO: nada foi salvo
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveCriarTransacaoComCategoriaExistente() {
        // DADO: a categoria 1 existe, e o banco devolve a transação com id
        TransacaoRequest request = new TransacaoRequest(
                "Mercado", new BigDecimal("450.90"), TipoTransacao.DESPESA, 1L, LocalDate.of(2026, 11, 10));

        Categoria alimentacao = new Categoria("Alimentação", GrupoCategoria.NECESSIDADE);
        alimentacao.setId(1L);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(alimentacao));

        when(transacaoRepository.save(any())).thenAnswer(invocacao -> {
            Transacao transacao = invocacao.getArgument(0);
            transacao.setId(10L);
            return transacao;
        });

        // QUANDO
        TransacaoResponse resposta = transacaoService.criar(request);

        // ENTÃO
        assertThat(resposta.id()).isEqualTo(10L);
        assertThat(resposta.descricao()).isEqualTo("Mercado");
        assertThat(resposta.categoria().nome()).isEqualTo("Alimentação");
    }

    @Test
    void deveLancarNaoEncontradoAoBuscarTransacaoInexistente() {

        when(transacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transacaoService.buscar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveAtualizarTransacaoMantendoOId() {

        Categoria alimentacao = new Categoria("Alimentação", GrupoCategoria.NECESSIDADE);
        alimentacao.setId(1L);
        Transacao existente = new Transacao();
        existente.setId(5L);
        existente.setDescricao("Mercado");
        existente.setValor(new BigDecimal("450.90"));
        existente.setTipo(TipoTransacao.DESPESA);
        existente.setCategoria(alimentacao);
        existente.setData(LocalDate.of(2026, 11, 10));
        when(transacaoRepository.findById(5L)).thenReturn(Optional.of(existente));


        Categoria transporte = new Categoria("Transporte", GrupoCategoria.NECESSIDADE);
        transporte.setId(2L);
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(transporte));
        when(transacaoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        TransacaoRequest dados = new TransacaoRequest(
                "Uber", new BigDecimal("25.00"), TipoTransacao.DESPESA, 2L, LocalDate.of(2026, 11, 12));


        TransacaoResponse resposta = transacaoService.atualizar(5L, dados);

        assertThat(resposta.id()).isEqualTo(5L);
        assertThat(resposta.descricao()).isEqualTo("Uber");
        assertThat(resposta.valor()).isEqualByComparingTo("25.00");
        assertThat(resposta.categoria().nome()).isEqualTo("Transporte");
    }
}