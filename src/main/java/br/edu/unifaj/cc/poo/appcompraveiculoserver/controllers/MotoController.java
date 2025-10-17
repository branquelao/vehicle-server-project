package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dao.MotoDAO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MotoController {

    @Autowired
    MotoDAO dao;

    @GetMapping("/veiculos/moto")
    public List<Moto> getMotos() {
        return dao.getMotos();
    }

    @GetMapping("/veiculos/moto/{id}")
    public Moto getMotoById(@PathVariable int id) {
        return dao.getMotoById(id);
    }

    @PostMapping("/veiculos/moto")
    public Moto postMoto(@RequestBody Moto p) {
        return dao.postMoto(p);
    }

    @PutMapping("/veiculos/moto/{id}")
    public Moto putMoto(@RequestBody  Moto p, @PathVariable Integer id)
            throws Exception {
        return dao.putMoto(p, id);
    }

    @DeleteMapping("/veiculos/moto/{id}")
    public Moto deleteMoto(@PathVariable Integer id) {
        return dao.deleteMoto(id);
    }

}
