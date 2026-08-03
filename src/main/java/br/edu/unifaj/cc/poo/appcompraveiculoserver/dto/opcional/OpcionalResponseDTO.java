package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.opcional;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Opcional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpcionalResponseDTO {

    private Long id;
    private String nome;

    public static OpcionalResponseDTO fromEntity(Opcional opcional) {
        return new OpcionalResponseDTO(opcional.getId(), opcional.getNome());
    }
}