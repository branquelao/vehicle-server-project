package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoVeiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoFiltroDTO {
    private TipoVeiculo tipo;
    private String marca;
    private String modelo;
    private Float precoMin;
    private Float precoMax;
    private Integer anoMin;
    private Integer anoMax;
    private Integer kmMax;
    private String cor;
    private String cidade;
    private String estado;
    private StatusAnuncio status;
}