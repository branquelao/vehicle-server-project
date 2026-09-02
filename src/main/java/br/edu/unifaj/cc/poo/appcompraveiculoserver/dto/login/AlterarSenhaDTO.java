package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlterarSenhaDTO {

    // Obrigatória apenas quando o próprio dono da conta troca a senha.
    // Um ADMIN resetando a senha de outra pessoa não precisa (nem tem como) informá-la.
    private String senhaAtual;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    @Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d).+", message = "Senha deve conter ao menos uma letra e um número")
    private String novaSenha;
}