package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotoDTO {
    
    private String motoNome;
    private String motoCor;
    private int motoAno;
    private float motoValor;
    private String motoImagem;
    private Long loginId;
}
