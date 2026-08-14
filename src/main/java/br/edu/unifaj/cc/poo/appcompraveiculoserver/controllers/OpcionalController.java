package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.opcional.OpcionalResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.OpcionalRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Opcionais", description = "Catálogo de opcionais disponíveis para associar a um veículo")
public class OpcionalController {

    private final OpcionalRepository opcionalRepository;

    public OpcionalController(OpcionalRepository opcionalRepository) {
        this.opcionalRepository = opcionalRepository;
    }

    @Operation(
            summary = "Listar catálogo de opcionais",
            description = "Retorna todos os opcionais cadastrados no sistema, usados para montar ou filtrar " +
                    "anúncios de veículos. Endpoint público, não exige autenticação."
    )
    @ApiResponse(responseCode = "200", description = "Lista de opcionais retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = OpcionalResponseDTO.class))))
    @SecurityRequirements
    @GetMapping("/opcionais")
    public List<OpcionalResponseDTO> listar() {
        return opcionalRepository.findAll().stream()
                .map(OpcionalResponseDTO::fromEntity)
                .toList();
    }
}