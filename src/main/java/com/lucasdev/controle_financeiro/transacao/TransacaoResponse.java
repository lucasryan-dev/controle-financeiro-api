package com.lucasdev.controle_financeiro.transacao;

import com.lucasdev.controle_financeiro.categoria.CategoriaResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoTransacao tipo,
        CategoriaResponse categoria,
        LocalDate data
) {
    public static TransacaoResponse from(Transacao transacao){
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getTipo(),
                CategoriaResponse.from(transacao.getCategoria()),
                transacao.getData()
        );
    }
}
