package com.lucasdev.controle_financeiro.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNomeIgnoreCaseAndUsuarioId(String nome, Long usuarioId);

    boolean existsByNomeIgnoreCaseAndIdNotAndUsuarioId(String nome, Long id, Long usuarioId);

    Optional<Categoria> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Categoria> findAllByUsuarioIdOrderByNomeAsc(Long usuarioId);
}
