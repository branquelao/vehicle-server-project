package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem.ConversaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem.MensagemResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem.NovaMensagemDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.MensagemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MensagemController {

    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @PostMapping("/veiculos/{veiculoId}/mensagens")
    public ResponseEntity<ConversaResponseDTO> iniciarConversa(@PathVariable Long veiculoId,
                                                               @Valid @RequestBody NovaMensagemDTO dto) {
        var conversa = mensagemService.iniciarOuContinuar(veiculoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversaResponseDTO.fromEntity(conversa));
    }

    @PostMapping("/conversas/{conversaId}/mensagens")
    public ResponseEntity<ConversaResponseDTO> responder(@PathVariable Long conversaId,
                                                         @Valid @RequestBody NovaMensagemDTO dto) {
        var conversa = mensagemService.responder(conversaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversaResponseDTO.fromEntity(conversa));
    }

    @GetMapping("/conversas")
    public List<ConversaResponseDTO> listarConversas() {
        return mensagemService.listarConversas().stream()
                .map(ConversaResponseDTO::fromEntity)
                .toList();
    }

    @GetMapping("/conversas/{conversaId}/mensagens")
    public List<MensagemResponseDTO> listarMensagens(@PathVariable Long conversaId) {
        return mensagemService.listarMensagens(conversaId).stream()
                .map(MensagemResponseDTO::fromEntity)
                .toList();
    }
}