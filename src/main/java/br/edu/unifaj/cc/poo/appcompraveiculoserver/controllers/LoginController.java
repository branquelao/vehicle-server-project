package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @DeleteMapping("/login/{id}")
    public void Login(@PathVariable Long id){
        loginRepository.deleteById(id);
    }
}
