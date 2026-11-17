package com.lucasdev.controle_financeiro.relatorio;

import java.math.BigDecimal;

public record GastoPorCategoriaResponse(
        String categoria,
        BigDecimal total,
        BigDecimal percentual
) {
}
