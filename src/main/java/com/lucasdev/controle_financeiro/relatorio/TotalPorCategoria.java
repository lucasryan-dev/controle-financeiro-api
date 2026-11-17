package com.lucasdev.controle_financeiro.relatorio;

import java.math.BigDecimal;

public record TotalPorCategoria(
        String categoria,
        BigDecimal total
) {
}
