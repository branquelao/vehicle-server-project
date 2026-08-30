package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoPerfil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Long id;
    private String usuario;
    private String telefone;
    private String loginImagem;
    private String role;
    private TipoPerfil tipoPerfil;
    private String razaoSocial;
    private String cnpj;
    private LocalDateTime loginCriadoEm;
    private LocalDateTime loginAtualizadoEm;

    public static LoginResponseDTO fromEntity(Login login) {
        return new LoginResponseDTO(
                login.getId(), login.getUsuario(), login.getTelefone(),
                login.getLoginImagem(), login.getRole(),
                login.getTipoPerfil(), login.getRazaoSocial(), login.getCnpj(),
                login.getLoginCriadoEm(), login.getLoginAtualizadoEm()
        );
    }
}