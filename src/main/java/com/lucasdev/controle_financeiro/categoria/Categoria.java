package com.lucasdev.controle_financeiro.categoria;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
    @Column(nullable = false, unique = true, length = 50)
   private String nome;

    public Categoria(String nome) {
        this.nome = nome;
    }
}

