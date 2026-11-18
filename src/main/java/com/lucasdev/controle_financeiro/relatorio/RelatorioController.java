package com.lucasdev.controle_financeiro.relatorio;

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
                                       @RequestParam Integer mes){
        return relatorioService.resumoMensal(ano, mes);
    }

    @GetMapping("/gastos-por-categoria")
    public List<GastoPorCategoriaResponse> gastosPorCategoria(@RequestParam Integer ano,
                                                              @RequestParam Integer mes){
        return relatorioService.gastosPorCategoria(ano, mes);
    }

    @GetMapping("/dicas")
    public DicasResponse dicas(@RequestParam Integer ano,
                               @RequestParam Integer mes){
        return relatorioService.dicas(ano, mes);

    }
}
