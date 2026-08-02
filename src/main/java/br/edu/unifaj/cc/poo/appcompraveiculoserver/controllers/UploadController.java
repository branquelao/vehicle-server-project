package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.upload.UploadResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Upload;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.UploadRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import org.springframework.http.HttpStatus;
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

    /**
     * Upload compartilhado — enviar a imagem antes de criar o anúncio de veículo.
     */
    @PostMapping(value = "/uploads", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponseDTO> uploadImagem(@RequestParam("file") MultipartFile file) throws IOException {
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

    @GetMapping("/uploads")
    public List<UploadResponseDTO> listarMeusUploads() {
        Long loginId = usuarioLogado().getId();
        return uploadRepository.findByLogin_IdOrderByEnviadoEmDesc(loginId).stream()
                .map(UploadResponseDTO::fromEntity)
                .toList();
    }
}