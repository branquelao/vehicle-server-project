package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovaMensagemDTO {

    @NotBlank(message = "Conteúdo da mensagem é obrigatório")
    @Size(max = 1000, message = "Mensagem deve ter no máximo 1000 caracteres")
    private String conteudo;
}