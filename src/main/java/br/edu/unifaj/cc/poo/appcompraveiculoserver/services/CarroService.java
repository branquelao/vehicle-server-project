package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.CarroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Carro;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.ImagemInvalidaException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.CarroRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class CarroService {

    private final CarroRepository carroRepository;
    private final LoginRepository loginRepository;

    public CarroService(CarroRepository carroRepository, LoginRepository loginRepository) {
        this.carroRepository = carroRepository;
        this.loginRepository = loginRepository;
    }

    public List<Carro> listarTodos() {
        return carroRepository.findAll();
    }

    public Optional<Carro> buscarPorId(Long id) {
        return carroRepository.findById(id);
    }

    public List<Carro> listarRecentes() {
        return carroRepository.findTop3ByOrderByIdDesc();
    }

    public Carro criar(CarroDTO dto, Path uploadDir) {
        validarImagemExiste(dto.getCarroImagem(), uploadDir);

        if (!loginRepository.existsById(dto.getLoginId())) {
            throw new RecursoNaoEncontradoException("Login não encontrado: " + dto.getLoginId());
        }

        Carro carro = new Carro();
        carro.setCarroNome(dto.getCarroNome());
        carro.setCarroCor(dto.getCarroCor());
        carro.setCarroAno(dto.getCarroAno());
        carro.setCarroValor(dto.getCarroValor());
        carro.setCarroImagem(dto.getCarroImagem());
        carro.setLoginId(dto.getLoginId());

        return carroRepository.save(carro);
    }

    public Carro atualizar(Long id, CarroDTO novoDto, Path uploadDir) {
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carro não encontrado: " + id));

        String imagemAntiga = carro.getCarroImagem();
        String novaImagem = novoDto.getCarroImagem();

        if (novaImagem != null && !novaImagem.isEmpty() && !novaImagem.equals(imagemAntiga)) {
            try {
                UploadPathResolver.apagarSeExistir(uploadDir, imagemAntiga);
            } catch (IOException e) {
                System.err.println("Erro ao remover imagem antiga: " + e.getMessage());
            }
            carro.setCarroImagem(novaImagem);
        }

        carro.setCarroNome(novoDto.getCarroNome());
        carro.setCarroCor(novoDto.getCarroCor());
        carro.setCarroAno(novoDto.getCarroAno());
        carro.setCarroValor(novoDto.getCarroValor());

        return carroRepository.save(carro);
    }

    public void deletar(Long id, Path uploadDir) {
        Carro carro = carroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carro não encontrado: " + id));

        try {
            UploadPathResolver.apagarSeExistir(uploadDir, carro.getCarroImagem());
        } catch (IOException e) {
            System.err.println("Erro ao remover imagem do carro: " + e.getMessage());
        }

        carroRepository.deleteById(id);
    }

    private void validarImagemExiste(String nomeImagem, Path uploadDir) {
        Path caminho;
        try {
            caminho = UploadPathResolver.resolveDentroDeUploads(uploadDir, nomeImagem);
        } catch (IllegalArgumentException e) {
            throw new ImagemInvalidaException("Nome de imagem inválido: " + nomeImagem);
        }
        if (!Files.exists(caminho)) {
            throw new ImagemInvalidaException("A imagem '" + nomeImagem + "' não foi encontrada em /uploads/");
        }
    }
}