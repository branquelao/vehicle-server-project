package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.MotoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.ImagemInvalidaException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.MotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class MotoController {

    private final MotoService motoService;

    public MotoController(MotoService motoService) {
        this.motoService = motoService;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    @GetMapping("/veiculos/moto")
    public List<Moto> getMotos() {
        return motoService.listarTodos();
    }

    @GetMapping("/veiculos/moto/{id}")
    public ResponseEntity<Moto> getMotoById(@PathVariable Long id) {
        return motoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/veiculos/moto/recentes")
    public List<Moto> ultimasMotos() {
        return motoService.listarRecentes();
    }

    @PostMapping("/veiculos/moto")
    public ResponseEntity<?> postMoto(@RequestBody MotoDTO dto) {
        try {
            Moto salva = motoService.criar(dto, uploadDir());
            return ResponseEntity.status(HttpStatus.CREATED).body(salva);
        } catch (ImagemInvalidaException | RecursoNaoEncontradoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/veiculos/moto/{id}")
    public ResponseEntity<?> putMoto(@RequestBody MotoDTO novoDto, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(motoService.atualizar(id, novoDto, uploadDir()));
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/veiculos/moto/{id}")
    public ResponseEntity<Object> deleteMoto(@PathVariable Long id) {
        try {
            motoService.deletar(id, uploadDir());
            return ResponseEntity.noContent().build();
        } catch (RecursoNaoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }
}