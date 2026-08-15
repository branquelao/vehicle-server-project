package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva.AlertaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva.BuscaSalvaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva.NovaBuscaSalvaDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.BuscaSalvaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Buscas salvas", description = "Filtros de busca salvos pelo usuário, com alertas automáticos de novos anúncios compatíveis")
public class BuscaSalvaController {

    private final BuscaSalvaService buscaSalvaService;

    public BuscaSalvaController(BuscaSalvaService buscaSalvaService) {
        this.buscaSalvaService = buscaSalvaService;
    }

    @Operation(
            summary = "Criar busca salva",
            description = "Salva um conjunto de filtros vinculado ao usuário autenticado. Um job periódico " +
                    "compara novos anúncios contra as buscas salvas e gera alertas quando houver compatibilidade."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Busca salva criada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BuscaSalvaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping("/buscas-salvas")
    public ResponseEntity<BuscaSalvaResponseDTO> criar(@Valid @RequestBody NovaBuscaSalvaDTO dto) {
        var busca = buscaSalvaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(BuscaSalvaResponseDTO.fromEntity(busca));
    }

    @Operation(
            summary = "Listar minhas buscas salvas",
            description = "Retorna todas as buscas salvas do usuário autenticado, da mais recente para a mais antiga."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de buscas salvas retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = BuscaSalvaResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/buscas-salvas")
    public List<BuscaSalvaResponseDTO> listar() {
        return buscaSalvaService.listarMinhas().stream()
                .map(BuscaSalvaResponseDTO::fromEntity)
                .toList();
    }

    @Operation(
            summary = "Excluir busca salva",
            description = "Remove uma busca salva. Apenas o dono da busca pode realizar essa operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Busca salva excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir esta busca salva",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Busca salva não encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @DeleteMapping("/buscas-salvas/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        buscaSalvaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar meus alertas",
            description = "Retorna os alertas gerados para o usuário autenticado, gerados pelo job de matching " +
                    "quando um novo anúncio corresponde a uma de suas buscas salvas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de alertas retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = AlertaResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/alertas")
    public List<AlertaResponseDTO> listarAlertas() {
        return buscaSalvaService.listarAlertas().stream()
                .map(AlertaResponseDTO::fromEntity)
                .toList();
    }

    @Operation(
            summary = "Marcar alerta como visualizado",
            description = "Marca um alerta como visualizado pelo usuário. Apenas o dono do alerta pode realizar essa operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alerta atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AlertaResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para acessar este alerta",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Alerta não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PutMapping("/alertas/{id}/visualizado")
    public ResponseEntity<AlertaResponseDTO> marcarVisualizado(@PathVariable Long id) {
        var alerta = buscaSalvaService.marcarVisualizado(id);
        return ResponseEntity.ok(AlertaResponseDTO.fromEntity(alerta));
    }
}