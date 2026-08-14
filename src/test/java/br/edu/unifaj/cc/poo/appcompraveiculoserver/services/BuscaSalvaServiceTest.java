package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.buscasalva.NovaBuscaSalvaDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Alerta;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.BuscaSalva;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.AlertaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.BuscaSalvaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class BuscaSalvaServiceTest {

    @Mock
    private BuscaSalvaRepository buscaSalvaRepository;
    @Mock
    private AlertaRepository alertaRepository;
    @Mock
    private LoginRepository loginRepository;

    @InjectMocks
    private BuscaSalvaService buscaSalvaService;

    private Login dono;

    @BeforeEach
    void setUp() {
        dono = new Login();
        dono.setId(1L);
        dono.setUsuario("maria456");

        autenticarComo("maria456");
        lenient().when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(dono));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String usuario) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = mock(SecurityContext.class);
        lenient().when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    private NovaBuscaSalvaDTO dtoValido() {
        NovaBuscaSalvaDTO dto = new NovaBuscaSalvaDTO();
        dto.setMarca("Volkswagen");
        dto.setPrecoMax(50000f);
        dto.setCidade("Pedreira");
        dto.setEstado("SP");
        return dto;
    }

    // ---------- criar() ----------
    @Test
    void criar_deveSalvarBuscaVinculadaAoUsuarioLogado() {
        when(buscaSalvaRepository.save(any(BuscaSalva.class))).thenAnswer(inv -> inv.getArgument(0));

        BuscaSalva salva = buscaSalvaService.criar(dtoValido());

        assertThat(salva.getLogin()).isEqualTo(dono);
        assertThat(salva.getMarca()).isEqualTo("Volkswagen");
        assertThat(salva.getPrecoMax()).isEqualTo(50000f);
        verify(buscaSalvaRepository).save(any(BuscaSalva.class));
    }

    // ---------- deletar() ----------
    @Test
    void deletar_deveLancarAccessDeniedQuandoNaoEDono() {
        Login outroUsuario = new Login();
        outroUsuario.setId(2L);

        BuscaSalva busca = new BuscaSalva();
        busca.setId(10L);
        busca.setLogin(outroUsuario);

        when(buscaSalvaRepository.findById(10L)).thenReturn(Optional.of(busca));

        assertThatThrownBy(() -> buscaSalvaService.deletar(10L))
                .isInstanceOf(AccessDeniedException.class);

        verify(buscaSalvaRepository, never()).deleteById(any());
    }

    @Test
    void deletar_devePermitirQuandoEDono() {
        BuscaSalva busca = new BuscaSalva();
        busca.setId(10L);
        busca.setLogin(dono);

        when(buscaSalvaRepository.findById(10L)).thenReturn(Optional.of(busca));

        buscaSalvaService.deletar(10L);

        verify(buscaSalvaRepository).deleteById(10L);
    }

    @Test
    void deletar_deveLancarExcecaoQuandoBuscaNaoExiste() {
        when(buscaSalvaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buscaSalvaService.deletar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- marcarVisualizado() ----------
    @Test
    void marcarVisualizado_deveLancarAccessDeniedQuandoAlertaNaoEDoUsuario() {
        Login outroUsuario = new Login();
        outroUsuario.setId(2L);

        BuscaSalva buscaDeOutro = new BuscaSalva();
        buscaDeOutro.setLogin(outroUsuario);

        Alerta alerta = new Alerta();
        alerta.setId(5L);
        alerta.setBuscaSalva(buscaDeOutro);

        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));

        assertThatThrownBy(() -> buscaSalvaService.marcarVisualizado(5L))
                .isInstanceOf(AccessDeniedException.class);

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void marcarVisualizado_deveAtualizarQuandoAlertaEDoUsuario() {
        BuscaSalva busca = new BuscaSalva();
        busca.setLogin(dono);

        Alerta alerta = new Alerta();
        alerta.setId(5L);
        alerta.setBuscaSalva(busca);
        alerta.setVeiculo(new Veiculo());

        when(alertaRepository.findById(5L)).thenReturn(Optional.of(alerta));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        Alerta resultado = buscaSalvaService.marcarVisualizado(5L);

        assertThat(resultado.isVisualizado()).isTrue();
        verify(alertaRepository).save(alerta);
    }

    // ---------- listarMinhas() / listarAlertas() ----------
    @Test
    void listarMinhas_deveRetornarBuscasDoUsuarioLogado() {
        BuscaSalva busca = new BuscaSalva();
        busca.setLogin(dono);
        when(buscaSalvaRepository.findByLogin_IdOrderByCriadaEmDesc(1L)).thenReturn(List.of(busca));

        assertThat(buscaSalvaService.listarMinhas()).containsExactly(busca);
    }

    @Test
    void listarAlertas_deveRetornarAlertasDoUsuarioLogado() {
        Alerta alerta = new Alerta();
        when(alertaRepository.findByBuscaSalva_Login_IdOrderByCriadoEmDesc(1L)).thenReturn(List.of(alerta));

        assertThat(buscaSalvaService.listarAlertas()).containsExactly(alerta);
    }
}