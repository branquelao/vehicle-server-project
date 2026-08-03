package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorito")
@Getter
@Setter
@NoArgsConstructor
public class Favorito {

    @EmbeddedId
    private FavoritoId id = new FavoritoId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("loginId")
    @JoinColumn(name = "login_id")
    private Login login;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("veiculoId")
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Favorito(Login login, Veiculo veiculo) {
        this.login = login;
        this.veiculo = veiculo;
        this.id = new FavoritoId(login.getId(), veiculo.getId());
    }

    @PrePersist
    protected void aoCriar() {
        criadoEm = LocalDateTime.now();
    }
}