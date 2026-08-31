package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoPerfil;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        LoginDTO dto = new LoginDTO();
        dto.setUsuario(usuario);
        dto.setSenha(senha);
        dto.setTelefone(telefone);
        dto.setTipoPerfil(TipoPerfil.PESSOA_FISICA);
        return dto;
    }

    // ---------- listarTodos() ----------
    @Test
    void listarTodos_deveUsarFindAllQuandoSemFiltros() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Login> pagina = new PageImpl<>(List.of(login(1L, "joao123")));
        when(loginRepository.findAll(pageable)).thenReturn(pagina);

        assertThat(loginService.listarTodos(null, null, pageable)).isEqualTo(pagina);
    }

    @Test
    void listarTodos_deveFiltrarApenasPorRole() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Login> pagina = new PageImpl<>(List.of(login(1L, "admin")));
        when(loginRepository.findByRoleIgnoreCase("ADMIN", pageable)).thenReturn(pagina);

        assertThat(loginService.listarTodos("ADMIN", null, pageable)).isEqualTo(pagina);
    }

    @Test
    void listarTodos_deveFiltrarApenasPorTipoPerfil() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Login> pagina = new PageImpl<>(List.of());
        when(loginRepository.findByTipoPerfil(TipoPerfil.LOJA, pageable)).thenReturn(pagina);

        assertThat(loginService.listarTodos(null, TipoPerfil.LOJA, pageable)).isEqualTo(pagina);
    }

    @Test
    void listarTodos_deveCombinarRoleETipoPerfil() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Login> pagina = new PageImpl<>(List.of());
        when(loginRepository.findByRoleIgnoreCaseAndTipoPerfil("USER", TipoPerfil.PESSOA_FISICA, pageable))
                .thenReturn(pagina);

        assertThat(loginService.listarTodos("USER", TipoPerfil.PESSOA_FISICA, pageable)).isEqualTo(pagina);
    }

    // ---------- buscarPorId() ----------
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

    // ---------- criar() com perfil de vendedor ----------
    @Test
    void criar_deveLancarExcecaoQuandoLojaSemRazaoSocial() {
        LoginDTO dto = loginDto("joao123", "senha123", "19999887766");
        dto.setTipoPerfil(TipoPerfil.LOJA);
        dto.setCnpj("12345678000190");

        assertThatThrownBy(() -> loginService.criar(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Razão social");

        verifyNoInteractions(loginRepository);
    }

    @Test
    void criar_deveLancarExcecaoQuandoLojaSemCnpj() {
        LoginDTO dto = loginDto("joao123", "senha123", "19999887766");
        dto.setTipoPerfil(TipoPerfil.LOJA);
        dto.setRazaoSocial("Joao Veiculos LTDA");

        assertThatThrownBy(() -> loginService.criar(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("CNPJ");

        verifyNoInteractions(loginRepository);
    }

    @Test
    void criar_devePermitirPessoaFisicaSemRazaoSocialECnpj() {
        LoginDTO dto = loginDto("joao123", "senha123", "19999887766");
        dto.setTipoPerfil(TipoPerfil.PESSOA_FISICA);

        when(passwordEncoder.encode("senha123")).thenReturn("senhaCodificada");
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));

        Login salvo = loginService.criar(dto);

        assertThat(salvo.getTipoPerfil()).isEqualTo(TipoPerfil.PESSOA_FISICA);
        assertThat(salvo.getRazaoSocial()).isNull();
        assertThat(salvo.getCnpj()).isNull();
    }

    @Test
    void criar_devePermitirLojaComRazaoSocialECnpj() {
        LoginDTO dto = loginDto("joao123", "senha123", "19999887766");
        dto.setTipoPerfil(TipoPerfil.LOJA);
        dto.setRazaoSocial("Joao Veiculos LTDA");
        dto.setCnpj("12345678000190");

        when(passwordEncoder.encode("senha123")).thenReturn("senhaCodificada");
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));

        Login salvo = loginService.criar(dto);

        assertThat(salvo.getTipoPerfil()).isEqualTo(TipoPerfil.LOJA);
        assertThat(salvo.getRazaoSocial()).isEqualTo("Joao Veiculos LTDA");
        assertThat(salvo.getCnpj()).isEqualTo("12345678000190");
    }

    // ---------- atualizar() com perfil de vendedor ----------
    @Test
    void atualizar_deveLimparRazaoSocialECnpjAoTrocarParaPessoaFisica() {
        Login existente = login(1L, "joao123");
        existente.setTipoPerfil(TipoPerfil.LOJA);
        existente.setRazaoSocial("Joao Veiculos LTDA");
        existente.setCnpj("12345678000190");

        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(loginRepository.save(any(Login.class))).thenAnswer(inv -> inv.getArgument(0));
        autenticarComo("joao123", "USER");

        LoginDTO dto = loginDto("joao123", "novaSenha", "19988887777");
        dto.setTipoPerfil(TipoPerfil.PESSOA_FISICA);

        Login atualizado = loginService.atualizar(1L, dto);

        assertThat(atualizado.getTipoPerfil()).isEqualTo(TipoPerfil.PESSOA_FISICA);
        assertThat(atualizado.getRazaoSocial()).isNull();
        assertThat(atualizado.getCnpj()).isNull();
    }

    @Test
    void atualizar_deveLancarExcecaoAoTrocarParaLojaSemCnpj() {
        Login existente = login(1L, "joao123");
        existente.setTipoPerfil(TipoPerfil.PESSOA_FISICA);

        when(loginRepository.findById(1L)).thenReturn(Optional.of(existente));
        autenticarComo("joao123", "USER");

        LoginDTO dto = loginDto("joao123", "novaSenha", "19988887777");
        dto.setTipoPerfil(TipoPerfil.LOJA);
        dto.setRazaoSocial("Joao Veiculos LTDA");

        assertThatThrownBy(() -> loginService.atualizar(1L, dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("CNPJ");

        verify(loginRepository, never()).save(any());
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