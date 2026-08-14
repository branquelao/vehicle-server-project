package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.NovaAvaliacaoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.avaliacao.ResumoAvaliacaoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Avaliacao;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.AvaliacaoRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.ConversaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;
    @Mock
    private LoginRepository loginRepository;
    @Mock
    private ConversaRepository conversaRepository;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

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

    private NovaAvaliacaoDTO avaliacaoDto(int nota, String comentario) {
        NovaAvaliacaoDTO dto = new NovaAvaliacaoDTO();
        dto.setNota(nota);
        dto.setComentario(comentario);
        return dto;
    }

    // ---------- avaliar() ----------
    @Test
    void avaliar_deveLancarExcecaoQuandoAvaliaSiMesmo() {
        Login avaliador = login(1L, "joao123");

        autenticarComo("joao123");
        when(loginRepository.findByUsuario("joao123")).thenReturn(Optional.of(avaliador));
        when(loginRepository.findById(1L)).thenReturn(Optional.of(avaliador));

        assertThatThrownBy(() -> avaliacaoService.avaliar(1L, avaliacaoDto(5, "Ótimo")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("a si mesmo");

        verifyNoInteractions(conversaRepository, avaliacaoRepository);
    }

    @Test
    void avaliar_deveLancarExcecaoQuandoNuncaConversouComVendedor() {
        Login avaliador = login(1L, "maria456");
        Login vendedor = login(2L, "joao123");

        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(avaliador));
        when(loginRepository.findById(2L)).thenReturn(Optional.of(vendedor));
        when(conversaRepository.existsByComprador_IdAndVendedor_Id(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> avaliacaoService.avaliar(2L, avaliacaoDto(5, "Ótimo")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("conversou");

        verifyNoInteractions(avaliacaoRepository);
    }

    @Test
    void avaliar_deveCriarNovaAvaliacaoQuandoValidoESemAvaliacaoPrevia() {
        Login avaliador = login(1L, "maria456");
        Login vendedor = login(2L, "joao123");

        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(avaliador));
        when(loginRepository.findById(2L)).thenReturn(Optional.of(vendedor));
        when(conversaRepository.existsByComprador_IdAndVendedor_Id(1L, 2L)).thenReturn(true);
        when(avaliacaoRepository.findByAvaliador_IdAndAvaliado_Id(1L, 2L)).thenReturn(Optional.empty());
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Avaliacao resultado = avaliacaoService.avaliar(2L, avaliacaoDto(5, "Vendedor muito atencioso"));

        assertThat(resultado.getNota()).isEqualTo(5);
        assertThat(resultado.getComentario()).isEqualTo("Vendedor muito atencioso");
        assertThat(resultado.getAvaliador()).isEqualTo(avaliador);
        assertThat(resultado.getAvaliado()).isEqualTo(vendedor);
    }

    @Test
    void avaliar_deveAtualizarAvaliacaoExistenteEmVezDeCriarNova() {
        Login avaliador = login(1L, "maria456");
        Login vendedor = login(2L, "joao123");
        Avaliacao existente = new Avaliacao(avaliador, vendedor, 3, "Ok no começo");

        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(avaliador));
        when(loginRepository.findById(2L)).thenReturn(Optional.of(vendedor));
        when(conversaRepository.existsByComprador_IdAndVendedor_Id(1L, 2L)).thenReturn(true);
        when(avaliacaoRepository.findByAvaliador_IdAndAvaliado_Id(1L, 2L)).thenReturn(Optional.of(existente));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> inv.getArgument(0));

        Avaliacao resultado = avaliacaoService.avaliar(2L, avaliacaoDto(5, "Melhorou bastante"));

        assertThat(resultado).isSameAs(existente);
        assertThat(resultado.getNota()).isEqualTo(5);
        assertThat(resultado.getComentario()).isEqualTo("Melhorou bastante");
        verify(avaliacaoRepository, times(1)).save(existente);
    }

    @Test
    void avaliar_deveLancarExcecaoQuandoVendedorNaoExiste() {
        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(login(1L, "maria456")));
        when(loginRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.avaliar(99L, avaliacaoDto(5, "Ótimo")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- resumo() ----------
    @Test
    void resumo_deveRetornarZeroQuandoVendedorSemAvaliacoes() {
        when(avaliacaoRepository.calcularMediaPorAvaliado(2L)).thenReturn(null);
        when(avaliacaoRepository.countByAvaliado_Id(2L)).thenReturn(0L);

        ResumoAvaliacaoDTO resumo = avaliacaoService.resumo(2L);

        assertThat(resumo.getMedia()).isEqualTo(0.0);
        assertThat(resumo.getTotal()).isEqualTo(0L);
    }

    @Test
    void resumo_deveRetornarMediaETotalCalculados() {
        when(avaliacaoRepository.calcularMediaPorAvaliado(2L)).thenReturn(4.67);
        when(avaliacaoRepository.countByAvaliado_Id(2L)).thenReturn(3L);

        ResumoAvaliacaoDTO resumo = avaliacaoService.resumo(2L);

        assertThat(resumo.getMedia()).isEqualTo(4.67);
        assertThat(resumo.getTotal()).isEqualTo(3L);
    }
}