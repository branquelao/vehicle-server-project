package br.edu.unifaj.cc.poo.appcompraveiculoserver.dao;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Repository
public class CarroDAO {

    Map<Integer, Carro> Carros = new TreeMap<>();

    public CarroDAO() {
        Carro p1 = new Carro(1, "Maverick V8", "Azul", 1978, 55000f);
        Carro p2 = new Carro(2, "Opala Diplomata", "Preto", 1985, 30000f);
        Carro p3 = new Carro(3, "Fiat Uno", "Vermelho", 2006, 15000f);
        Carros.put(p1.getId(), p1);
        Carros.put(p2.getId(), p2);
        Carros.put(p3.getId(), p3);
    }

    public List<Carro> getCarros() {
        return new ArrayList<>(Carros.values());
    }

    public Carro getCarroById(int id) {
        return Carros.get(id);
    }

    public Carro putCarro(Carro p, Integer id)
            throws Exception {
        Carro existente = Carros.get(id);
        if (existente == null) {
            throw new Exception("Carro não existe");
        }
        existente.setCarroNome(p.getCarroNome());
        existente.setCarroCor(p.getCarroCor());
        existente.setCarroAno(p.getCarroAno());
        existente.setCarroValor(p.getCarroValor());
        return existente;
    }

    public Carro deleteCarro(Integer id) {
        return Carros.remove(id);
    }

    public Carro postCarro(Carro p) {
        p.setId(nextId());
        Carros.put(p.getId(), p);
        return p;
    }


    public int nextId() {
        int max = 1;
        for (int id : Carros.keySet()) {
            if (max <= id) {
                max = id + 1;
            }
        }
        return max;
    }
}
