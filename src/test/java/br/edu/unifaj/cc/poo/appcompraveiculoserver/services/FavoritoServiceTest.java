package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Favorito;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.FavoritoRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.VeiculoRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;
    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private LoginRepository loginRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    private Login comprador;

    @BeforeEach
    void setUp() {
        comprador = new Login();
        comprador.setId(1L);
        comprador.setUsuario("maria456");

        autenticarComo("maria456");
        when(loginRepository.findByUsuario("maria456")).thenReturn(Optional.of(comprador));
    }

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

    private Veiculo veiculoDoVendedor(Long loginId) {
        Login vendedor = new Login();
        vendedor.setId(loginId);
        Veiculo veiculo = new Veiculo();
        veiculo.setId(10L);
        veiculo.setLogin(vendedor);
        return veiculo;
    }

    @Test
    void favoritar_deveLancarExcecaoQuandoTentaFavoritarProprioAnuncio() {
        Veiculo proprioAnuncio = veiculoDoVendedor(comprador.getId());
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(proprioAnuncio));

        assertThatThrownBy(() -> favoritoService.favoritar(10L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("próprio anúncio");

        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void favoritar_deveSerIdempotenteQuandoJaFavoritado() {
        Veiculo veiculo = veiculoDoVendedor(2L);
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(favoritoRepository.existsByLogin_IdAndVeiculo_Id(1L, 10L)).thenReturn(true);

        favoritoService.favoritar(10L);

        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void favoritar_deveSalvarQuandoValidoENaoFavoritadoAinda() {
        Veiculo veiculo = veiculoDoVendedor(2L);
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(favoritoRepository.existsByLogin_IdAndVeiculo_Id(1L, 10L)).thenReturn(false);

        favoritoService.favoritar(10L);

        verify(favoritoRepository).save(argThat(f ->
                f.getLogin().getId().equals(1L) && f.getVeiculo().getId().equals(10L)));
    }

    @Test
    void favoritar_deveLancarExcecaoQuandoVeiculoNaoExiste() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoritoService.favoritar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void desfavoritar_deveDelegarParaRepositorioComLoginEVeiculoCorretos() {
        favoritoService.desfavoritar(10L);

        verify(favoritoRepository).deleteByLogin_IdAndVeiculo_Id(1L, 10L);
    }

    @Test
    void listarFavoritos_deveRetornarListaDoUsuarioLogado() {
        Favorito favorito = new Favorito(comprador, veiculoDoVendedor(2L));
        when(favoritoRepository.findByLogin_IdOrderByCriadoEmDesc(1L)).thenReturn(List.of(favorito));

        List<Favorito> resultado = favoritoService.listarFavoritos();

        assertThat(resultado).containsExactly(favorito);
    }
}