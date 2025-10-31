package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.CarroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.CarroRepository;
import jakarta.annotation.Resource;
import org.apache.coyote.Response;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    /*
    @GetMapping("/uploads/{nomeArquivo}")
    public ResponseEntity<Resource> getCarroImagem(@PathVariable String nomeArquivo){
        try {
            Path caminho = Paths.get("src/main/resources/static/uploads").resolve(nomeArquivo);
            if(!Files.exists(caminho)) {
                return ResponseEntity.notFound().build();
            }

            UrlResource resource = new UrlResource(caminho.toUri());
            String contentType = Files.probeContentType(caminho);

            if(contentType == null ||
                    !(contentType.equals("image/jpeg") || contentType.equals("image/png") ||
                            contentType.equals("image/webp"))) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body((Resource) resource);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/veiculos/carro")
    public Carro postCarro(@RequestBody Carro c) {
        return carroRepository.save(c);
    }
     */

    @PostMapping("/veiculos/carro")
    public ResponseEntity<?> postCarro(@RequestBody CarroDTO dto) {

        // Caminho absoluto para a pasta de uploads
        Path pastaUploads = Paths.get("src/main/resources/static/uploads");

        // Caminho completo do arquivo informado
        Path caminhoArquivo = pastaUploads.resolve(dto.getCarroImagem());

        // Verifica se o arquivo realmente existe
        if (!Files.exists(caminhoArquivo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ A imagem '" + dto.getCarroImagem() + "' não foi encontrada em /uploads/");
        }

        // Se o arquivo existe, cria e salva o carro normalmente
        Carro carro = new Carro();
        carro.setCarroNome(dto.getCarroNome());
        carro.setCarroCor(dto.getCarroCor());
        carro.setCarroAno(dto.getCarroAno());
        carro.setCarroValor(dto.getCarroValor());
        carro.setCarroImagem(dto.getCarroImagem()); // salva o nome do arquivo

        carroRepository.save(carro);

        return ResponseEntity.ok("✅ Carro salvo com sucesso!");
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
