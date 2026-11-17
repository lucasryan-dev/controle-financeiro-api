package com.lucasdev.controle_financeiro.relatorio;


import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
