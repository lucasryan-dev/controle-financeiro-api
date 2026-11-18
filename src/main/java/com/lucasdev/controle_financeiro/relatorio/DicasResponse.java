package com.lucasdev.controle_financeiro.relatorio;

import java.util.List;

public record DicasResponse(
        IndicadorGrupo necessidades,
        IndicadorGrupo desejos,
        IndicadorGrupo poupanca,
        List<String> dicas
) {
}
