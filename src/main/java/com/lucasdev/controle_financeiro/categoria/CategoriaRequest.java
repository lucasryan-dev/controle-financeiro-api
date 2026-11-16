package com.lucasdev.controle_financeiro.categoria;
import jakarta.validation.constraints.*;

public record CategoriaRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
        String nome
) {
    public Categoria toEntity() {
        return new Categoria(nome);
    }
}

