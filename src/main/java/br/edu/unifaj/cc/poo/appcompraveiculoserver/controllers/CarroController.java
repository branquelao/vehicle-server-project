package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.CarroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.ImagemInvalidaException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.CarroService;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
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
    public List<Carro> getCarros() {
        return carroService.listarTodos();
    }

    @GetMapping("/veiculos/carro/{id}")
    public ResponseEntity<Carro> getCarroById(@PathVariable Long id) {
        return carroService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/veiculos/carro/recentes")
    public List<Carro> ultimosCarros() {
        return carroService.listarRecentes();
    }

    @PostMapping("/veiculos/carro")
    public ResponseEntity<?> postCarro(@RequestBody CarroDTO dto) {
        try {
            Carro salvo = carroService.criar(dto, uploadDir());
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (ImagemInvalidaException | RecursoNaoEncontradoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Upload compartilhado por Carro e Moto — enviar a imagem antes de criar o anúncio. */
    @PostMapping("/uploads")
    public ResponseEntity<Map<String, String>> uploadImagem(@RequestParam("file") MultipartFile file) {
        try {
            Path pastaUploads = uploadDir();
            if (!Files.exists(pastaUploads)) {
                Files.createDirectories(pastaUploads);
            }

            String nomeArquivo = UploadPathResolver.gerarNomeSeguro(file.getOriginalFilename());
            Path destino = UploadPathResolver.resolveDentroDeUploads(pastaUploads, nomeArquivo);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(Map.of("arquivo", nomeArquivo));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao enviar a imagem: " + e.getMessage()));
        }
    }

    @PutMapping("/veiculos/carro/{id}")
    public ResponseEntity<?> putCarro(@RequestBody CarroDTO novoDto, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(carroService.atualizar(id, novoDto, uploadDir()));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/veiculos/carro/{id}")
    public ResponseEntity<Object> deleteCarro(@PathVariable Long id) {
        try {
            carroService.deletar(id, uploadDir());
            return ResponseEntity.noContent().build();
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }
}