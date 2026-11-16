package com.lucasdev.controle_financeiro.transacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    boolean existsByCategoriaId(Long categoriaId);
    List<Transacao> id(Long id);


}
