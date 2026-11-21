package com.lucasdev.controle_financeiro.categoria;

import com.lucasdev.controle_financeiro.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "nome"}))
@Getter
@Setter
@NoArgsConstructor
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
    @Column(nullable = false, length = 50)
   private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrupoCategoria grupo;

    public Categoria(String nome, GrupoCategoria grupo, Usuario usuario) {
        this.nome = nome;
        this.grupo = grupo;
        this.usuario = usuario;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}

