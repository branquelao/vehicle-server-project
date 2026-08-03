package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoResponseDTO {
    private Long id;
    private TipoVeiculo tipo;
    private String marca;
    private String modelo;
    private int anoFabricacao;
    private int anoModelo;
    private int km;
    private String cor;
    private Combustivel combustivel;
    private Cambio cambio;
    private boolean unicoDono;
    private boolean aceitaTroca;
    private EstadoConservacao estadoConservacao;
    private float valor;
    private String descricao;

    private Carroceria carroceria;
    private Integer portas;
    private Integer potenciaCv;
    private String cilindradaCarro;
    private Boolean blindado;

    private Integer cilindradaMoto;
    private CategoriaMoto categoriaMoto;
    private TipoPartida tipoPartida;

    private StatusAnuncio status;

    private String cidade;
    private String estado;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime anunciadoEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime atualizadoEm;

    private Long loginId;
    private List<String> imagens;
    private List<String> opcionais;

    public static VeiculoResponseDTO fromEntity(Veiculo v) {
        VeiculoResponseDTO dto = new VeiculoResponseDTO();
        dto.setId(v.getId());
        dto.setTipo(v.getTipo());
        dto.setMarca(v.getMarca());
        dto.setModelo(v.getModelo());
        dto.setAnoFabricacao(v.getAnoFabricacao());
        dto.setAnoModelo(v.getAnoModelo());
        dto.setKm(v.getKm());
        dto.setCor(v.getCor());
        dto.setCombustivel(v.getCombustivel());
        dto.setCambio(v.getCambio());
        dto.setUnicoDono(v.isUnicoDono());
        dto.setAceitaTroca(v.isAceitaTroca());
        dto.setEstadoConservacao(v.getEstadoConservacao());
        dto.setValor(v.getValor());
        dto.setDescricao(v.getDescricao());
        dto.setCarroceria(v.getCarroceria());
        dto.setPortas(v.getPortas());
        dto.setPotenciaCv(v.getPotenciaCv());
        dto.setCilindradaCarro(v.getCilindradaCarro());
        dto.setBlindado(v.getBlindado());
        dto.setCilindradaMoto(v.getCilindradaMoto());
        dto.setCategoriaMoto(v.getCategoriaMoto());
        dto.setTipoPartida(v.getTipoPartida());
        dto.setStatus(v.getStatus());
        dto.setCidade(v.getCidade());
        dto.setEstado(v.getEstado());
        dto.setAnunciadoEm(v.getAnunciadoEm());
        dto.setAtualizadoEm(v.getAtualizadoEm());
        dto.setLoginId(v.getLogin().getId());
        dto.setImagens(v.getImagens().stream()
                .sorted((a, b) -> Integer.compare(a.getOrdem(), b.getOrdem()))
                .map(img -> img.getUrlImagem())
                .toList());
        dto.setOpcionais(v.getOpcionais().stream()
                .map(op -> op.getNome())
                .toList());
        return dto;
    }
}