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
                new Categoria("Alimentação"),
                new Categoria("Transporte"),
                new Categoria("Moradia"),
                new Categoria("Saúde"),
                new Categoria("Educação"),
                new Categoria("Lazer"),
                new Categoria("Salário"),
                new Categoria("Outros")
                ));
    }

}
