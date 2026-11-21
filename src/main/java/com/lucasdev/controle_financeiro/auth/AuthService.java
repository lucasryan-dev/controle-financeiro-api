package com.lucasdev.controle_financeiro.auth;


import com.lucasdev.controle_financeiro.categoria.CategoriasIniciais;
import com.lucasdev.controle_financeiro.exception.ConflitoException;
import com.lucasdev.controle_financeiro.exception.CredenciaisInvalidasException;
import com.lucasdev.controle_financeiro.usuario.Usuario;
import com.lucasdev.controle_financeiro.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final CategoriasIniciais categoriasIniciais;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, TokenService tokenService, CategoriasIniciais categoriasIniciais) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.categoriasIniciais = categoriasIniciais;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request){
        String email = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)){
            throw new ConflitoException("Já existe uma conta com esse e-mail");
        }

        String senhaCriptografada = passwordEncoder.encode(request.senha());
        Usuario salvo = usuarioRepository.save(new Usuario(request.nome(), email, senhaCriptografada));

        categoriasIniciais.criarPara(salvo);

        return UsuarioResponse.from(salvo);
    }

    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        return tokenService.gerar(usuario);
    }
}
