package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
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
    private LocalDateTime loginCriadoEm;
    private LocalDateTime loginAtualizadoEm;

    public static LoginResponseDTO fromEntity(Login login) {
        return new LoginResponseDTO(
                login.getId(), login.getUsuario(), login.getTelefone(),
                login.getLoginImagem(), login.getLoginCriadoEm(), login.getLoginAtualizadoEm()
        );
    }
}