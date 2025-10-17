package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dao.LoginDAO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LoginController {

    @Autowired
    LoginDAO dao;


    @GetMapping("/login")
    public List<Login> getLogins(){
        return dao.getLogins();
    }

    @GetMapping("/login/{id}")
    public Login getLoginId(@PathVariable Integer id){
        return dao.getLoginId(id);
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

    @PostMapping("/login")
    public Login postLogin(@RequestBody Login p) {
        return dao.postLogin(p);
    }

    @PutMapping("/login/{id}")
    public Login putLogin(@RequestBody Login p, @PathVariable Integer id)
            throws Exception{
        return dao.putLogin(p, id);
    }

}
