package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class FavoritoId implements Serializable {

    private Long loginId;
    private Long veiculoId;

    public FavoritoId(Long loginId, Long veiculoId) {
        this.loginId = loginId;
        this.veiculoId = veiculoId;
    }
}