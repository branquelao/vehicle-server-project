package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Avaliacao;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoResponseDTO {

    private Long id;
    private Long avaliadorId;
    private String avaliadorUsuario;
    private int nota;
    private String comentario;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime criadaEm;

    public static AvaliacaoResponseDTO fromEntity(Avaliacao a) {
        return new AvaliacaoResponseDTO(
                a.getId(),
                a.getAvaliador().getId(),
                a.getAvaliador().getUsuario(),
                a.getNota(),
                a.getComentario(),
                a.getCriadaEm()
        );
    }
}