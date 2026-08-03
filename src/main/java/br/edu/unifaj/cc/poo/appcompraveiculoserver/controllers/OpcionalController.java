package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.opcional.OpcionalResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.OpcionalRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OpcionalController {

    private final OpcionalRepository opcionalRepository;

    public OpcionalController(OpcionalRepository opcionalRepository) {
        this.opcionalRepository = opcionalRepository;
    }

    @GetMapping("/opcionais")
    public List<OpcionalResponseDTO> listar() {
        return opcionalRepository.findAll().stream()
                .map(OpcionalResponseDTO::fromEntity)
                .toList();
    }
}