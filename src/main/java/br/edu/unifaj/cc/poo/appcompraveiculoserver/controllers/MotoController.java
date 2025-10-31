package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.MotoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.MotoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /*
    @PostMapping("/veiculos/moto")
    public Moto postMoto(@RequestBody Moto m) {
        return motoRepository.save(m);
    }
    */

    @PostMapping("/veiculos/moto")
    public ResponseEntity<?> postMoto(@RequestBody MotoDTO dto) {
        Path pastaUploads = Paths.get("src/main/resources/static/uploads");
        Path caminhoArquivo = pastaUploads.resolve(dto.getMotoImagem());

        if (!Files.exists(caminhoArquivo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ A imagem '" + dto.getMotoImagem() + "' não foi encontrada em /uploads/");
        }

        Moto moto = new Moto();
        moto.setMotoNome(dto.getMotoNome());
        moto.setMotoCor(dto.getMotoCor());
        moto.setMotoAno(dto.getMotoAno());
        moto.setMotoValor(dto.getMotoValor());
        moto.setMotoImagem(dto.getMotoImagem());

        motoRepository.save(moto);

        return ResponseEntity.ok("✅ Moto salva com sucesso!");
    }
    
    @PutMapping("/veiculos/moto/{id}")
    public Moto putMoto(@RequestBody MotoDTO novoDTO, @PathVariable Long id) {
            return motoRepository.findById(id)
                    .map(m -> {
                        m.setMotoNome(novoDTO.getMotoNome());
                        m.setMotoCor(novoDTO.getMotoCor());
                        m.setMotoAno(novoDTO.getMotoAno());
                        m.setMotoValor(novoDTO.getMotoValor());
                        return motoRepository.save(m);
                    })
                    .orElse(null);
    }

    @DeleteMapping("/veiculos/moto/{id}")
    public ResponseEntity<?> deleteMoto(@PathVariable Long id) {
        motoRepository.deleteById(id);
        return ResponseEntity.ok("✅ Moto de ID = " + id + " deletada com sucesso!");
    }
}
