package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.mensagem.NovaMensagemDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Conversa;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Mensagem;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.ConversaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.MensagemRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.VeiculoRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MensagemService {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final VeiculoRepository veiculoRepository;
    private final LoginRepository loginRepository;

    public MensagemService(ConversaRepository conversaRepository, MensagemRepository mensagemRepository,
                           VeiculoRepository veiculoRepository, LoginRepository loginRepository) {
        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.veiculoRepository = veiculoRepository;
        this.loginRepository = loginRepository;
    }

    private Login usuarioLogado() {
        String usuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return loginRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado"));
    }

    private void verificarParticipante(Conversa conversa) {
        Long loginId = usuarioLogado().getId();
        if (!conversa.temParticipante(loginId)) {
            throw new AccessDeniedException("Você não faz parte desta conversa");
        }
    }

    @Transactional
    public Conversa iniciarOuContinuar(Long veiculoId, NovaMensagemDTO dto) {
        Login comprador = usuarioLogado();
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado: " + veiculoId));

        if (veiculo.getLogin().getId().equals(comprador.getId())) {
            throw new ValidationException("Não é possível iniciar uma conversa sobre o próprio anúncio");
        }

        Conversa conversa = conversaRepository.findByVeiculo_IdAndComprador_Id(veiculoId, comprador.getId())
                .orElseGet(() -> {
                    Conversa nova = new Conversa();
                    nova.setVeiculo(veiculo);
                    nova.setComprador(comprador);
                    nova.setVendedor(veiculo.getLogin());
                    return conversaRepository.save(nova);
                });

        adicionarMensagem(conversa, comprador, dto.getConteudo());
        return conversa;
    }

    @Transactional
    public Conversa responder(Long conversaId, NovaMensagemDTO dto) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada: " + conversaId));

        verificarParticipante(conversa);
        adicionarMensagem(conversa, usuarioLogado(), dto.getConteudo());
        return conversa;
    }

    private void adicionarMensagem(Conversa conversa, Login remetente, String conteudo) {
        Mensagem mensagem = new Mensagem(conversa, remetente, conteudo);
        mensagemRepository.save(mensagem);
        conversa.setAtualizadaEm(LocalDateTime.now());
        conversaRepository.save(conversa);
    }

    public List<Conversa> listarConversas() {
        Long loginId = usuarioLogado().getId();
        return conversaRepository.findByComprador_IdOrVendedor_IdOrderByAtualizadaEmDesc(loginId, loginId);
    }

    public List<Mensagem> listarMensagens(Long conversaId) {
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conversa não encontrada: " + conversaId));

        verificarParticipante(conversa);
        return mensagemRepository.findByConversa_IdOrderByEnviadaEmAsc(conversaId);
    }
}