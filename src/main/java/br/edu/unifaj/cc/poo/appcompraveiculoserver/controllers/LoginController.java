package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.LoginResponseDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class LoginController {

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginController(LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public List<LoginResponseDTO> getLogins() {
        return loginRepository.findAll().stream()
                .map(LoginResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /*
    @GetMapping("/login")
    public LoginResponse getLogin(LoginRequest request) {
        Usuario u = dao.getUsuarioByLoginSenha(request.getLogin(), request.getSenha());
        LoginResponse resp = new LoginResponse();
        if (u == null) {
            resp.setStatus("NOK");
        } else {
            resp.setStatus("OK");
            resp.setTocken("222333444");
        }
        return resp;
    }

    @GetMapping("/login/{id}")
    public Login getLoginId(@PathVariable Long id){
        return loginRepository.findById(id).orElse(null);
    }
    */

    @GetMapping("/login/{id}")
    public ResponseEntity<LoginResponseDTO> getLoginId(@PathVariable Long id) {
        return loginRepository.findById(id)
                .map(login -> {
                    login.getCarros().size();
                    login.getMotos().size();
                    return ResponseEntity.ok(LoginResponseDTO.fromEntity(login));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/login/verificar")
    public ResponseEntity<LoginResponseDTO> verificarLogin(
            @RequestParam String usuario,
            @RequestParam String senha) {
        return loginRepository.findByUsuario(usuario)
                .filter(login -> passwordEncoder.matches(senha, login.getSenha()))
                .map(login -> ResponseEntity.ok(LoginResponseDTO.fromEntity(login)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> postLogin(@RequestBody Login l) {
        l.setSenha(passwordEncoder.encode(l.getSenha()));
        Login salvo = loginRepository.save(l);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponseDTO.fromEntity(salvo));
    }

    @PutMapping("/login/{id}")
    public ResponseEntity<LoginResponseDTO> atualizar(@PathVariable Long id, @RequestBody Login novoLogin) {
        return loginRepository.findById(id)
                .map(login -> {
                    login.setUsuario(novoLogin.getUsuario());
                    if (novoLogin.getSenha() != null && !novoLogin.getSenha().isBlank()) {
                        login.setSenha(passwordEncoder.encode(novoLogin.getSenha()));
                    }
                    login.setTelefone(novoLogin.getTelefone());
                    login.setCarteira(novoLogin.getCarteira());
                    Login salvo = loginRepository.save(login);
                    return ResponseEntity.ok(LoginResponseDTO.fromEntity(salvo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/login/{id}/imagem")
    public ResponseEntity<LoginResponseDTO> atualizarImagem(
            @PathVariable Long id,
            @RequestParam("imagem") MultipartFile imagem) {
        Optional<Login> optionalLogin = loginRepository.findById(id);
        if (optionalLogin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Login login = optionalLogin.get();

        try {
            Path pastaUploads = Paths.get(System.getProperty("user.dir"), "uploads");
            if (!Files.exists(pastaUploads)) {
                Files.createDirectories(pastaUploads);
            }

            if (login.getLoginImagem() != null && !login.getLoginImagem().isBlank()) {
                Path caminhoAntigo = pastaUploads.resolve(login.getLoginImagem()).normalize();
                if (caminhoAntigo.startsWith(pastaUploads)) {
                    Files.deleteIfExists(caminhoAntigo);
                }
            }

            String extensao = extrairExtensao(imagem.getOriginalFilename());
            String nomeArquivo = UUID.randomUUID() + extensao;
            Path caminhoNovo = pastaUploads.resolve(nomeArquivo);
            Files.copy(imagem.getInputStream(), caminhoNovo, StandardCopyOption.REPLACE_EXISTING);

            login.setLoginImagem(nomeArquivo);
            Login salvo = loginRepository.save(login);

            return ResponseEntity.ok(LoginResponseDTO.fromEntity(salvo));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            return "";
        }
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }

    @DeleteMapping("/login/{id}")
    public ResponseEntity<Void> deletarLogin(@PathVariable Long id) {
        if (!loginRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        loginRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
