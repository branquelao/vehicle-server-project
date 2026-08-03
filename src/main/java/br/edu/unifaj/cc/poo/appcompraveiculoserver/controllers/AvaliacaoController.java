package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.AvaliacaoResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.NovaAvaliacaoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.ResumoAvaliacaoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logins/{vendedorId}/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponseDTO> avaliar(@PathVariable Long vendedorId,
                                                        @Valid @RequestBody NovaAvaliacaoDTO dto) {
        var avaliacao = avaliacaoService.avaliar(vendedorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(AvaliacaoResponseDTO.fromEntity(avaliacao));
    }

    @GetMapping
    public List<AvaliacaoResponseDTO> listar(@PathVariable Long vendedorId) {
        return avaliacaoService.listarPorVendedor(vendedorId).stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/resumo")
    public ResumoAvaliacaoDTO resumo(@PathVariable Long vendedorId) {
        return avaliacaoService.resumo(vendedorId);
    }
}