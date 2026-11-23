package com.lucasdev.controle_financeiro.relatorio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@Tag(name = "Relatórios", description = "Resumo mensal, gastos por categoria e dicas pela regra 50/30/20")
@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @Operation(summary = "Mostra receitas, despesas, saldo e percentual gasto no mês")
    @GetMapping("/resumo")
    public ResumoMensalResponse resumo(@RequestParam Integer ano,
                                       @RequestParam Integer mes,
                                       @AuthenticationPrincipal Jwt jwt) {
        return relatorioService.resumoMensal(ano, mes, usuarioId(jwt));
    }

    @Operation(summary = "Mostra quanto foi gasto em cada categoria no mês")
    @GetMapping("/gastos-por-categoria")
    public List<GastoPorCategoriaResponse> gastosPorCategoria(@RequestParam Integer ano,
                                                              @RequestParam Integer mes,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return relatorioService.gastosPorCategoria(ano, mes, usuarioId(jwt));
    }

    @Operation(summary = "Gera dicas de saúde financeira com base na regra 50/30/20")
    @GetMapping("/dicas")
    public DicasResponse dicas(@RequestParam Integer ano,
                               @RequestParam Integer mes,
                               @AuthenticationPrincipal Jwt jwt) {
        return relatorioService.dicas(ano, mes, usuarioId(jwt));
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
