package com.lucasdev.controle_financeiro.auth;

import com.lucasdev.controle_financeiro.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expiracaoHoras;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${jwt.expiracao-horas}") long expiracaoHoras) {
        this.jwtEncoder = jwtEncoder;
        this.expiracaoHoras = expiracaoHoras;
    }

    public TokenResponse gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(expiracaoHoras, ChronoUnit.HOURS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("controle-financeiro")
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(cabecalho, claims))
                .getTokenValue();

        return new TokenResponse(token, "Bearer", expiraEm);
    }
}
