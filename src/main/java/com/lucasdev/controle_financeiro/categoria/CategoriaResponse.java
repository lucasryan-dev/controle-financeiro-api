package com.lucasdev.controle_financeiro.categoria;

public record CategoriaResponse(
        Long id,
        String nome
) {

    public static CategoriaResponse from(Categoria categoria){
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome());

    }
}
