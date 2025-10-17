package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {
    private Integer id;
    private String nome;
    private String senha;
    private int telefone;
    private int carteira;
}
