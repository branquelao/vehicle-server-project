package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.CarroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.CarroRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import jakarta.annotation.Resource;
import org.apache.coyote.Response;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class CarroController {

    private final CarroRepository carroRepository;
    private final LoginRepository loginRepository;

    public CarroController(CarroRepository carroRepository, LoginRepository loginRepository) {
        this.carroRepository = carroRepository;
        this.loginRepository = loginRepository;
    }

    @GetMapping("/veiculos/carro")
    public List<Carro> getCarros() {
        return carroRepository.findAll();
    }

    @GetMapping("/veiculos/carro/{id}")
    public Carro getCarroById(@PathVariable Long id) {
        return carroRepository.findById(id).orElse(null);
    }

    @GetMapping("veiculos/carro/recentes")
    public List<Carro> ultimosCarros() {
        return carroRepository.findTop3ByOrderByIdDesc();
    }

    @PostMapping("/veiculos/carro")
    public ResponseEntity<?> postCarro(@RequestBody CarroDTO dto) {
        Path pastaUploads = Paths.get("src/main/resources/static/uploads");
        Path caminhoArquivo = pastaUploads.resolve(dto.getCarroImagem());

        if (!Files.exists(caminhoArquivo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A imagem '" + dto.getCarroImagem() + "' não foi encontrada em /uploads/");
        }

        Login login = loginRepository.findById(dto.getLoginId())
                .orElseThrow(() -> new RuntimeException("Login não encontrado"));

        Carro carro = new Carro();
        carro.setCarroNome(dto.getCarroNome());
        carro.setCarroCor(dto.getCarroCor());
        carro.setCarroAno(dto.getCarroAno());
        carro.setCarroValor(dto.getCarroValor());
        carro.setCarroImagem(dto.getCarroImagem());
        carro.setLogin(login);

        carroRepository.save(carro);
        return ResponseEntity.ok("Carro salvo com sucesso!");
    }

    //Upload das imagens para a pasta correta
    @PostMapping("/uploads")
    public ResponseEntity<String> uploadImagem(@RequestParam("file") MultipartFile file) {
        try {
            Path pastaUploads = Paths.get("src/main/resources/static/uploads");

            if (!Files.exists(pastaUploads)) {
                Files.createDirectories(pastaUploads);
            }

            Path destino = pastaUploads.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), destino);

            return ResponseEntity.ok("Imagem enviada com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar a imagem: " + e.getMessage());
        }
    }

    @PutMapping("/veiculos/carro/{id}")
    public Carro putCarro(@RequestBody CarroDTO novoDto, @PathVariable Long id){
        return carroRepository.findById(id)
                .map(c -> {
                    c.setCarroNome(novoDto.getCarroNome());
                    c.setCarroCor(novoDto.getCarroCor());
                    c.setCarroAno(novoDto.getCarroAno());
                    c.setCarroValor(novoDto.getCarroValor());
                    return carroRepository.save(c);
                })
                .orElse(null);
    }

    @DeleteMapping("/veiculos/carro/{id}")
    public ResponseEntity<?> deleteCarro(@PathVariable long id) {
        carroRepository.deleteById(id);
        return ResponseEntity.ok("Carro de ID = " + id + " deletado com sucesso!");
    }
}
