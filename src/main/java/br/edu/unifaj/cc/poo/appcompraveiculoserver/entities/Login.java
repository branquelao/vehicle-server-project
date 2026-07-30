package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoPerfil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "login")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Login {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String usuario;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(name = "login_imagem", length = 100)
    private String loginImagem;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(name = "login_criado_em", nullable = false)
    private LocalDateTime loginCriadoEm;

    @Column(name = "login_atualizado_em", nullable = false)
    private LocalDateTime loginAtualizadoEm;

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        loginCriadoEm = agora;
        loginAtualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        loginAtualizadoEm = LocalDateTime.now();
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_perfil", nullable = false, length = 15)
    private TipoPerfil tipoPerfil = TipoPerfil.PESSOA_FISICA;

    @Column(name = "razao_social", length = 100)
    private String razaoSocial;

    @Column(length = 18)
    private String cnpj;
}