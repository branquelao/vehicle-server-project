package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem.ConversaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem.MensagemResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem.NovaMensagemDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.MensagemService;
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
@Tag(name = "Mensagens", description = "Conversas entre comprador e vendedor sobre um anúncio de veículo")
public class MensagemController {

    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @Operation(
            summary = "Iniciar conversa sobre um veículo",
            description = "Envia a primeira mensagem sobre um anúncio. Se já existir uma conversa entre o " +
                    "comprador e o vendedor sobre esse veículo, a mensagem é adicionada a ela em vez de criar " +
                    "uma nova conversa. Não é possível iniciar conversa sobre o próprio anúncio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensagem enviada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConversaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Tentativa de iniciar conversa sobre o próprio anúncio ou conteúdo inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping("/veiculos/{veiculoId}/mensagens")
    public ResponseEntity<ConversaResponseDTO> iniciarConversa(@PathVariable Long veiculoId,
                                                               @Valid @RequestBody NovaMensagemDTO dto) {
        var conversa = mensagemService.iniciarOuContinuar(veiculoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversaResponseDTO.fromEntity(conversa));
    }

    @Operation(
            summary = "Responder em uma conversa existente",
            description = "Adiciona uma nova mensagem a uma conversa já iniciada. Apenas os participantes da " +
                    "conversa (comprador ou vendedor) podem responder."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensagem enviada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ConversaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Conteúdo inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é participante desta conversa",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Conversa não encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping("/conversas/{conversaId}/mensagens")
    public ResponseEntity<ConversaResponseDTO> responder(@PathVariable Long conversaId,
                                                         @Valid @RequestBody NovaMensagemDTO dto) {
        var conversa = mensagemService.responder(conversaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversaResponseDTO.fromEntity(conversa));
    }

    @Operation(
            summary = "Listar minhas conversas",
            description = "Retorna todas as conversas em que o usuário autenticado participa, como comprador " +
                    "ou vendedor, ordenadas pela mais recentemente atualizada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de conversas retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ConversaResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/conversas")
    public List<ConversaResponseDTO> listarConversas() {
        return mensagemService.listarConversas().stream()
                .map(ConversaResponseDTO::fromEntity)
                .toList();
    }

    @Operation(
            summary = "Listar mensagens de uma conversa",
            description = "Retorna todas as mensagens de uma conversa, da mais antiga para a mais recente. " +
                    "Apenas os participantes da conversa podem visualizar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de mensagens retornada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = MensagemResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é participante desta conversa",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Conversa não encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/conversas/{conversaId}/mensagens")
    public List<MensagemResponseDTO> listarMensagens(@PathVariable Long conversaId) {
        return mensagemService.listarMensagens(conversaId).stream()
                .map(MensagemResponseDTO::fromEntity)
                .toList();
    }
}