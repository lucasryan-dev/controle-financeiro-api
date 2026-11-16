package com.lucasdev.controle_financeiro.categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lucasdev.controle_financeiro.categoria.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
