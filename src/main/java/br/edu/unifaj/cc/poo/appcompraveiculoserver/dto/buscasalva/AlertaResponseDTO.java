package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Alerta;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponseDTO {

    private Long id;
    private Long buscaSalvaId;
    private VeiculoResponseDTO veiculo;
    private boolean visualizado;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime criadoEm;

    public static AlertaResponseDTO fromEntity(Alerta a) {
        return new AlertaResponseDTO(
                a.getId(),
                a.getBuscaSalva().getId(),
                VeiculoResponseDTO.fromEntity(a.getVeiculo()),
                a.isVisualizado(),
                a.getCriadoEm()
        );
    }
}