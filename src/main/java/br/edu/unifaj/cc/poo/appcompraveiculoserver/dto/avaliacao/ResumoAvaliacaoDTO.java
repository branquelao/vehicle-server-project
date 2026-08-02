package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumoAvaliacaoDTO {
    private double media;
    private long total;
}