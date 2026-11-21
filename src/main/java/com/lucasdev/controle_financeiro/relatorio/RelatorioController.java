package com.lucasdev.controle_financeiro.relatorio;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/resumo")
    public ResumoMensalResponse resumo(@RequestParam Integer ano,
                                       @RequestParam Integer mes,
                                       @AuthenticationPrincipal Jwt jwt) {
        return relatorioService.resumoMensal(ano, mes, usuarioId(jwt));
    }

    @GetMapping("/gastos-por-categoria")
    public List<GastoPorCategoriaResponse> gastosPorCategoria(@RequestParam Integer ano,
                                                              @RequestParam Integer mes,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return relatorioService.gastosPorCategoria(ano, mes, usuarioId(jwt));
    }

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
