package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Mensagem;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemResponseDTO {

    private Long id;
    private Long remetenteId;
    private String remetenteUsuario;
    private String conteudo;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime enviadaEm;

    private boolean lida;

    public static MensagemResponseDTO fromEntity(Mensagem m) {
        return new MensagemResponseDTO(
                m.getId(),
                m.getRemetente().getId(),
                m.getRemetente().getUsuario(),
                m.getConteudo(),
                m.getEnviadaEm(),
                m.isLida()
        );
    }
}