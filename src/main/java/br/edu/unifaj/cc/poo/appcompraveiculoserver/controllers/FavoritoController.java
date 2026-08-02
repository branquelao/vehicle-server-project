package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.favorito.FavoritoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.FavoritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping("/veiculos/{veiculoId}/favoritos")
    public ResponseEntity<Void> favoritar(@PathVariable Long veiculoId) {
        favoritoService.favoritar(veiculoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/veiculos/{veiculoId}/favoritos")
    public ResponseEntity<Void> desfavoritar(@PathVariable Long veiculoId) {
        favoritoService.desfavoritar(veiculoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favoritos")
    public List<FavoritoResponseDTO> listarFavoritos() {
        return favoritoService.listarFavoritos().stream()
                .map(FavoritoResponseDTO::fromEntity)
                .toList();
    }
}
