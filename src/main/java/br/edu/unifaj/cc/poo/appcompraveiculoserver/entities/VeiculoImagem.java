package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "veiculo_imagem")
@Getter
@Setter
@NoArgsConstructor
public class VeiculoImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @Column(name = "url_imagem", nullable = false, length = 200)
    private String urlImagem;

    @Column(nullable = false)
    private boolean principal = false;

    @Column(nullable = false)
    private int ordem = 0;

    public VeiculoImagem(Veiculo veiculo, String urlImagem, boolean principal, int ordem) {
        this.veiculo = veiculo;
        this.urlImagem = urlImagem;
        this.principal = principal;
        this.ordem = ordem;
    }
}