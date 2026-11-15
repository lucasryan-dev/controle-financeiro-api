package com.lucasdev.controle_financeiro.transacao;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoRequest(
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 100, message = "A descrição deve ter no maximo 100 caracteres")
        String descricao,

        @NotNull(message = "o valor é Obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "O valor deve ter no maximo duas casas decimais")
        BigDecimal valor,

        @NotNull(message = "O tipo é obrigatório")
        tipoTransacao tipo,

        @NotNull(message = "A categoria é obrigatória")
        categoria categoria,

        @NotNull(message = "A data é obrigaória")
        LocalDate data
) {
    public Transacao toEntity() {
        Transacao transacao = new Transacao();
        transacao.setDescricao(descricao);
        transacao.setValor(valor);
        transacao.setTipo(tipo);
        transacao.setCategoria(categoria);
        transacao.setData(data);

        return transacao;
    }
}
