package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.BuscaSalva;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoVeiculo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuscaSalvaResponseDTO {

    private Long id;
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

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime criadaEm;

    public static BuscaSalvaResponseDTO fromEntity(BuscaSalva b) {
        return new BuscaSalvaResponseDTO(
                b.getId(), b.getTipo(), b.getMarca(), b.getModelo(),
                b.getPrecoMin(), b.getPrecoMax(), b.getAnoMin(), b.getAnoMax(), b.getKmMax(),
                b.getCor(), b.getCidade(), b.getEstado(), b.getCriadaEm()
        );
    }
}