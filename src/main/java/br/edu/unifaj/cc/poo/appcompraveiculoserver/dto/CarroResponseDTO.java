package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarroResponseDTO {
    private Long id;
    private String carroNome;
    private String carroCor;
    private int carroAno;
    private float carroValor;
    private String carroImagem;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime carroAnunciadoEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime carroAtualizadoEm;

    private Long loginId;

    public static CarroResponseDTO fromEntity(Carro carro) {
        return new CarroResponseDTO(
                carro.getId(), carro.getCarroNome(), carro.getCarroCor(),
                carro.getCarroAno(), carro.getCarroValor(), carro.getCarroImagem(),
                carro.getCarroAnunciadoEm(), carro.getCarroAtualizadoEm(),
                carro.getLogin().getId()
        );
    }
}