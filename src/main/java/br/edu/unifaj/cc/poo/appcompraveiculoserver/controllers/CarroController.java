package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dao.CarroDAO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CarroController {

    @Autowired
    CarroDAO dao;

    @GetMapping("/veiculos/carro")
    public List<Carro> getCarros() {
        return dao.getCarros();
    }

    @GetMapping("/veiculos/carro/{id}")
    public Carro getCarroById(@PathVariable int id) {
        return dao.getCarroById(id);
    }

    @PostMapping("/veiculos/carro")
    public Carro postCarro(@RequestBody Carro p) {
        return dao.postCarro(p);
    }

    @PutMapping("/veiculos/carro/{id}")
    public Carro putCarro(@RequestBody  Carro p, @PathVariable Integer id)
            throws Exception {
        return dao.putCarro(p, id);
    }

    @DeleteMapping("/veiculos/carro/{id}")
    public Carro deleteCarro(@PathVariable Integer id) {
        return dao.deleteCarro(id);
    }

}
