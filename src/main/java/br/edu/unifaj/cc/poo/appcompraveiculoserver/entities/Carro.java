package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "carro")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "carro_nome", nullable = false, length = 50)
    private String carroNome;

    @Column(name = "carro_cor", nullable = false, length = 25)
    private String carroCor;

    @Column(name = "carro_ano", nullable = false)
    private int carroAno;

    @Column(name = "carro_valor", nullable = false)
    private float carroValor;

    @Column(name = "carro_imagem", nullable = false, length = 200)
    private String carroImagem;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "carro_anunciado_em", nullable = false)
    private LocalDateTime carroAnunciadoEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "carro_atualizado_em", nullable = false)
    private LocalDateTime carroAtualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id", nullable = false)
    private Login login;

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        carroAnunciadoEm = agora;
        carroAtualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        carroAtualizadoEm = LocalDateTime.now();
    }
}