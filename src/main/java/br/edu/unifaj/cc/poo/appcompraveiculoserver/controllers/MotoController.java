package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.MotoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MotoController {

    private final MotoRepository motoRepository;

    public MotoController(MotoRepository motoRepository) {
        this.motoRepository = motoRepository;
    }

    @GetMapping("/veiculos/moto")
    public List<Moto> getMotos() {
        return motoRepository.findAll();
    }

    @GetMapping("/veiculos/moto/{id}")
    public Moto getMotoById(@PathVariable Long id) {
        return motoRepository.findById(id).orElse(null);
    }

    @PostMapping("/veiculos/moto")
    public Moto postMoto(@RequestBody Moto m) {
        return motoRepository.save(m);
    }

    @PutMapping("/veiculos/moto/{id}")
    public Moto putMoto(@RequestBody Moto nova, @PathVariable Long id) {
            return motoRepository.findById(id)
                    .map(m -> {
                        m.setMotoNome(m.getMotoNome());
                        m.setMotoCor(m.getMotoCor());
                        m.setMotoAno(m.getMotoAno());
                        m.setMotoValor(m.getMotoValor());
                        return motoRepository.save(m);
                    })
                    .orElse(null);
    }

    @DeleteMapping("/veiculos/moto/{id}")
    public void deleteMoto(@PathVariable Long id) {
        motoRepository.deleteById(id);
    }
}
