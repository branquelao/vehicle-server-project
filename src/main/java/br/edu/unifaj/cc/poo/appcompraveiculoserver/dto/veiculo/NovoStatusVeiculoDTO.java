package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovoStatusVeiculoDTO {

    @NotNull(message = "Status é obrigatório")
    private StatusAnuncio status;
}