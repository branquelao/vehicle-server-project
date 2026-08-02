package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.NovaAvaliacaoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.ResumoAvaliacaoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Avaliacao;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.AvaliacaoRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.ConversaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final LoginRepository loginRepository;
    private final ConversaRepository conversaRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, LoginRepository loginRepository,
                            ConversaRepository conversaRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.loginRepository = loginRepository;
        this.conversaRepository = conversaRepository;
    }

    private Login usuarioLogado() {
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return loginRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado"));
    }

    @Transactional
    public Avaliacao avaliar(Long vendedorId, NovaAvaliacaoDTO dto) {
        Login avaliador = usuarioLogado();
        Login vendedor = loginRepository.findById(vendedorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vendedor não encontrado: " + vendedorId));

        if (avaliador.getId().equals(vendedorId)) {
            throw new ValidationException("Não é possível avaliar a si mesmo");
        }

        if (!conversaRepository.existsByComprador_IdAndVendedor_Id(avaliador.getId(), vendedorId)) {
            throw new ValidationException("Só é possível avaliar vendedores com quem você já conversou");
        }

        Avaliacao avaliacao = avaliacaoRepository.findByAvaliador_IdAndAvaliado_Id(avaliador.getId(), vendedorId)
                .orElseGet(() -> new Avaliacao(avaliador, vendedor, dto.getNota(), dto.getComentario()));

        avaliacao.setNota(dto.getNota());
        avaliacao.setComentario(dto.getComentario());

        return avaliacaoRepository.save(avaliacao);
    }

    public List<Avaliacao> listarPorVendedor(Long vendedorId) {
        return avaliacaoRepository.findByAvaliado_IdOrderByCriadaEmDesc(vendedorId);
    }

    public ResumoAvaliacaoDTO resumo(Long vendedorId) {
        Double media = avaliacaoRepository.calcularMediaPorAvaliado(vendedorId);
        long total = avaliacaoRepository.countByAvaliado_Id(vendedorId);
        return new ResumoAvaliacaoDTO(media != null ? media : 0.0, total);
    }
}