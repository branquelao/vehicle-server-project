package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.upload.UploadResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Upload;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.UploadRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@Tag(name = "Uploads", description = "Envio e consulta de imagens usadas em anúncios de veículos e perfis")
public class UploadController {

    private final UploadRepository uploadRepository;
    private final LoginRepository loginRepository;

    public UploadController(UploadRepository uploadRepository, LoginRepository loginRepository) {
        this.uploadRepository = uploadRepository;
        this.loginRepository = loginRepository;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    private Login usuarioLogado() {
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return loginRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado"));
    }

    @Operation(
            summary = "Enviar imagem",
            description = "Faz upload de uma imagem para uso posterior em um anúncio de veículo. O nome de " +
                    "arquivo retornado deve ser referenciado no campo imagens ao criar ou atualizar um veículo. " +
                    "Requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imagem enviada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UploadResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Falha ao processar o upload da imagem",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDTO> uploadImagem(
            @Parameter(description = "Arquivo de imagem (jpg, png, etc.)")
            @RequestParam("file") MultipartFile file) throws IOException {
        Path pastaUploads = uploadDir();
        if (!Files.exists(pastaUploads)) {
            Files.createDirectories(pastaUploads);
        }

        String nomeArquivo = UploadPathResolver.gerarNomeSeguro(file.getOriginalFilename());
        Path destino = UploadPathResolver.resolveDentroDeUploads(pastaUploads, nomeArquivo);
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        Upload registro = new Upload(file.getOriginalFilename(), nomeArquivo, usuarioLogado());
        uploadRepository.save(registro);

        return ResponseEntity.status(HttpStatus.CREATED).body(UploadResponseDTO.fromEntity(registro));
    }

    @Operation(
            summary = "Listar meus uploads",
            description = "Retorna o histórico de imagens enviadas pelo usuário autenticado, da mais recente " +
                    "para a mais antiga. Requer autenticação."
    )
    @ApiResponse(responseCode = "200", description = "Lista de uploads retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = UploadResponseDTO.class))))
    @GetMapping("/uploads")
    public List<UploadResponseDTO> listarMeusUploads() {
        Long loginId = usuarioLogado().getId();
        return uploadRepository.findByLogin_IdOrderByEnviadoEmDesc(loginId).stream()
                .map(UploadResponseDTO::fromEntity)
                .toList();
    }
}