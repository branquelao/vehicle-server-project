package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.MotoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Moto;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.ImagemInvalidaException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.MotoRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class MotoService {

    private final MotoRepository motoRepository;
    private final LoginRepository loginRepository;

    public MotoService(MotoRepository motoRepository, LoginRepository loginRepository) {
        this.motoRepository = motoRepository;
        this.loginRepository = loginRepository;
    }

    public List<Moto> listarTodos() {
        return motoRepository.findAll();
    }

    public Optional<Moto> buscarPorId(Long id) {
        return motoRepository.findById(id);
    }

    public List<Moto> listarRecentes() {
        return motoRepository.findTop3ByOrderByIdDesc();
    }

    public Moto criar(MotoDTO dto, Path uploadDir) {
        validarImagemExiste(dto.getMotoImagem(), uploadDir);

        if (!loginRepository.existsById(dto.getLoginId())) {
            throw new RecursoNaoEncontradoException("Login não encontrado: " + dto.getLoginId());
        }

        Moto moto = new Moto();
        moto.setMotoNome(dto.getMotoNome());
        moto.setMotoCor(dto.getMotoCor());
        moto.setMotoAno(dto.getMotoAno());
        moto.setMotoValor(dto.getMotoValor());
        moto.setMotoImagem(dto.getMotoImagem());
        moto.setLoginId(dto.getLoginId());

        return motoRepository.save(moto);
    }

    public Moto atualizar(Long id, MotoDTO novoDto, Path uploadDir) {
        Moto moto = motoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Moto não encontrada: " + id));

        String imagemAntiga = moto.getMotoImagem();
        String novaImagem = novoDto.getMotoImagem();

        if (novaImagem != null && !novaImagem.isEmpty() && !novaImagem.equals(imagemAntiga)) {
            try {
                UploadPathResolver.apagarSeExistir(uploadDir, imagemAntiga);
            } catch (IOException e) {
                System.err.println("Erro ao remover imagem antiga da moto: " + e.getMessage());
            }
            moto.setMotoImagem(novaImagem);
        }

        moto.setMotoNome(novoDto.getMotoNome());
        moto.setMotoCor(novoDto.getMotoCor());
        moto.setMotoAno(novoDto.getMotoAno());
        moto.setMotoValor(novoDto.getMotoValor());

        return motoRepository.save(moto);
    }

    public void deletar(Long id, Path uploadDir) {
        Moto moto = motoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Moto não encontrada: " + id));

        try {
            UploadPathResolver.apagarSeExistir(uploadDir, moto.getMotoImagem());
        } catch (IOException e) {
            System.err.println("Erro ao remover imagem da moto: " + e.getMessage());
        }

        motoRepository.deleteById(id);
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