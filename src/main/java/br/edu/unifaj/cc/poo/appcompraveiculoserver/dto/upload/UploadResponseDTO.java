package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.upload;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Upload;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDTO {

    private Long id;
    private String nomeOriginal;
    private String nomeGerado;
    private String url;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime enviadoEm;

    public static UploadResponseDTO fromEntity(Upload upload) {
        return new UploadResponseDTO(
                upload.getId(),
                upload.getNomeOriginal(),
                upload.getNomeGerado(),
                "/arquivos/" + upload.getNomeGerado(),
                upload.getEnviadoEm()
        );
    }
}