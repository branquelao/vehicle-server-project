package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva.AlertaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva.BuscaSalvaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva.NovaBuscaSalvaDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.BuscaSalvaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BuscaSalvaController {

    private final BuscaSalvaService buscaSalvaService;

    public BuscaSalvaController(BuscaSalvaService buscaSalvaService) {
        this.buscaSalvaService = buscaSalvaService;
    }

    @PostMapping("/buscas-salvas")
    public ResponseEntity<BuscaSalvaResponseDTO> criar(@Valid @RequestBody NovaBuscaSalvaDTO dto) {
        var busca = buscaSalvaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BuscaSalvaResponseDTO.fromEntity(busca));
    }

    @GetMapping("/buscas-salvas")
    public List<BuscaSalvaResponseDTO> listar() {
        return buscaSalvaService.listarMinhas().stream()
                .map(BuscaSalvaResponseDTO::fromEntity)
                .toList();
    }

    @DeleteMapping("/buscas-salvas/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        buscaSalvaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alertas")
    public List<AlertaResponseDTO> listarAlertas() {
        return buscaSalvaService.listarAlertas().stream()
                .map(AlertaResponseDTO::fromEntity)
                .toList();
    }

    @PutMapping("/alertas/{id}/visualizado")
    public ResponseEntity<AlertaResponseDTO> marcarVisualizado(@PathVariable Long id) {
        var alerta = buscaSalvaService.marcarVisualizado(id);
        return ResponseEntity.ok(AlertaResponseDTO.fromEntity(alerta));
    }
}