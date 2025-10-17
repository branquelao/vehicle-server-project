package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carro {
    private int id;
    private String carroNome;
    private String carroCor;
    private int carroAno;
    private float carroValor;
}
