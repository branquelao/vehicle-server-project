package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Opcional;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.*;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.ImagemInvalidaException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.OpcionalRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.VeiculoRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do VeiculoService.
 * Foco: regras de negócio (validação por tipo de veículo, imagens obrigatórias,
 * permissão de dono/admin, resolução de opcionais) isoladas de banco/Spring context.
 */
@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private LoginRepository loginRepository;
    @Mock
    private OpcionalRepository opcionalRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    @TempDir
    Path uploadDir;

    private Login dono;

    @BeforeEach
    void setUp() throws Exception {
        dono = new Login();
        dono.setId(1L);
        dono.setUsuario("joao123");
        dono.setRole("USER");

        // Arquivo "existente" no diretório de upload, pra satisfazer a validação de imagem
        Files.createFile(uploadDir.resolve("foto1.jpg"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String usuario, String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    private VeiculoDTO veiculoDtoValido() {
        VeiculoDTO dto = new VeiculoDTO();
        dto.setTipo(TipoVeiculo.CARRO);
        dto.setMarca("Volkswagen");
        dto.setModelo("Fusca");
        dto.setAnoFabricacao(1972);
        dto.setAnoModelo(1972);
        dto.setKm(85000);
        dto.setCor("Azul");
        dto.setCombustivel(Combustivel.GASOLINA);
        dto.setCambio(Cambio.MANUAL);
        dto.setEstadoConservacao(EstadoConservacao.USADO);
        dto.setValor(15000f);
        dto.setCarroceria(Carroceria.HATCH);
        dto.setPortas(2);
        dto.setLoginId(1L);
        dto.setImagens(List.of("foto1.jpg"));
        dto.setCidade("Pedreira");
        dto.setEstado("SP");
        return dto;
    }

    // ---------- criar() ----------
    @Test
    void criar_deveLancarExcecaoQuandoCarroSemCarroceria() {
        VeiculoDTO dto = veiculoDtoValido();
        dto.setCarroceria(null);

        assertThatThrownBy(() -> veiculoService.criar(dto, uploadDir))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Carroceria");

        verifyNoInteractions(veiculoRepository);
    }

    @Test
    void criar_deveLancarExcecaoQuandoMotoSemCilindrada() {
        VeiculoDTO dto = veiculoDtoValido();
        dto.setTipo(TipoVeiculo.MOTO);
        dto.setCategoriaMoto(CategoriaMoto.NAKED);
        dto.setCilindradaMoto(null);

        assertThatThrownBy(() -> veiculoService.criar(dto, uploadDir))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Cilindrada");
    }

    @Test
    void criar_deveLancarExcecaoQuandoNenhumaImagemInformada() {
        VeiculoDTO dto = veiculoDtoValido();
        dto.setImagens(List.of());

        assertThatThrownBy(() -> veiculoService.criar(dto, uploadDir))
                .isInstanceOf(ImagemInvalidaException.class);
    }

    @Test
    void criar_deveLancarExcecaoQuandoImagemNaoExisteNoDisco() {
        VeiculoDTO dto = veiculoDtoValido();
        dto.setImagens(List.of("nao-existe.jpg"));

        assertThatThrownBy(() -> veiculoService.criar(dto, uploadDir))
                .isInstanceOf(ImagemInvalidaException.class)
                .hasMessageContaining("nao-existe.jpg");
    }

    @Test
    void criar_deveLancarExcecaoQuandoLoginNaoExiste() {
        VeiculoDTO dto = veiculoDtoValido();
        when(loginRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.criar(dto, uploadDir))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void criar_deveSalvarVeiculoComImagemPrincipalEOpcionaisQuandoDadosValidos() {
        VeiculoDTO dto = veiculoDtoValido();
        dto.setOpcionalIds(Set.of(10L, 20L));

        when(loginRepository.findById(1L)).thenReturn(Optional.of(dono));
        when(opcionalRepository.findAllById(dto.getOpcionalIds())).thenReturn(List.of(
                new Opcional(10L, "Ar condicionado"),
                new Opcional(20L, "Freio ABS")
        ));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        Veiculo salvo = veiculoService.criar(dto, uploadDir);

        assertThat(salvo.getMarca()).isEqualTo("Volkswagen");
        assertThat(salvo.getImagens()).hasSize(1);
        assertThat(salvo.getImagens().get(0).isPrincipal()).isTrue();
        assertThat(salvo.getOpcionais()).hasSize(2);
        assertThat(salvo.getLogin()).isEqualTo(dono);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    void criar_deveLancarExcecaoQuandoOpcionalInformadoNaoExiste() {
        VeiculoDTO dto = veiculoDtoValido();
        dto.setOpcionalIds(Set.of(10L, 20L));

        when(loginRepository.findById(1L)).thenReturn(Optional.of(dono));
        when(opcionalRepository.findAllById(dto.getOpcionalIds()))
                .thenReturn(List.of(new Opcional(10L, "Ar condicionado"))); // só achou 1 de 2

        assertThatThrownBy(() -> veiculoService.criar(dto, uploadDir))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- atualizar() ----------
    @Test
    void atualizar_deveLancarAccessDeniedQuandoUsuarioNaoEDonoNemAdmin() {
        Veiculo existente = new Veiculo();
        existente.setId(1L);
        existente.setLogin(dono);

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(existente));
        autenticarComo("outroUsuario", "USER");

        VeiculoDTO dto = veiculoDtoValido();

        assertThatThrownBy(() -> veiculoService.atualizar(1L, dto, uploadDir))
                .isInstanceOf(AccessDeniedException.class);

        verify(veiculoRepository, never()).save(any());
    }

    @Test
    void atualizar_devePermitirQuandoUsuarioEAdmin() {
        Veiculo existente = new Veiculo();
        existente.setId(1L);
        existente.setLogin(dono);
        existente.setImagens(new ArrayList<>());

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));
        autenticarComo("admin", "ADMIN");

        VeiculoDTO dto = veiculoDtoValido();
        dto.setValor(20000f);
        dto.setImagens(null); // não altera imagens nesta atualização

        Veiculo atualizado = veiculoService.atualizar(1L, dto, uploadDir);

        assertThat(atualizado.getValor()).isEqualTo(20000f);
        verify(veiculoRepository).save(existente);
    }

    @Test
    void atualizar_devePermitirQuandoUsuarioEDono() {
        Veiculo existente = new Veiculo();
        existente.setId(1L);
        existente.setLogin(dono);
        existente.setImagens(new ArrayList<>());

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));
        autenticarComo("joao123", "USER");

        VeiculoDTO dto = veiculoDtoValido();
        dto.setImagens(null);

        assertThat(veiculoService.atualizar(1L, dto, uploadDir)).isNotNull();
    }

    @Test
    void atualizar_deveLancarExcecaoQuandoVeiculoNaoExiste() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        VeiculoDTO dto = veiculoDtoValido();

        assertThatThrownBy(() -> veiculoService.atualizar(99L, dto, uploadDir))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- deletar() ----------
    @Test
    void deletar_deveLancarAccessDeniedQuandoUsuarioNaoEDono() {
        Veiculo existente = new Veiculo();
        existente.setId(1L);
        existente.setLogin(dono);

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(existente));
        autenticarComo("outroUsuario", "USER");

        assertThatThrownBy(() -> veiculoService.deletar(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(veiculoRepository, never()).deleteById(any());
    }

    @Test
    void deletar_deveExcluirQuandoUsuarioEDono() {
        Veiculo existente = new Veiculo();
        existente.setId(1L);
        existente.setLogin(dono);

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(existente));
        autenticarComo("joao123", "USER");

        veiculoService.deletar(1L);

        verify(veiculoRepository).deleteById(1L);
    }

    @Test
    void deletar_deveLancarExcecaoQuandoVeiculoNaoExiste() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.deletar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verifyNoMoreInteractions(veiculoRepository);
    }
}