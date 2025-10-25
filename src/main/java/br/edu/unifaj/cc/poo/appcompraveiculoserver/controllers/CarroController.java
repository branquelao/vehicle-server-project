package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.CarroRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CarroController {

    private final CarroRepository carroRepository;

    public CarroController(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

    @GetMapping("/veiculos/carro")
    public List<Carro> getCarros() {
        return carroRepository.findAll();
    }

    @GetMapping("/veiculos/carro/{id}")
    public Carro getCarroById(@PathVariable Long id) {
        return carroRepository.findById(id).orElse(null);
    }

    @PostMapping("/veiculos/carro")
    public Carro postCarro(@RequestBody Carro c) {
        return carroRepository.save(c);
    }

    @PutMapping("/veiculos/carro/{id}")
    public Carro putCarro(@RequestBody Carro novo, @PathVariable Long id){
        return carroRepository.findById(id)
                .map(c -> {
                    c.setCarroNome(c.getCarroNome());
                    c.setCarroCor(c.getCarroCor());
                    c.setCarroAno(c.getCarroAno());
                    c.setCarroValor(c.getCarroValor());
                    return carroRepository.save(c);
                })
                .orElse(null);
    }

    @DeleteMapping("/veiculos/carro/{id}")
    public void deleteCarro(@PathVariable long id) {
        carroRepository.deleteById(id);
    }
}
