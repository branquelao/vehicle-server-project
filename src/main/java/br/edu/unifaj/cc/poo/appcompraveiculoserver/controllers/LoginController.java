package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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
    */

    @GetMapping("/login/{id}")
    public Login getLoginId(@PathVariable Long id){
        return loginRepository.findById(id).orElse(null);
    }

    @PostMapping("/login")
    public Login postLogin(@RequestBody Login l) {
        return loginRepository.save(l);
    }

    @PutMapping("/login/{id}")
    public Login putLogin(@RequestBody Login p, @PathVariable Long id){
            return loginRepository.findById(id)
                    .map(l -> {
                        l.setNome(l.getNome());
                        l.setSenha(l.getSenha());
                        l.setTelefone(l.getTelefone());
                        l.setCarteira(l.getCarteira());
                        return loginRepository.save(l);
                    })
                    .orElse(null);
    }

    @DeleteMapping("/login/{id}")
    public void Login(@PathVariable Long id){
        loginRepository.deleteById(id);
    }
}
