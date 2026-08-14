package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários do LoginService.
 * Foco: codificação de senha, permissão de dono/admin, atualização condicional de
 * senha e imagem de perfil — isolados de banco/Spring context (JUnit 5 + Mockito).
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private LoginRepository loginRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @TempDir
    Path uploadDir;

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

    private Login login(Long id, String usuario) {
        Login login = new Login();
        login.setId(id);
        login.setUsuario(usuario);
        login.setSenha("hashAntigo");
        login.setTelefone("19999887766");
        return login;
    }

    private LoginDTO loginDto(String usuario, String senha, String telefone) {
        return new LoginDTO(usuario, senha, telefone);
    }

    // ---------- listarTodos() / buscarPorId() ----------
    @Test
    void listarTodos_deveRetornarTodosOsLogins() {
        List<Login> logins = List.of(login(1L, "joao123"), login(2L, "maria456"));
        when(loginRepository.findAll()).thenReturn(logins);

        assertThat(loginService.listarTodos()).containsExactlyElementsOf(logins);
    }

    @Test
    void buscarPorId_deveRetornarVazioQuandoNaoExiste() {
        when(loginRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(loginService.buscarPorId(99L)).isEmpty();
    }

    // ---------- criar() ----------
    @Test
    void criar_deveCodificarSenhaAntesDeSalvar() {
        LoginDTO dto = loginDto("joao123", "senha123", "19999887766");
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCodificada");
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));

        Login salvo = loginService.criar(dto);

        assertThat(salvo.getUsuario()).isEqualTo("joao123");
        assertThat(salvo.getSenha()).isEqualTo("senhaCodificada");
        assertThat(salvo.getTelefone()).isEqualTo("19999887766");
        verify(passwordEncoder).encode("senha123");
    }

    // ---------- atualizar() ----------
    @Test
    void atualizar_deveLancarExcecaoQuandoLoginNaoExiste() {
        when(loginRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.atualizar(99L, loginDto("x", "senha123", "19999887766")))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void atualizar_deveLancarAccessDeniedQuandoUsuarioNaoEDonoNemAdmin() {
        Login existente = login(1L, "joao123");
        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        autenticarComo("outroUsuario", "USER");

        assertThatThrownBy(() -> loginService.atualizar(1L, loginDto("joao123", "novaSenha", "19999887766")))
                .isInstanceOf(AccessDeniedException.class);

        verify(loginRepository, never()).save(any());
    }

    @Test
    void atualizar_devePermitirQuandoUsuarioEDono() {
        Login existente = login(1L, "joao123");
        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("novaSenha")).thenReturn("novaSenhaCodificada");
        autenticarComo("joao123", "USER");

        Login atualizado = loginService.atualizar(1L, loginDto("joao123", "novaSenha", "19988887777"));

        assertThat(atualizado.getTelefone()).isEqualTo("19988887777");
        assertThat(atualizado.getSenha()).isEqualTo("novaSenhaCodificada");
    }

    @Test
    void atualizar_devePermitirQuandoUsuarioEAdmin() {
        Login existente = login(1L, "joao123");
        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));
        autenticarComo("admin", "ADMIN");

        Login atualizado = loginService.atualizar(1L, loginDto("joao123", null, "19988887777"));

        assertThat(atualizado.getTelefone()).isEqualTo("19988887777");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void atualizar_deveManterSenhaAtualQuandoSenhaNaoInformada() {
        Login existente = login(1L, "joao123");
        existente.setSenha("hashAntigo");
        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));
        autenticarComo("joao123", "USER");

        Login atualizado = loginService.atualizar(1L, loginDto("joao123", "   ", "19988887777"));

        assertThat(atualizado.getSenha()).isEqualTo("hashAntigo");
        verifyNoInteractions(passwordEncoder);
    }

    // ---------- atualizarImagem() ----------
    @Test
    void atualizarImagem_deveLancarAccessDeniedQuandoUsuarioNaoEDonoNemAdmin() throws Exception {
        Login existente = login(1L, "joao123");
        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        autenticarComo("outroUsuario", "USER");

        MockMultipartFile arquivo = new MockMultipartFile("imagem", "foto.jpg", "image/jpeg", "conteudo".getBytes());

        assertThatThrownBy(() -> loginService.atualizarImagem(1L, arquivo, uploadDir))
                .isInstanceOf(AccessDeniedException.class);

        verify(loginRepository, never()).save(any());
    }

    @Test
    void atualizarImagem_deveSalvarNovaImagemQuandoUsuarioEDono() throws Exception {
        Login existente = login(1L, "joao123");
        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));
        autenticarComo("joao123", "USER");

        MockMultipartFile arquivo = new MockMultipartFile("imagem", "foto.jpg", "image/jpeg", "conteudo".getBytes());

        Login atualizado = loginService.atualizarImagem(1L, arquivo, uploadDir);

        assertThat(atualizado.getLoginImagem()).isNotBlank();
        assertThat(atualizado.getLoginImagem()).endsWith(".jpg");
        assertThat(Files.exists(uploadDir.resolve(atualizado.getLoginImagem()))).isTrue();
    }

    @Test
    void atualizarImagem_deveApagarImagemAntigaAntesDeSalvarNova() throws Exception {
        Login existente = login(1L, "joao123");
        existente.setLoginImagem("antiga.jpg");
        Files.createFile(uploadDir.resolve("antiga.jpg"));

        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));
        autenticarComo("joao123", "USER");

        MockMultipartFile arquivo = new MockMultipartFile("imagem", "nova.png", "image/png", "conteudo".getBytes());

        loginService.atualizarImagem(1L, arquivo, uploadDir);

        assertThat(Files.exists(uploadDir.resolve("antiga.jpg"))).isFalse();
    }

    // ---------- deletar() ----------
    @Test
    void deletar_deveLancarAccessDeniedQuandoUsuarioNaoEDono() {
        Login existente = login(1L, "joao123");
        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        autenticarComo("outroUsuario", "USER");

        assertThatThrownBy(() -> loginService.deletar(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(loginRepository, never()).deleteById(any());
    }

    @Test
    void deletar_deveExcluirQuandoUsuarioEDono() {
        Login existente = login(1L, "joao123");
        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        autenticarComo("joao123", "USER");

        loginService.deletar(1L);

        verify(loginRepository).deleteById(1L);
    }

    @Test
    void deletar_deveLancarExcecaoQuandoLoginNaoExiste() {
        when(loginRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.deletar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}