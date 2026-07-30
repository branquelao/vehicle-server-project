package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    @GetMapping
    public List<VeiculoResponseDTO> getVeiculos() {
        return veiculoService.listarTodos().stream()
                .map(VeiculoResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> getVeiculoById(@PathVariable Long id) {
        return veiculoService.buscarPorId(id)
                .map(VeiculoResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recentes")
    public List<VeiculoResponseDTO> ultimosVeiculos() {
        return veiculoService.listarRecentes().stream()
                .map(VeiculoResponseDTO::fromEntity)
                .toList();
    }

    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> postVeiculo(@Valid @RequestBody VeiculoDTO dto) {
        Veiculo salvo = veiculoService.criar(dto, uploadDir());
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponseDTO.fromEntity(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> putVeiculo(@Valid @RequestBody VeiculoDTO novoDto, @PathVariable Long id) {
        return ResponseEntity.ok(VeiculoResponseDTO.fromEntity(veiculoService.atualizar(id, novoDto, uploadDir())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteVeiculo(@PathVariable Long id) {
        veiculoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}