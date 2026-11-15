package com.lucasdev.controle_financeiro.transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        tipoTransacao tipo,
        categoria categoria,
        LocalDate data
) {
    public static TransacaoResponse from(Transacao transacao){
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getTipo(),
                transacao.getCategoria(),
                transacao.getData()
        );
    }
}
