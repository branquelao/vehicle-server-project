package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loginNome", length = 100, nullable = false, unique = false)
    private String nome;

    @Column(name = "loginSenha", length = 30, nullable = false, unique = false)
    private String senha;

    @Column(name = "loginTelefone", nullable = false, unique = false)
    private int telefone;

    @Column(name = "loginCarteira", nullable = false, unique = false)
    private int carteira;

    @Column(name = "loginImagem", length = 100, nullable = false, unique = false)
    private String loginImagem;
}
