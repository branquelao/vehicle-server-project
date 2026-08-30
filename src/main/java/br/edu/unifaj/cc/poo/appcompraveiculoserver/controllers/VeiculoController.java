package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.PaginaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.NovoStatusVeiculoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoFiltroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoVeiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veículos", description = "Anúncios de veículos (carros e motos): busca, cadastro e gestão")
public class VeiculoController {

    private final VeiculoService veiculoService;

    private static final Set<String> CAMPOS_ORDENACAO_PERMITIDOS = Set.of(
            "valor", "anoModelo", "anoFabricacao", "km", "anunciadoEm", "atualizadoEm", "marca", "modelo"
    );
    private static final Sort ORDENACAO_PADRAO = Sort.by(Sort.Direction.DESC, "anunciadoEm");

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    private Pageable pageableSeguro(Pageable pageable) {
        List<Sort.Order> validos = pageable.getSort().stream()
                .filter(o -> CAMPOS_ORDENACAO_PERMITIDOS.contains(o.getProperty()))
                .toList();

        Sort sortFinal = validos.isEmpty() ? ORDENACAO_PADRAO : Sort.by(validos);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortFinal);
    }

    @Operation(
            summary = "Buscar veículos com filtros e paginação",
            description = "Lista anúncios ativos (por padrão) com suporte a filtros combinados, paginação e " +
                    "ordenação. Endpoint público, não exige autenticação."
    )
    @ApiResponse(responseCode = "200", description = "Página de veículos retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PaginaResponseDTO.class)))
    @SecurityRequirements // público
    @GetMapping
    public PaginaResponseDTO<VeiculoResponseDTO> getVeiculos(
            @Parameter(description = "Tipo do veículo") @RequestParam(required = false) TipoVeiculo tipo,
            @Parameter(description = "Marca (busca parcial, case-insensitive)") @RequestParam(required = false) String marca,
            @Parameter(description = "Modelo (busca parcial, case-insensitive)") @RequestParam(required = false) String modelo,
            @Parameter(description = "Preço mínimo") @RequestParam(required = false) Float precoMin,
            @Parameter(description = "Preço máximo") @RequestParam(required = false) Float precoMax,
            @Parameter(description = "Ano modelo mínimo") @RequestParam(required = false) Integer anoMin,
            @Parameter(description = "Ano modelo máximo") @RequestParam(required = false) Integer anoMax,
            @Parameter(description = "Quilometragem máxima") @RequestParam(required = false) Integer kmMax,
            @Parameter(description = "Cor (busca exata, case-insensitive)") @RequestParam(required = false) String cor,
            @Parameter(description = "Cidade (busca exata, case-insensitive)") @RequestParam(required = false) String cidade,
            @Parameter(description = "Estado / UF (busca exata, case-insensitive)") @RequestParam(required = false) String estado,
            @Parameter(description = "Status do anúncio. Se omitido, retorna apenas ATIVO") @RequestParam(required = false) StatusAnuncio status,
            @Parameter(description = "Campos aceitos: valor, anoModelo, anoFabricacao, km, anunciadoEm, atualizadoEm, marca, modelo. Formato: campo,asc ou campo,desc. Ex: valor,asc")
            @PageableDefault(size = 20)
            Pageable pageable) {

        VeiculoFiltroDTO filtro = new VeiculoFiltroDTO(tipo, marca, modelo, precoMin, precoMax,
                anoMin, anoMax, kmMax, cor, cidade, estado, status);

        Page<Veiculo> pagina = veiculoService.buscar(filtro, pageableSeguro(pageable));
        return PaginaResponseDTO.fromPage(pagina, VeiculoResponseDTO::fromEntity);
    }

    @Operation(
            summary = "Buscar veículo por ID",
            description = "Retorna os detalhes completos de um anúncio. Endpoint público, não exige autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VeiculoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @SecurityRequirements // público
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> getVeiculoById(@PathVariable Long id) {
        return veiculoService.buscarPorId(id)
                .map(VeiculoResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar veículos mais recentes",
            description = "Retorna os 3 anúncios mais recentes, para uso em destaque/home. Endpoint público."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = VeiculoResponseDTO.class))))
    @SecurityRequirements // público
    @GetMapping("/recentes")
    public List<VeiculoResponseDTO> ultimosVeiculos() {
        return veiculoService.listarRecentes().stream()
                .map(VeiculoResponseDTO::fromEntity)
                .toList();
    }

    @Operation(
            summary = "Criar novo anúncio de veículo",
            description = "Cadastra um novo anúncio de carro ou moto. As imagens devem ser enviadas previamente " +
                    "via /uploads e referenciadas pelo nome do arquivo gerado. Requer autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veículo criado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VeiculoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (campos obrigatórios por tipo, validação)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Login (loginId) ou opcional informado não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<VeiculoResponseDTO> postVeiculo(@Valid @RequestBody VeiculoDTO dto) {
        Veiculo salvo = veiculoService.criar(dto, uploadDir());
        return ResponseEntity.status(HttpStatus.CREATED).body(VeiculoResponseDTO.fromEntity(salvo));
    }

    @Operation(
            summary = "Atualizar anúncio de veículo",
            description = "Atualiza os dados de um anúncio existente. Apenas o dono do anúncio ou um ADMIN " +
                    "pode realizar essa operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VeiculoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para editar este anúncio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponseDTO> putVeiculo(@Valid @RequestBody VeiculoDTO novoDto, @PathVariable Long id) {
        return ResponseEntity.ok(VeiculoResponseDTO.fromEntity(veiculoService.atualizar(id, novoDto, uploadDir())));
    }

    @Operation(
            summary = "Atualizar status do anúncio",
            description = "Atualiza apenas o status de um anúncio (ex: pausar, marcar como vendido), sem exigir " +
                    "o reenvio dos demais campos. Apenas o dono do anúncio ou um ADMIN pode realizar essa operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VeiculoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para editar este anúncio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<VeiculoResponseDTO> patchStatus(@PathVariable Long id,
                                                          @Valid @RequestBody NovoStatusVeiculoDTO dto) {
        Veiculo atualizado = veiculoService.atualizarStatus(id, dto);
        return ResponseEntity.ok(VeiculoResponseDTO.fromEntity(atualizado));
    }

    @Operation(
            summary = "Excluir anúncio de veículo",
            description = "Remove um anúncio. Apenas o dono do anúncio ou um ADMIN pode realizar essa operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veículo excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir este anúncio",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteVeiculo(@PathVariable Long id) {
        veiculoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}