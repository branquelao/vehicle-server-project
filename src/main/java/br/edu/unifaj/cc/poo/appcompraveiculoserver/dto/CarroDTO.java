package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarroDTO {

    private String carroNome;
    private String carroCor;
    private int carroAno;
    private float carroValor;
    private String carroImagem;
    private Long loginId;
}
