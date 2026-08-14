package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.favorito.FavoritoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Favoritos", description = "Gestão de veículos favoritados pelo usuário autenticado")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @Operation(
            summary = "Favoritar veículo",
            description = "Adiciona um veículo à lista de favoritos do usuário autenticado. A operação é " +
                    "idempotente: favoritar um veículo já favoritado não gera erro. Não é possível favoritar " +
                    "o próprio anúncio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veículo favoritado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Tentativa de favoritar o próprio anúncio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping("/veiculos/{veiculoId}/favoritos")
    public ResponseEntity<Void> favoritar(@PathVariable Long veiculoId) {
        favoritoService.favoritar(veiculoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remover veículo dos favoritos",
            description = "Remove um veículo da lista de favoritos do usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veículo removido dos favoritos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @DeleteMapping("/veiculos/{veiculoId}/favoritos")
    public ResponseEntity<Void> desfavoritar(@PathVariable Long veiculoId) {
        favoritoService.desfavoritar(veiculoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar meus favoritos",
            description = "Retorna a lista de veículos favoritados pelo usuário autenticado, da mais recente " +
                    "para a mais antiga."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de favoritos retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = FavoritoResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/favoritos")
    public List<FavoritoResponseDTO> listarFavoritos() {
        return favoritoService.listarFavoritos().stream()
                .map(FavoritoResponseDTO::fromEntity)
                .toList();
    }
}