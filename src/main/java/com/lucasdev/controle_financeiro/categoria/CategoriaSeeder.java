package com.lucasdev.controle_financeiro.categoria;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoriaSeeder implements CommandLineRunner {

    private final CategoriaRepository repository;

    public CategoriaSeeder(CategoriaRepository repository) {
        this.repository = repository;
    }


    @Override
    public void run(String... args) {
        if (repository.count() > 0){
            return;
        }

        repository.saveAll(List.of(
                new Categoria("Alimentação", GrupoCategoria.NECESSIDADE),
                new Categoria("Transporte", GrupoCategoria.NECESSIDADE),
                new Categoria("Moradia", GrupoCategoria.NECESSIDADE),
                new Categoria("Saúde", GrupoCategoria.NECESSIDADE),
                new Categoria("Educação", GrupoCategoria.NECESSIDADE),
                new Categoria("Lazer", GrupoCategoria.DESEJO),
                new Categoria("Salário", GrupoCategoria.RENDA),
                new Categoria("Outros", GrupoCategoria.DESEJO)
                ));
    }

}
