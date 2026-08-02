package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "upload")
@Getter
@Setter
@NoArgsConstructor
public class Upload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_original")
    private String nomeOriginal;

    @Column(name = "nome_gerado", nullable = false, unique = true)
    private String nomeGerado;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id")
    private Login login;

    public Upload(String nomeOriginal, String nomeGerado, Login login) {
        this.nomeOriginal = nomeOriginal;
        this.nomeGerado = nomeGerado;
        this.login = login;
    }

    @PrePersist
    protected void aoCriar() {
        enviadoEm = LocalDateTime.now();
    }
}