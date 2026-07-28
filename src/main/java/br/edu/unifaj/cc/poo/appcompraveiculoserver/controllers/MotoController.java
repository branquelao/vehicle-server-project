package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.MotoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.MotoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.MotoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class MotoController {

    private final MotoService motoService;

    public MotoController(MotoService motoService) {
        this.motoService = motoService;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    @GetMapping("/veiculos/moto")
    public List<MotoResponseDTO> getMotos() {
        return motoService.listarTodos().stream()
                .map(MotoResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/veiculos/moto/{id}")
    public ResponseEntity<MotoResponseDTO> getMotoById(@PathVariable Long id) {
        return motoService.buscarPorId(id)
                .map(MotoResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/veiculos/moto/recentes")
    public List<MotoResponseDTO> ultimasMotos() {
        return motoService.listarRecentes().stream()
                .map(MotoResponseDTO::fromEntity)
                .toList();
    }

    @PostMapping("/veiculos/moto")
    public ResponseEntity<MotoResponseDTO> postMoto(@Valid @RequestBody MotoDTO dto) {
        Moto salva = motoService.criar(dto, uploadDir());
        return ResponseEntity.status(HttpStatus.CREATED).body(MotoResponseDTO.fromEntity(salva));
    }

    @PutMapping("/veiculos/moto/{id}")
    public ResponseEntity<MotoResponseDTO> putMoto(@Valid @RequestBody MotoDTO novoDto, @PathVariable Long id) {
        return ResponseEntity.ok(MotoResponseDTO.fromEntity(motoService.atualizar(id, novoDto, uploadDir())));
    }

    @DeleteMapping("/veiculos/moto/{id}")
    public ResponseEntity<Object> deleteMoto(@PathVariable Long id) {
        motoService.deletar(id, uploadDir());
        return ResponseEntity.noContent().build();
    }
}