package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "veiculo")
@Getter
@Setter
@NoArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoVeiculo tipo;

    // Campos comuns
    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(name = "ano_fabricacao", nullable = false)
    private int anoFabricacao;

    @Column(name = "ano_modelo", nullable = false)
    private int anoModelo;

    @Column(nullable = false)
    private int km;

    @Column(nullable = false, length = 25)
    private String cor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Combustivel combustivel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Cambio cambio;

    @Column(name = "unico_dono", nullable = false)
    private boolean unicoDono = false;

    @Column(name = "aceita_troca", nullable = false)
    private boolean aceitaTroca = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conservacao", nullable = false, length = 15)
    private EstadoConservacao estadoConservacao;

    @Column(nullable = false)
    private float valor;

    @Column(length = 1000)
    private String descricao;

    // Específicos de carro
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Carroceria carroceria;

    private Integer portas;

    @Column(name = "potencia_cv")
    private Integer potenciaCv;

    @Column(name = "cilindrada_carro", length = 10)
    private String cilindradaCarro;

    private Boolean blindado;

    // Específicos de moto
    @Column(name = "cilindrada_moto")
    private Integer cilindradaMoto;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_moto", length = 20)
    private CategoriaMoto categoriaMoto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_partida", length = 10)
    private TipoPartida tipoPartida;

    // Status e metadados
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusAnuncio status = StatusAnuncio.ATIVO;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "anunciado_em", nullable = false)
    private LocalDateTime anunciadoEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id", nullable = false)
    private Login login;

    @OneToMany(mappedBy = "veiculo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VeiculoImagem> imagens = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "veiculo_opcional",
            joinColumns = @JoinColumn(name = "veiculo_id"),
            inverseJoinColumns = @JoinColumn(name = "opcional_id")
    )
    private Set<Opcional> opcionais = new HashSet<>();

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        anunciadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    private Double latitude;

    private Double longitude;
}