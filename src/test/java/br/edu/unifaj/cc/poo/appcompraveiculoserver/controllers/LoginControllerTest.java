package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.AlterarSenhaDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginAtualizarDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoPerfil;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.security.CustomUserDetailsService;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.security.JwtService;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Login loginSalvo(Long id) {
        Login login = new Login();
        login.setId(id);
        login.setUsuario("joao123");
        login.setSenha("hash");
        login.setTelefone("19999887766");
        login.setRole("USER");
        login.setTipoPerfil(TipoPerfil.PESSOA_FISICA);
        return login;
    }

    private LoginDTO loginDtoValido() {
        LoginDTO dto = new LoginDTO();
        dto.setUsuario("joao123");
        dto.setSenha("senha123");
        dto.setTelefone("19999887766");
        dto.setTipoPerfil(TipoPerfil.PESSOA_FISICA);
        return dto;
    }

    private LoginAtualizarDTO loginAtualizarDtoValido() {
        LoginAtualizarDTO dto = new LoginAtualizarDTO();
        dto.setUsuario("joao123");
        dto.setTelefone("19999887766");
        dto.setTipoPerfil(TipoPerfil.PESSOA_FISICA);
        return dto;
    }

    private AlterarSenhaDTO alterarSenhaDtoValido() {
        AlterarSenhaDTO dto = new AlterarSenhaDTO();
        dto.setSenhaAtual("senhaAntiga123");
        dto.setNovaSenha("senhaNova123");
        return dto;
    }

    // ---------- GET /login ----------
    @Test
    void deveListarLoginsComPaginacaoPadrao() throws Exception {
        Page<Login> pagina = new PageImpl<>(List.of(loginSalvo(1L)), PageRequest.of(0, 20), 1);
        when(loginService.listarTodos(any(), any(), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].usuario").value("joao123"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void deveAplicarFiltrosDeRoleETipoPerfil() throws Exception {
        when(loginService.listarTodos(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/login").param("role", "ADMIN").param("tipoPerfil", "LOJA"))
                .andExpect(status().isOk());

        ArgumentCaptor<String> roleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TipoPerfil> tipoCaptor = ArgumentCaptor.forClass(TipoPerfil.class);
        verify(loginService).listarTodos(roleCaptor.capture(), tipoCaptor.capture(), any(Pageable.class));

        assertThat(roleCaptor.getValue()).isEqualTo("ADMIN");
        assertThat(tipoCaptor.getValue()).isEqualTo(TipoPerfil.LOJA);
    }

    // ---------- GET /login/{id} ----------
    @Test
    void deveRetornarLoginPorIdQuandoExiste() throws Exception {
        when(loginService.buscarPorId(1L)).thenReturn(Optional.of(loginSalvo(1L)));

        mockMvc.perform(get("/login/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuario").value("joao123"));
    }

    @Test
    void deveRetornar404QuandoLoginNaoExiste() throws Exception {
        when(loginService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/login/99"))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /login ----------
    @Test
    void deveCriarLoginERetornar201() throws Exception {
        when(loginService.criar(any(LoginDTO.class))).thenReturn(loginSalvo(1L));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDtoValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuario").value("joao123"));
    }

    @Test
    void deveRetornar400QuandoDtoInvalido() throws Exception {
        LoginDTO dto = loginDtoValido();
        dto.setUsuario(""); // @NotBlank

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação"));
    }

    @Test
    void deveRetornar400QuandoSenhaNaoAtendeARegra() throws Exception {
        LoginDTO dto = loginDtoValido();
        dto.setSenha("semnumero"); // sem dígito

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação"));
    }

    // ---------- PUT /login/{id} ----------
    @Test
    void deveAtualizarLoginERetornar200() throws Exception {
        Login atualizado = loginSalvo(1L);
        atualizado.setTelefone("19988887777");
        when(loginService.atualizar(eq(1L), any(LoginAtualizarDTO.class))).thenReturn(atualizado);

        mockMvc.perform(put("/login/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAtualizarDtoValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("19988887777"));
    }

    @Test
    void deveRetornar404AoAtualizarLoginInexistente() throws Exception {
        when(loginService.atualizar(eq(99L), any(LoginAtualizarDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Login não encontrado: 99"));

        mockMvc.perform(put("/login/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAtualizarDtoValido())))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /login/{id}/senha ----------
    @Test
    void deveAlterarSenhaERetornar200() throws Exception {
        when(loginService.alterarSenha(eq(1L), any(AlterarSenhaDTO.class))).thenReturn(loginSalvo(1L));

        mockMvc.perform(put("/login/1/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alterarSenhaDtoValido())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("joao123"));
    }

    @Test
    void deveRetornar400QuandoNovaSenhaNaoAtendeARegra() throws Exception {
        AlterarSenhaDTO dto = alterarSenhaDtoValido();
        dto.setNovaSenha("semnumero"); // sem dígito

        mockMvc.perform(put("/login/1/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação"));
    }

    @Test
    void deveRetornar404AoAlterarSenhaDeLoginInexistente() throws Exception {
        when(loginService.alterarSenha(eq(99L), any(AlterarSenhaDTO.class)))
                .thenThrow(new RecursoNaoEncontradoException("Login não encontrado: 99"));

        mockMvc.perform(put("/login/99/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alterarSenhaDtoValido())))
                .andExpect(status().isNotFound());
    }

    // ---------- PUT /login/{id}/imagem ----------
    @Test
    void deveAtualizarImagemERetornar200() throws Exception {
        when(loginService.atualizarImagem(eq(1L), any(), any())).thenReturn(loginSalvo(1L));

        MockMultipartFile arquivo = new MockMultipartFile("imagem", "foto.jpg", "image/jpeg", "conteudo".getBytes());

        mockMvc.perform(multipart("/login/1/imagem").file(arquivo).with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("joao123"));
    }

    // ---------- DELETE /login/{id} ----------
    @Test
    void deveDeletarLoginERetornar204() throws Exception {
        mockMvc.perform(delete("/login/1"))
                .andExpect(status().isNoContent());

        verify(loginService).deletar(1L);
    }
}