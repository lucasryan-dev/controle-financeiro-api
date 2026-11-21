package com.lucasdev.controle_financeiro.categoria;

import com.lucasdev.controle_financeiro.usuario.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoriasIniciais {

    private final CategoriaRepository repository;

    public CategoriasIniciais(CategoriaRepository repository) {
        this.repository = repository;
    }

    public void criarPara(Usuario usuario) {
        repository.saveAll(List.of(
                new Categoria("Alimentação", GrupoCategoria.NECESSIDADE, usuario),
                new Categoria("Transporte", GrupoCategoria.NECESSIDADE, usuario),
                new Categoria("Moradia", GrupoCategoria.NECESSIDADE, usuario),
                new Categoria("Saúde", GrupoCategoria.NECESSIDADE, usuario),
                new Categoria("Educação", GrupoCategoria.NECESSIDADE, usuario),
                new Categoria("Lazer", GrupoCategoria.DESEJO, usuario),
                new Categoria("Outros", GrupoCategoria.DESEJO, usuario),
                new Categoria("Salário", GrupoCategoria.RENDA, usuario)
        ));
    }
}
