package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.PaginaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoFiltroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoVeiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.VeiculoService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/veiculos")
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

    @GetMapping
    public PaginaResponseDTO<VeiculoResponseDTO> getVeiculos(
            @RequestParam(required = false) TipoVeiculo tipo,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Float precoMin,
            @RequestParam(required = false) Float precoMax,
            @RequestParam(required = false) Integer anoMin,
            @RequestParam(required = false) Integer anoMax,
            @RequestParam(required = false) Integer kmMax,
            @RequestParam(required = false) String cor,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) StatusAnuncio status,
            @Parameter(description = "Campos aceitos: valor, anoModelo, anoFabricacao, km, anunciadoEm, atualizadoEm, marca, modelo. Formato: campo,asc ou campo,desc. Ex: valor,asc")
            @PageableDefault(size = 20)
            Pageable pageable) {

        VeiculoFiltroDTO filtro = new VeiculoFiltroDTO(tipo, marca, modelo, precoMin, precoMax,
                anoMin, anoMax, kmMax, cor, cidade, estado, status);

        Page<Veiculo> pagina = veiculoService.buscar(filtro, pageableSeguro(pageable));
        return PaginaResponseDTO.fromPage(pagina, VeiculoResponseDTO::fromEntity);
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