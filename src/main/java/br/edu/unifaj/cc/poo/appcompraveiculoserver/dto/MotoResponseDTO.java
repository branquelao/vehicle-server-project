package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotoResponseDTO {
    private Long id;
    private String motoNome;
    private String motoCor;
    private int motoAno;
    private float motoValor;
    private String motoImagem;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime motoAnunciadaEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime motoAtualizadaEm;

    private Long loginId;

    public static MotoResponseDTO fromEntity(Moto moto) {
        return new MotoResponseDTO(
                moto.getId(), moto.getMotoNome(), moto.getMotoCor(),
                moto.getMotoAno(), moto.getMotoValor(), moto.getMotoImagem(),
                moto.getMotoAnunciadaEm(), moto.getMotoAtualizadaEm(),
                moto.getLogin().getId()
        );
    }
}