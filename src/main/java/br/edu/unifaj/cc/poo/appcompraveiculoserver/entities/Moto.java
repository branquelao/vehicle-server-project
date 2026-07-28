package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "moto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moto_nome", nullable = false, length = 50)
    private String motoNome;

    @Column(name = "moto_cor", nullable = false, length = 25)
    private String motoCor;

    @Column(name = "moto_ano", nullable = false)
    private int motoAno;

    @Column(name = "moto_valor", nullable = false)
    private float motoValor;

    @Column(name = "moto_imagem", nullable = false, length = 200)
    private String motoImagem;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "moto_anunciada_em", nullable = false)
    private LocalDateTime motoAnunciadaEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "moto_atualizada_em", nullable = false)
    private LocalDateTime motoAtualizadaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id", nullable = false)
    private Login login;

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        motoAnunciadaEm = agora;
        motoAtualizadaEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        motoAtualizadaEm = LocalDateTime.now();
    }
}