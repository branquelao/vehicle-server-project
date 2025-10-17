package br.edu.unifaj.cc.poo.appcompraveiculoserver.dao;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Repository
public class LoginDAO {
    Map<Integer, Login> loginMap = new TreeMap<>();

    public LoginDAO() {
        Login u1 = new Login(1, "henrique", "henrique", 1234, 1000);
        loginMap.put(u1.getId(), u1);
    }

    public List<Login> getLogins() {
        return new ArrayList<>(loginMap.values());
    }

    public Login getLoginId(int id){
        return loginMap.get(id);
    }

    public Login postLogin(Login p) {
        p.setId(nextId());
        loginMap.put(p.getId(), p);
        return p;
    }

    //SÓ DA CARTEIRA
    public Login putLogin(Login p, Integer id) throws Exception{
        Login existente = loginMap.get(id);
        if (existente == null) {
            throw new Exception("Login não existe!");
        }
        existente.setCarteira(p.getCarteira());
        return existente;
    }

    /*
    public Login getUsuarioByLoginSenha(String nome, String senha) {
        for (Login u : loginMap.values()) {
            if (u.getNome().equals(nome)
                    && u.getSenha().equals(senha)) {
                return u;
            }
        }
        return null;
    }
    */

    public int nextId() {
        int max = 1;
        for (int id : loginMap.keySet()) {
            if (max <= id) {
                max = id + 1;
            }
        }
        return max;
    }
}
