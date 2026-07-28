package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.CarroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.CarroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.CarroService;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
public class CarroController {

    private final CarroService carroService;

    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    @GetMapping("/veiculos/carro")
    public List<CarroResponseDTO> getCarros() {
        return carroService.listarTodos().stream()
                .map(CarroResponseDTO::fromEntity)
                .toList();
    }


    @GetMapping("/veiculos/carro/{id}")
    public ResponseEntity<CarroResponseDTO> getCarroById(@PathVariable Long id) {
        return carroService.buscarPorId(id)
                .map(CarroResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/veiculos/carro/recentes")
    public List<CarroResponseDTO> ultimosCarros() {
        return carroService.listarRecentes().stream()
                .map(CarroResponseDTO::fromEntity)
                .toList();
    }

    @PostMapping("/veiculos/carro")
    public ResponseEntity<CarroResponseDTO> postCarro(@Valid @RequestBody CarroDTO dto) {
        Carro salvo = carroService.criar(dto, uploadDir());
        return ResponseEntity.status(HttpStatus.CREATED).body(CarroResponseDTO.fromEntity(salvo));
    }

    /**
     * Upload compartilhado por Carro e Moto — enviar a imagem antes de criar o anúncio.
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

    @PutMapping("/veiculos/carro/{id}")
    public ResponseEntity<CarroResponseDTO> putCarro(@Valid @RequestBody CarroDTO novoDto, @PathVariable Long id) {
        return ResponseEntity.ok(CarroResponseDTO.fromEntity(carroService.atualizar(id, novoDto, uploadDir())));
    }

    @DeleteMapping("/veiculos/carro/{id}")
    public ResponseEntity<Object> deleteCarro(@PathVariable Long id) {
        carroService.deletar(id, uploadDir());
        return ResponseEntity.noContent().build();
    }
}