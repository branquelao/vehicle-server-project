package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Conversa;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversaResponseDTO {

    private Long id;
    private Long veiculoId;
    private String veiculoTitulo;
    private Long compradorId;
    private String compradorUsuario;
    private Long vendedorId;
    private String vendedorUsuario;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime criadaEm;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime atualizadaEm;

    public static ConversaResponseDTO fromEntity(Conversa c) {
        return new ConversaResponseDTO(
                c.getId(),
                c.getVeiculo().getId(),
                c.getVeiculo().getMarca() + " " + c.getVeiculo().getModelo(),
                c.getComprador().getId(),
                c.getComprador().getUsuario(),
                c.getVendedor().getId(),
                c.getVendedor().getUsuario(),
                c.getCriadaEm(),
                c.getAtualizadaEm()
        );
    }
}