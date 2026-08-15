package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva.NovaBuscaSalvaDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Alerta;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.BuscaSalva;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.AlertaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.BuscaSalvaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuscaSalvaService {

    private final BuscaSalvaRepository buscaSalvaRepository;
    private final AlertaRepository alertaRepository;
    private final LoginRepository loginRepository;

    public BuscaSalvaService(BuscaSalvaRepository buscaSalvaRepository, AlertaRepository alertaRepository,
                             LoginRepository loginRepository) {
        this.buscaSalvaRepository = buscaSalvaRepository;
        this.alertaRepository = alertaRepository;
        this.loginRepository = loginRepository;
    }

    private Login usuarioLogado() {
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return loginRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado"));
    }

    @Transactional
    public BuscaSalva criar(NovaBuscaSalvaDTO dto) {
        Login login = usuarioLogado();

        BuscaSalva busca = new BuscaSalva();
        busca.setLogin(login);
        busca.setTipo(dto.getTipo());
        busca.setMarca(dto.getMarca());
        busca.setModelo(dto.getModelo());
        busca.setPrecoMin(dto.getPrecoMin());
        busca.setPrecoMax(dto.getPrecoMax());
        busca.setAnoMin(dto.getAnoMin());
        busca.setAnoMax(dto.getAnoMax());
        busca.setKmMax(dto.getKmMax());
        busca.setCor(dto.getCor());
        busca.setCidade(dto.getCidade());
        busca.setEstado(dto.getEstado());

        return buscaSalvaRepository.save(busca);
    }

    public List<BuscaSalva> listarMinhas() {
        Long loginId = usuarioLogado().getId();
        return buscaSalvaRepository.findByLogin_IdOrderByCriadaEmDesc(loginId);
    }

    @Transactional
    public void deletar(Long id) {
        BuscaSalva busca = buscaSalvaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Busca salva não encontrada: " + id));

        verificarDono(busca.getLogin());

        buscaSalvaRepository.deleteById(id);
    }

    public List<Alerta> listarAlertas() {
        Long loginId = usuarioLogado().getId();
        return alertaRepository.findByBuscaSalva_Login_IdOrderByCriadoEmDesc(loginId);
    }

    @Transactional
    public Alerta marcarVisualizado(Long alertaId) {
        Alerta alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Alerta não encontrado: " + alertaId));

        verificarDono(alerta.getBuscaSalva().getLogin());

        alerta.setVisualizado(true);
        return alertaRepository.save(alerta);
    }

    private void verificarDono(Login donoDaBusca) {
        Long loginId = usuarioLogado().getId();
        if (!donoDaBusca.getId().equals(loginId)) {
            throw new AccessDeniedException("Você não tem permissão para acessar este recurso");
        }
    }
}