package com.lucasdev.controle_financeiro.relatorio;

import java.math.BigDecimal;

public record IndicadorGrupo(
        BigDecimal valor,
        BigDecimal percentual,
        int recomendado
) {
}
