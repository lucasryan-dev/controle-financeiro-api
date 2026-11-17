package com.lucasdev.controle_financeiro.relatorio;

import java.math.BigDecimal;

public record ResumoMensalResponse(
        Integer ano,
        Integer mes,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        BigDecimal percentualGasto
) {
}
