package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Favorito;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.FavoritoRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.VeiculoRepository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final VeiculoRepository veiculoRepository;
    private final LoginRepository loginRepository;

    public FavoritoService(FavoritoRepository favoritoRepository, VeiculoRepository veiculoRepository, LoginRepository loginRepository) {
        this.favoritoRepository = favoritoRepository;
        this.veiculoRepository = veiculoRepository;
        this.loginRepository = loginRepository;
    }

    private Login usuarioLogado() {
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return loginRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado"));
    }

    @Transactional
    public void favoritar(Long veiculoId) {
        Login login = usuarioLogado();
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado: " + veiculoId));

        if (veiculo.getLogin().getId().equals(login.getId())) {
            throw new ValidationException("Não é possível favoritar o próprio anúncio");
        }

        if (favoritoRepository.existsByLogin_IdAndVeiculo_Id(login.getId(), veiculoId)) {
            return; // já favoritado — idempotente
        }

        favoritoRepository.save(new Favorito(login, veiculo));
    }

    @Transactional
    public void desfavoritar(Long veiculoId) {
        Login login = usuarioLogado();
        favoritoRepository.deleteByLogin_IdAndVeiculo_Id(login.getId(), veiculoId);
    }

    public List<Favorito> listarFavoritos() {
        Login login = usuarioLogado();
        return favoritoRepository.findByLogin_IdOrderByCriadoEmDesc(login.getId());
    }
}