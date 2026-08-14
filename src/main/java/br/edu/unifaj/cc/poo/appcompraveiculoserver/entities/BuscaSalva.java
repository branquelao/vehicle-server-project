package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoVeiculo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "busca_salva")
@Getter
@Setter
@NoArgsConstructor
public class BuscaSalva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id", nullable = false)
    private Login login;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TipoVeiculo tipo;

    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(name = "preco_min")
    private Float precoMin;

    @Column(name = "preco_max")
    private Float precoMax;

    @Column(name = "ano_min")
    private Integer anoMin;

    @Column(name = "ano_max")
    private Integer anoMax;

    @Column(name = "km_max")
    private Integer kmMax;

    @Column(length = 25)
    private String cor;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @PrePersist
    protected void aoCriar() {
        criadaEm = LocalDateTime.now();
    }
}