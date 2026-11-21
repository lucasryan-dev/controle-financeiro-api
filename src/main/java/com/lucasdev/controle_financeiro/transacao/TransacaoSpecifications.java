package com.lucasdev.controle_financeiro.transacao;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class TransacaoSpecifications {

    public static Specification<Transacao> comTipo(TipoTransacao tipo) {
        return (root, query, cb) ->
                tipo == null ? null : cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Transacao> comCategoria(Long categoriaId) {
        return (root, query, cb) ->
                categoriaId == null ? null : cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Transacao> aPartirDe(LocalDate inicio) {
        return (root, query, cb) ->
                inicio == null ? null : cb.greaterThanOrEqualTo(root.get("data"), inicio);
    }

    public static Specification<Transacao> ate(LocalDate fim) {
        return (root, query, cb) ->
                fim == null ? null : cb.lessThanOrEqualTo(root.get("data"), fim);
    }

    public static Specification<Transacao> doUsuario(Long usuarioId) {
        return (root, query, cb) -> cb.equal(root.get("usuario").get("id"), usuarioId);
    }
}