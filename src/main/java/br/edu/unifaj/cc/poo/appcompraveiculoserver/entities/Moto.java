package br.edu.unifaj.cc.poo.appcompraveiculoserver.entities;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moto {

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
}
