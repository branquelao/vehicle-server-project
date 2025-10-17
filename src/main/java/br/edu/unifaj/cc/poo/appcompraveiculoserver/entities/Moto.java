package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moto {
    private int id;
    private String motoNome;
    private String motoCor;
    private int motoAno;
    private float motoValor;
}
