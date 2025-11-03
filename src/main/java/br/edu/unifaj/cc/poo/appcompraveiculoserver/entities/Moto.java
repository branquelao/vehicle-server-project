package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class Moto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "motoNome", length = 50, nullable = false, unique = false)
    private String motoNome;

    @Column(name = "motoCor", length = 25, nullable = false, unique = false)
    private String motoCor;

    @Column(name = "motoAno", nullable = false, unique = false)
    private int motoAno;

    @Column(name = "motoValor", nullable = false, unique = false)
    private float motoValor;

    @Column(name = "motoImagem", length = 100, nullable = true, unique = false)
    private String motoImagem;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime motoAnunciadaEm;

    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime motoAtualizadaEm;

    @ManyToOne
    @JoinColumn(name = "login_id")
    @JsonBackReference
    private Login login;
}
