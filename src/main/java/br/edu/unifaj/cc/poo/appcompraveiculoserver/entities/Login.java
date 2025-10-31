package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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
    private String telefone;

    @Column(name = "loginCarteira", nullable = false, unique = false)
    private float carteira;

    @Column(name = "loginImagem", length = 100, nullable = false, unique = false)
    private String loginImagem;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime loginCriadoEm;

    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime loginAtualizadoEm;
}
