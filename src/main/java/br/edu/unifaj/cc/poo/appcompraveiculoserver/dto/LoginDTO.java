package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    private String nome;
    private String senha;
    private String telefone;
    private float carteira;

    //Arquivo da Imagem
    private String loginImagem;
}
