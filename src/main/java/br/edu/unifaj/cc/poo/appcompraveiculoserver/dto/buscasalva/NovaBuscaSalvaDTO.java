package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoVeiculo;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovaBuscaSalvaDTO {

    private TipoVeiculo tipo;

    @Size(max = 50)
    private String marca;

    @Size(max = 50)
    private String modelo;

    private Float precoMin;
    private Float precoMax;
    private Integer anoMin;
    private Integer anoMax;
    private Integer kmMax;

    @Size(max = 25)
    private String cor;

    @Size(max = 100)
    private String cidade;

    @Size(max = 2, message = "Estado deve ser a sigla UF (ex: SP)")
    private String estado;
}