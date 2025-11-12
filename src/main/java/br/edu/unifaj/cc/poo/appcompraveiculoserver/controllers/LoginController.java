package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
public class LoginController {

    private final LoginRepository loginRepository;

    public LoginController(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    @GetMapping("/login")
    public List<Login> getLogins(){
        return loginRepository.findAll();
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
    public ResponseEntity<Login> getLoginId(@PathVariable Long id) {
        Optional<Login> login = loginRepository.findById(id);

        if (login.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Força o carregamento das listas (porque estão LAZY)
        login.get().getCarros().size();
        login.get().getMotos().size();

        return ResponseEntity.ok(login.get());
    }

    @GetMapping("/login/verificar")
    public ResponseEntity<Login> verificarLogin(
            @RequestParam String usuario,
            @RequestParam String senha) {
        Optional<Login> loginOpt = loginRepository.findByUsuarioAndSenha(usuario, senha);
        return loginOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }


    @PostMapping("/login")
    public Login postLogin(@RequestBody Login l) {
        return loginRepository.save(l);
    }

    @PutMapping("/login/{id}")
    public ResponseEntity<Login> atualizar(@PathVariable Long id, @RequestBody Login novoLogin) {
        return loginRepository.findById(id)
                .map(login -> {
                    login.setUsuario(novoLogin.getUsuario());
                    login.setSenha(novoLogin.getSenha());
                    login.setTelefone(novoLogin.getTelefone());
                    login.setCarteira(novoLogin.getCarteira());
                    loginRepository.save(login);
                    return ResponseEntity.ok(login);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/login/{id}/imagem")
    public ResponseEntity<Login> atualizarImagem(
            @PathVariable Long id,
            @RequestParam("imagem") MultipartFile imagem) {
        try {
            Optional<Login> optionalLogin = loginRepository.findById(id);
            if (optionalLogin.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Login login = optionalLogin.get();

            // Caminho da pasta de uploads
            Path pastaUploads = Paths.get("uploads");
            if (!Files.exists(pastaUploads)) {
                Files.createDirectories(pastaUploads);
            }

            // Exclui a imagem antiga, se existir
            if (login.getLoginImagem() != null && !login.getLoginImagem().isBlank()) {
                Path caminhoAntigo = pastaUploads.resolve(login.getLoginImagem());
                if (Files.exists(caminhoAntigo)) {
                    try {
                        Files.delete(caminhoAntigo);
                    } catch (IOException e) {
                        System.err.println("⚠️ Não foi possível excluir a imagem antiga: " + e.getMessage());
                    }
                }
            }

            // Salva a nova imagem
            String nomeArquivo = UUID.randomUUID() + "_" + imagem.getOriginalFilename();
            Path caminhoNovo = pastaUploads.resolve(nomeArquivo);
            Files.copy(imagem.getInputStream(), caminhoNovo, StandardCopyOption.REPLACE_EXISTING);

            // Atualiza no banco
            login.setLoginImagem(nomeArquivo);
            loginRepository.save(login);

            return ResponseEntity.ok(login);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/login/{id}")
    public void Login(@PathVariable Long id){
        loginRepository.deleteById(id);
    }
}
