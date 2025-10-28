package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carro {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carroNome", length = 50, nullable = false, unique = false)
    private String carroNome;

    @Column(name = "carroCor", length = 25, nullable = false, unique = false)
    private String carroCor;

    @Column(name = "carroAno", nullable = false, unique = false)
    private int carroAno;

    @Column(name = "carroValor", nullable = false, unique = false)
    private float carroValor;

    @Column(name = "carroImagem", length = 100, nullable = false, unique = false)
    private String carroImagem;
}
