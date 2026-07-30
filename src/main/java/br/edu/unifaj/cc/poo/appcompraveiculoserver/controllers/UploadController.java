package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
public class UploadController {

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    /**
     * Upload compartilhado — enviar a imagem antes de criar o anúncio de veículo.
     */
    @PostMapping(value = "/uploads", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImagem(@RequestParam("file") MultipartFile file) throws IOException {
        Path pastaUploads = uploadDir();
        if (!Files.exists(pastaUploads)) {
            Files.createDirectories(pastaUploads);
        }

        String nomeArquivo = UploadPathResolver.gerarNomeSeguro(file.getOriginalFilename());
        Path destino = UploadPathResolver.resolveDentroDeUploads(pastaUploads, nomeArquivo);
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok(Map.of("arquivo", nomeArquivo));
    }
}