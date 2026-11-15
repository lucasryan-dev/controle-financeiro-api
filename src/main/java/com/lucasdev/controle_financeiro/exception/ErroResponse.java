package com.lucasdev.controle_financeiro.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponse(
        int status,
        String mensagem,
        Map<String, String> campos,
        LocalDateTime timnestamp
) {
    public ErroResponse(int status, String mensagem){
        this(status, mensagem, null, LocalDateTime.now());
    }


}
