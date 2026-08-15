package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.AvaliacaoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.NovaAvaliacaoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.ResumoAvaliacaoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logins/{vendedorId}/avaliacoes")
@Tag(name = "Avaliações", description = "Reputação de vendedores, avaliada por compradores com quem já conversaram")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @Operation(
            summary = "Avaliar vendedor",
            description = "Registra uma avaliação (nota de 1 a 5 e comentário opcional) para um vendedor. " +
                    "Só é permitido avaliar vendedores com quem o usuário autenticado já trocou mensagens, e " +
                    "não é possível avaliar a si mesmo. Uma nova avaliação do mesmo comprador para o mesmo " +
                    "vendedor substitui a anterior."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliação registrada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AvaliacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, avaliação de si mesmo ou vendedor com quem nunca conversou",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vendedor não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> avaliar(@PathVariable Long vendedorId,
                                                        @Valid @RequestBody NovaAvaliacaoDTO dto) {
        var avaliacao = avaliacaoService.avaliar(vendedorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(AvaliacaoResponseDTO.fromEntity(avaliacao));
    }

    @Operation(
            summary = "Listar avaliações de um vendedor",
            description = "Retorna todas as avaliações recebidas por um vendedor, da mais recente para a mais " +
                    "antiga. Endpoint público, não exige autenticação."
    )
    @ApiResponse(responseCode = "200", description = "Lista de avaliações retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = AvaliacaoResponseDTO.class))))
    @SecurityRequirements
    @GetMapping
    public List<AvaliacaoResponseDTO> listar(@PathVariable Long vendedorId) {
        return avaliacaoService.listarPorVendedor(vendedorId).stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    @Operation(
            summary = "Resumo de avaliações de um vendedor",
            description = "Retorna a nota média e o total de avaliações recebidas por um vendedor. Se o " +
                    "vendedor ainda não tiver avaliações, a média retorna zero. Endpoint público, não exige " +
                    "autenticação."
    )
    @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ResumoAvaliacaoDTO.class)))
    @SecurityRequirements
    @GetMapping("/resumo")
    public ResumoAvaliacaoDTO resumo(@PathVariable Long vendedorId) {
        return avaliacaoService.resumo(vendedorId);
    }
}