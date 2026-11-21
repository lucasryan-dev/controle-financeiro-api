package com.lucasdev.controle_financeiro.auth;

import java.time.Instant;

public record TokenResponse(String token, String tipo, Instant expiraEm){
}
