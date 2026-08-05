package br.edu.unifaj.cc.poo.appcompraveiculoserver.specification;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoFiltroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class VeiculoSpecification {

    private VeiculoSpecification() {}

    public static Specification<Veiculo> comFiltros(VeiculoFiltroDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtro.getTipo() != null) {
                predicates.add(cb.equal(root.get("tipo"), filtro.getTipo()));
            }
            if (filtro.getMarca() != null && !filtro.getMarca().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("marca")), "%" + filtro.getMarca().toLowerCase() + "%"));
            }
            if (filtro.getModelo() != null && !filtro.getModelo().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("modelo")), "%" + filtro.getModelo().toLowerCase() + "%"));
            }
            if (filtro.getPrecoMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("valor"), filtro.getPrecoMin()));
            }
            if (filtro.getPrecoMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("valor"), filtro.getPrecoMax()));
            }
            if (filtro.getAnoMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("anoModelo"), filtro.getAnoMin()));
            }
            if (filtro.getAnoMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("anoModelo"), filtro.getAnoMax()));
            }
            if (filtro.getKmMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("km"), filtro.getKmMax()));
            }
            if (filtro.getCor() != null && !filtro.getCor().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("cor")), filtro.getCor().toLowerCase()));
            }
            if (filtro.getCidade() != null && !filtro.getCidade().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("cidade")), filtro.getCidade().toLowerCase()));
            }
            if (filtro.getEstado() != null && !filtro.getEstado().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("estado")), filtro.getEstado().toUpperCase()));
            }
            predicates.add(cb.equal(root.get("status"),
                    filtro.getStatus() != null ? filtro.getStatus() : StatusAnuncio.ATIVO));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}