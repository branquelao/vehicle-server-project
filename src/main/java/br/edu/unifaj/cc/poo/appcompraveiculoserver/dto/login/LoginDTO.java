package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoPerfil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(message = "Usuário é obrigatório")
    @Size(min = 3, max = 100, message = "Usuário deve ter entre 3 e 100 caracteres")
    private String usuario;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String senha;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos (com DDD, sem símbolos)")
    private String telefone;

    @NotNull(message = "Tipo de perfil é obrigatório")
    private TipoPerfil tipoPerfil;

    @Schema(description = "Obrigatório quando tipoPerfil=LOJA. Não enviar quando tipoPerfil=PESSOA_FISICA.")
    @Size(max = 100)
    private String razaoSocial;

    @Schema(description = "Obrigatório quando tipoPerfil=LOJA. Não enviar quando tipoPerfil=PESSOA_FISICA.")
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos (sem símbolos)")
    private String cnpj;
}