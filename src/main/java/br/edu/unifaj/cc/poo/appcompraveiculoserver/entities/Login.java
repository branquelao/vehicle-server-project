package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario", length = 100, nullable = false, unique = false)
    private String usuario;

    @Column(name = "senha", length = 30, nullable = false, unique = false)
    private String senha;

    @Column(name = "telefone", nullable = false, unique = false)
    private String telefone;

    @Column(name = "carteira", nullable = false, unique = false)
    private float carteira;

    @Column(name = "loginImagem", length = 100, nullable = true, unique = false)
    private String loginImagem;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime loginCriadoEm;

    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime loginAtualizadoEm;

    @OneToMany(mappedBy = "login", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Carro> carros = new ArrayList<>();

    @OneToMany(mappedBy = "login", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Moto> motos = new ArrayList<>();
}
