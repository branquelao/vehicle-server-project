package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.CarroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.CarroRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@RestController
public class CarroController {

    private final CarroRepository carroRepository;
    private final LoginRepository loginRepository;

    public CarroController(CarroRepository carroRepository, LoginRepository loginRepository) {
        this.carroRepository = carroRepository;
        this.loginRepository = loginRepository;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
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
        Path caminhoArquivo;
        try {
            caminhoArquivo = UploadPathResolver.resolveDentroDeUploads(uploadDir(), dto.getCarroImagem());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nome de imagem inválido.");
        }

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
        return ResponseEntity.status(HttpStatus.CREATED).body("Carro salvo com sucesso!");
    }

    @PostMapping("/uploads")
    public ResponseEntity<Map<String, String>> uploadImagem(@RequestParam("file") MultipartFile file) {
        try {
            Path pastaUploads = uploadDir();
            if (!Files.exists(pastaUploads)) {
                Files.createDirectories(pastaUploads);
            }

            String nomeArquivo = UploadPathResolver.gerarNomeSeguro(file.getOriginalFilename());
            Path destino = UploadPathResolver.resolveDentroDeUploads(pastaUploads, nomeArquivo);
            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(Map.of("arquivo", nomeArquivo));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao enviar a imagem: " + e.getMessage()));
        }
    }

    @PutMapping("/veiculos/carro/{id}")
    public Carro putCarro(@RequestBody CarroDTO novoDto, @PathVariable Long id) {
        return carroRepository.findById(id)
                .map(c -> {
                    // Verifica se a imagem mudou
                    String imagemAntiga = c.getCarroImagem();
                    String novaImagem = novoDto.getCarroImagem();

                    if (novaImagem != null && !novaImagem.isEmpty() && !novaImagem.equals(imagemAntiga)) {
                        try {
                            UploadPathResolver.apagarSeExistir(uploadDir(), imagemAntiga);
                        } catch (IOException e) {
                            System.err.println("Erro ao remover imagem antiga: " + e.getMessage());
                        }
                        c.setCarroImagem(novaImagem);
                    }

                    // Atualiza os outros campos normalmente
                    c.setCarroNome(novoDto.getCarroNome());
                    c.setCarroCor(novoDto.getCarroCor());
                    c.setCarroAno(novoDto.getCarroAno());
                    c.setCarroValor(novoDto.getCarroValor());

                    return carroRepository.save(c);
                })
                .orElse(null);
    }

    @DeleteMapping("/veiculos/carro/{id}")
    public ResponseEntity<Object> deleteCarro(@PathVariable Long id) {
        return carroRepository.findById(id)
                .map(carro -> {
                    // Exclui a imagem antiga, se existir
                    try {
                        UploadPathResolver.apagarSeExistir(uploadDir(), carro.getCarroImagem());
                    } catch (IOException e) {
                        System.err.println("Erro ao remover imagem do carro: " + e.getMessage());
                    }

                    carroRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
