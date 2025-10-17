package br.edu.unifaj.cc.poo.appcompraveiculoserver.dao;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Repository
public class MotoDAO {

    Map<Integer, Moto> Motos = new TreeMap<>();

    public MotoDAO() {
        Moto p1 = new Moto(1, "Hornet 600", "Vermelha", 2011, 25000f);
        Moto p2 = new Moto(2, "Twister 250", "Prata", 2008, 10000f);
        Moto p3 = new Moto(3, "Intruder 125", "Preta", 2002, 6000f);
        Motos.put(p1.getId(), p1);
        Motos.put(p2.getId(), p2);
        Motos.put(p3.getId(), p3);
    }

    public List<Moto> getMotos() {
        return new ArrayList<>(Motos.values());
    }

    public Moto getMotoById(int id) {
        return Motos.get(id);
    }

    public Moto putMoto(Moto p, Integer id)
            throws Exception {
        Moto existente = Motos.get(id);
        if (existente == null) {
            throw new Exception("Moto não existe");
        }
        existente.setMotoNome(p.getMotoNome());
        existente.setMotoCor(p.getMotoCor());
        existente.setMotoAno(p.getMotoAno());
        existente.setMotoValor(p.getMotoValor());
        return existente;
    }

    public Moto deleteMoto(Integer id) {
        return Motos.remove(id);
    }

    public Moto postMoto(Moto p) {
        p.setId(nextId());
        Motos.put(p.getId(), p);
        return p;
    }

    public int nextId() {
        int max = 1;
        for (int id : Motos.keySet()) {
            if (max <= id) {
                max = id + 1;
            }
        }
        return max;
    }
}
