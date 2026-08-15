package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
@Getter
@Setter
@NoArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "busca_salva_id", nullable = false)
    private BuscaSalva buscaSalva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private boolean visualizado = false;

    public Alerta(BuscaSalva buscaSalva, Veiculo veiculo) {
        this.buscaSalva = buscaSalva;
        this.veiculo = veiculo;
    }

    @PrePersist
    protected void aoCriar() {
        criadoEm = LocalDateTime.now();
    }
}