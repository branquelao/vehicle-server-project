package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

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

    //Arquivo da Imagem
    private MultipartFile motoImagem;
}
