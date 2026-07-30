package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "opcional")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Opcional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;
}