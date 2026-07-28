package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.LoginDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.LoginResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    @GetMapping("/login")
    public List<LoginResponseDTO> getLogins() {
        return loginService.listarTodos().stream()
                .map(LoginResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/login/{id}")
    public ResponseEntity<LoginResponseDTO> getLoginId(@PathVariable Long id) {
        return loginService.buscarPorId(id)
                .map(login -> ResponseEntity.ok(LoginResponseDTO.fromEntity(login)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> postLogin(@Valid @RequestBody LoginDTO dto) {
        Login salvo = loginService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponseDTO.fromEntity(salvo));
    }

    @PutMapping("/login/{id}")
    public ResponseEntity<LoginResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody LoginDTO dto) {
        Login salvo = loginService.atualizar(id, dto);
        return ResponseEntity.ok(LoginResponseDTO.fromEntity(salvo));
    }

    @PutMapping("/login/{id}/imagem")
    public ResponseEntity<LoginResponseDTO> atualizarImagem(
            @PathVariable Long id,
            @RequestParam("imagem") MultipartFile imagem) throws IOException {
        Login salvo = loginService.atualizarImagem(id, imagem, uploadDir());
        return ResponseEntity.ok(LoginResponseDTO.fromEntity(salvo));
    }

    @DeleteMapping("/login/{id}")
    public ResponseEntity<Void> deletarLogin(@PathVariable Long id) {
        loginService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}