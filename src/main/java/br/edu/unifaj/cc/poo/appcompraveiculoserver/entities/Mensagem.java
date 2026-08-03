package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagem")
@Getter
@Setter
@NoArgsConstructor
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Login remetente;

    @Column(nullable = false, length = 1000)
    private String conteudo;

    @Column(name = "enviada_em", nullable = false)
    private LocalDateTime enviadaEm;

    @Column(nullable = false)
    private boolean lida = false;

    public Mensagem(Conversa conversa, Login remetente, String conteudo) {
        this.conversa = conversa;
        this.remetente = remetente;
        this.conteudo = conteudo;
    }

    @PrePersist
    protected void aoCriar() {
        enviadaEm = LocalDateTime.now();
    }
}