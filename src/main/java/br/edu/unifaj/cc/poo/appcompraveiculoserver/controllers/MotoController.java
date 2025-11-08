package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.MotoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.MotoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class MotoController {

    private final MotoRepository motoRepository;
    private final LoginRepository loginRepository;

    public MotoController(MotoRepository motoRepository, LoginRepository loginRepository) {
        this.motoRepository = motoRepository;
        this.loginRepository = loginRepository;
    }

    @GetMapping("/veiculos/moto")
    public List<Moto> getMotos() {
        return motoRepository.findAll();
    }

    @GetMapping("/veiculos/moto/{id}")
    public Moto getMotoById(@PathVariable Long id) {
        return motoRepository.findById(id).orElse(null);
    }

    @GetMapping("veiculos/moto/recentes")
    public List<Moto> ultimasMotos() {
        return motoRepository.findTop3ByOrderByIdDesc();
    }

    @PostMapping("/veiculos/moto")
    public ResponseEntity<?> postMoto(@RequestBody MotoDTO dto) {
        Path pastaUploads = Paths.get(System.getProperty("user.dir"), "uploads");
        Path caminhoArquivo = pastaUploads.resolve(dto.getMotoImagem());

        if (!Files.exists(caminhoArquivo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A imagem '" + dto.getMotoImagem() + "' não foi encontrada em /uploads/");
        }

        Login login = loginRepository.findById(dto.getLoginId())
                .orElseThrow(() -> new RuntimeException("Login não encontrado"));

        Moto moto = new Moto();
        moto.setMotoNome(dto.getMotoNome());
        moto.setMotoCor(dto.getMotoCor());
        moto.setMotoAno(dto.getMotoAno());
        moto.setMotoValor(dto.getMotoValor());
        moto.setMotoImagem(dto.getMotoImagem());
        moto.setLogin(login);

        motoRepository.save(moto);

        return ResponseEntity.ok("Moto salva com sucesso!");
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
        return ResponseEntity.ok("Moto de ID = " + id + " deletada com sucesso!");
    }
}
