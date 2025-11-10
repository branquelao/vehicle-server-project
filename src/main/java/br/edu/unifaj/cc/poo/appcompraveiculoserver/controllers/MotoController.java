package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.MotoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.MotoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
                    // Verifica se a imagem foi alterada
                    String imagemAntiga = m.getMotoImagem();
                    String novaImagem = novoDTO.getMotoImagem();

                    if (novaImagem != null && !novaImagem.isEmpty() && !novaImagem.equals(imagemAntiga)) {
                        try {
                            Path uploadDir = Paths.get("uploads");
                            Path caminhoAntigo = uploadDir.resolve(imagemAntiga);

                            // Exclui o arquivo antigo se existir
                            Files.deleteIfExists(caminhoAntigo);

                            System.out.println("Imagem antiga de moto removida: " + caminhoAntigo);
                        } catch (IOException e) {
                            System.err.println("Erro ao remover imagem antiga da moto: " + e.getMessage());
                        }

                        m.setMotoImagem(novaImagem);
                    }

                    // Atualiza os outros campos normalmente
                    m.setMotoNome(novoDTO.getMotoNome());
                    m.setMotoCor(novoDTO.getMotoCor());
                    m.setMotoAno(novoDTO.getMotoAno());
                    m.setMotoValor(novoDTO.getMotoValor());

                    return motoRepository.save(m);
                })
                .orElse(null);
    }

    @DeleteMapping("/veiculos/moto/{id}")
    public ResponseEntity<Object> deleteMoto(@PathVariable Long id) {
        return motoRepository.findById(id)
                .map(moto -> {
                    // Exclui a imagem antiga, se existir
                    try {
                        if (moto.getMotoImagem() != null && !moto.getMotoImagem().isEmpty()) {
                            Path uploadDir = Paths.get("uploads");
                            Path caminhoImagem = uploadDir.resolve(moto.getMotoImagem());
                            Files.deleteIfExists(caminhoImagem);
                            System.out.println("🗑️ Imagem da moto removida: " + caminhoImagem);
                        }
                    } catch (IOException e) {
                        System.err.println("⚠️ Erro ao remover imagem da moto: " + e.getMessage());
                    }

                    motoRepository.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
