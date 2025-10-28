package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motoNome", length = 50, nullable = false, unique = false)
    private String motoNome;

    @Column(name = "motoCor", length = 25, nullable = false, unique = false)
    private String motoCor;

    @Column(name = "motoAno", nullable = false, unique = false)
    private int motoAno;

    @Column(name = "motoValor", nullable = false, unique = false)
    private float motoValor;

    @Column(name = "motoImagem", length = 100, nullable = false, unique = false)
    private String motoImagem;
}
