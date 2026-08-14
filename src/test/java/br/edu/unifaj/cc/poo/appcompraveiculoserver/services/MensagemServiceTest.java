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
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensagemServiceTest {

    @Mock
    private ConversaRepository conversaRepository;
    @Mock
    private MensagemRepository mensagemRepository;
    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private LoginRepository loginRepository;

    @InjectMocks
    private MensagemService mensagemService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String usuario) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    private Login login(Long id, String usuario) {
        Login login = new Login();
        login.setId(id);
        login.setUsuario(usuario);
        return login;
    }

    private NovaMensagemDTO mensagemDto(String conteudo) {
        NovaMensagemDTO dto = new NovaMensagemDTO();
        dto.setConteudo(conteudo);
        return dto;
    }

    // ---------- iniciarOuContinuar() ----------
    @Test
    void iniciarOuContinuar_deveLancarExcecaoQuandoCompradorEDono() {
        Login dono = login(1L, "joao123");
        Veiculo veiculo = new Veiculo();
        veiculo.setId(10L);
        veiculo.setLogin(dono);

        autenticarComo("joao123");
        when(loginRepository.findByUsuario("joao123")).thenReturn(Optional.of(dono));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

        assertThatThrownBy(() -> mensagemService.iniciarOuContinuar(10L, mensagemDto("Oi")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("próprio anúncio");

        verifyNoInteractions(conversaRepository);
    }

    @Test
    void iniciarOuContinuar_deveCriarNovaConversaQuandoNaoExisteAinda() {
        Login vendedor = login(1L, "joao123");
        Login comprador = login(2L, "maria456");
        Veiculo veiculo = new Veiculo();
        veiculo.setId(10L);
        veiculo.setLogin(vendedor);

        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(comprador));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(conversaRepository.findByVeiculo_IdAndComprador_Id(10L, 2L)).thenReturn(Optional.empty());
        when(conversaRepository.save(any(Conversa.class))).thenAnswer(inv -> inv.getArgument(0));

        Conversa resultado = mensagemService.iniciarOuContinuar(10L, mensagemDto("Ainda está disponível?"));

        assertThat(resultado.getComprador()).isEqualTo(comprador);
        assertThat(resultado.getVendedor()).isEqualTo(vendedor);
        assertThat(resultado.getVeiculo()).isEqualTo(veiculo);
        verify(mensagemRepository).save(argThat(m ->
                m.getRemetente().equals(comprador) && m.getConteudo().equals("Ainda está disponível?")));
        // uma vez ao criar a conversa, outra ao atualizar atualizadaEm em adicionarMensagem
        verify(conversaRepository, times(2)).save(any(Conversa.class));
    }

    @Test
    void iniciarOuContinuar_deveReaproveitarConversaExistente() {
        Login vendedor = login(1L, "joao123");
        Login comprador = login(2L, "maria456");
        Veiculo veiculo = new Veiculo();
        veiculo.setId(10L);
        veiculo.setLogin(vendedor);

        Conversa existente = new Conversa();
        existente.setId(50L);
        existente.setVeiculo(veiculo);
        existente.setComprador(comprador);
        existente.setVendedor(vendedor);

        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(comprador));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(conversaRepository.findByVeiculo_IdAndComprador_Id(10L, 2L)).thenReturn(Optional.of(existente));
        when(conversaRepository.save(any(Conversa.class))).thenAnswer(inv -> inv.getArgument(0));

        Conversa resultado = mensagemService.iniciarOuContinuar(10L, mensagemDto("Ainda topa negociar?"));

        assertThat(resultado.getId()).isEqualTo(50L);
        // não deve criar conversa nova: só o save de atualização em adicionarMensagem
        verify(conversaRepository, times(1)).save(any(Conversa.class));
    }

    @Test
    void iniciarOuContinuar_deveLancarExcecaoQuandoVeiculoNaoExiste() {
        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(login(2L, "maria456")));
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mensagemService.iniciarOuContinuar(99L, mensagemDto("Oi")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- responder() ----------
    @Test
    void responder_deveLancarAccessDeniedQuandoUsuarioNaoEParticipante() {
        Login vendedor = login(1L, "joao123");
        Login comprador = login(2L, "maria456");
        Login estranho = login(3L, "carlos789");

        Conversa conversa = new Conversa();
        conversa.setId(50L);
        conversa.setComprador(comprador);
        conversa.setVendedor(vendedor);

        autenticarComo("carlos789");
        when(loginRepository.findByUsuario("carlos789")).thenReturn(Optional.of(estranho));
        when(conversaRepository.findById(50L)).thenReturn(Optional.of(conversa));

        assertThatThrownBy(() -> mensagemService.responder(50L, mensagemDto("Posso entrar nessa conversa?")))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(mensagemRepository);
    }

    @Test
    void responder_devePermitirQuandoUsuarioEParticipante() {
        Login vendedor = login(1L, "joao123");
        Login comprador = login(2L, "maria456");

        Conversa conversa = new Conversa();
        conversa.setId(50L);
        conversa.setComprador(comprador);
        conversa.setVendedor(vendedor);

        autenticarComo("joao123");
        when(loginRepository.findByUsuario("joao123")).thenReturn(Optional.of(vendedor));
        when(conversaRepository.findById(50L)).thenReturn(Optional.of(conversa));
        when(conversaRepository.save(any(Conversa.class))).thenAnswer(inv -> inv.getArgument(0));

        Conversa resultado = mensagemService.responder(50L, mensagemDto("Sim, ainda está à venda."));

        assertThat(resultado).isEqualTo(conversa);
        verify(mensagemRepository).save(any(Mensagem.class));
    }

    @Test
    void responder_deveLancarExcecaoQuandoConversaNaoExiste() {
        when(conversaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mensagemService.responder(99L, mensagemDto("Oi")))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verifyNoInteractions(loginRepository);
    }

    // ---------- listarMensagens() ----------
    @Test
    void listarMensagens_deveLancarAccessDeniedQuandoNaoEParticipante() {
        Login vendedor = login(1L, "joao123");
        Login comprador = login(2L, "maria456");
        Login estranho = login(3L, "carlos789");

        Conversa conversa = new Conversa();
        conversa.setId(50L);
        conversa.setComprador(comprador);
        conversa.setVendedor(vendedor);

        autenticarComo("carlos789");
        when(loginRepository.findByUsuario("carlos789")).thenReturn(Optional.of(estranho));
        when(conversaRepository.findById(50L)).thenReturn(Optional.of(conversa));

        assertThatThrownBy(() -> mensagemService.listarMensagens(50L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void listarMensagens_deveRetornarQuandoUsuarioEParticipante() {
        Login vendedor = login(1L, "joao123");
        Login comprador = login(2L, "maria456");

        Conversa conversa = new Conversa();
        conversa.setId(50L);
        conversa.setComprador(comprador);
        conversa.setVendedor(vendedor);

        Mensagem mensagem = new Mensagem(conversa, comprador, "Oi, tudo bem?");

        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(comprador));
        when(conversaRepository.findById(50L)).thenReturn(Optional.of(conversa));
        when(mensagemRepository.findByConversa_IdOrderByEnviadaEmAsc(50L)).thenReturn(List.of(mensagem));

        List<Mensagem> resultado = mensagemService.listarMensagens(50L);

        assertThat(resultado).containsExactly(mensagem);
    }
}