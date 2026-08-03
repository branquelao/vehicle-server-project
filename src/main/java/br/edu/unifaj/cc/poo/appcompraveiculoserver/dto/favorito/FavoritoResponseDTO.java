package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.favorito;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Favorito;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoResponseDTO {

    private VeiculoResponseDTO veiculo;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime favoritadoEm;

    public static FavoritoResponseDTO fromEntity(Favorito favorito) {
        return new FavoritoResponseDTO(
                VeiculoResponseDTO.fromEntity(favorito.getVeiculo()),
                favorito.getCriadoEm()
        );
    }
}