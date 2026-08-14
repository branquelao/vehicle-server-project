package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoFiltroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.*;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.security.CustomUserDetailsService;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.security.JwtService;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.VeiculoService;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de integração do VeiculoController via MockMvc.
 * Sobe apenas a camada web (@WebMvcTest) com o VeiculoService mockado, foco em
 * roteamento, serialização de request/response, status HTTP, paginação/ordenação
 * segura e o tratamento de erro centralizado (GlobalExceptionHandler).
 */
@WebMvcTest(VeiculoController.class)
@AutoConfigureMockMvc(addFilters = false)
class VeiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VeiculoService veiculoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Veiculo veiculoSalvo(Long id) {
        Login login = new Login();
        login.setId(1L);
        login.setUsuario("joao123");

        Veiculo veiculo = new Veiculo();
        veiculo.setId(id);
        veiculo.setTipo(TipoVeiculo.CARRO);
        veiculo.setMarca("Volkswagen");
        veiculo.setModelo("Fusca");
        veiculo.setAnoFabricacao(1972);
        veiculo.setAnoModelo(1972);
        veiculo.setKm(85000);
        veiculo.setCor("Azul");
        veiculo.setCombustivel(Combustivel.GASOLINA);
        veiculo.setCambio(Cambio.MANUAL);
        veiculo.setEstadoConservacao(EstadoConservacao.USADO);
        veiculo.setValor(15000f);
        veiculo.setCarroceria(Carroceria.HATCH);
        veiculo.setPortas(2);
        veiculo.setStatus(StatusAnuncio.ATIVO);
        veiculo.setCidade("Pedreira");
        veiculo.setEstado("SP");
        veiculo.setLogin(login);
        veiculo.setImagens(new ArrayList<>());
        veiculo.setOpcionais(new HashSet<>());
        return veiculo;
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

    // ---------- GET /veiculos ----------
    @Test
    void deveListarVeiculosComPaginacaoPadrao() throws Exception {
        Page<Veiculo> pagina = new PageImpl<>(List.of(veiculoSalvo(1L)), PageRequest.of(0, 20), 1);
        when(veiculoService.buscar(any(VeiculoFiltroDTO.class), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].marca").value("Volkswagen"))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.paginaAtual").value(0))
                .andExpect(jsonPath("$.primeira").value(true));
    }

    @Test
    void deveAplicarFiltrosInformadosNaQuery() throws Exception {
        Page<Veiculo> pagina = new PageImpl<>(List.of(veiculoSalvo(1L)));
        when(veiculoService.buscar(any(VeiculoFiltroDTO.class), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/veiculos")
                        .param("marca", "volkswagen")
                        .param("precoMax", "30000")
                        .param("cidade", "pedreira"))
                .andExpect(status().isOk());

        ArgumentCaptor<VeiculoFiltroDTO> captor = ArgumentCaptor.forClass(VeiculoFiltroDTO.class);
        verify(veiculoService).buscar(captor.capture(), any(Pageable.class));

        VeiculoFiltroDTO filtroUsado = captor.getValue();
        assertThat(filtroUsado.getMarca()).isEqualTo("volkswagen");
        assertThat(filtroUsado.getPrecoMax()).isEqualTo(30000f);
        assertThat(filtroUsado.getCidade()).isEqualTo("pedreira");
    }

    @Test
    void deveUsarOrdenacaoPadraoQuandoCampoDeOrdenacaoNaoPermitido() throws Exception {
        when(veiculoService.buscar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/veiculos").param("sort", "campoInvalido,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(veiculoService).buscar(any(), captor.capture());

        Sort.Order ordem = captor.getValue().getSort().getOrderFor("anunciadoEm");
        assertThat(ordem).isNotNull();
        assertThat(ordem.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void deveAplicarOrdenacaoQuandoCampoPermitido() throws Exception {
        when(veiculoService.buscar(any(), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/veiculos").param("sort", "valor,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(veiculoService).buscar(any(), captor.capture());

        Sort.Order ordem = captor.getValue().getSort().getOrderFor("valor");
        assertThat(ordem).isNotNull();
        assertThat(ordem.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void deveRetornar400QuandoTipoInvalidoNaQuery() throws Exception {
        mockMvc.perform(get("/veiculos").param("tipo", "CAMINHAO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Parâmetro inválido"));
    }

    // ---------- GET /veiculos/{id} ----------
    @Test
    void deveRetornarVeiculoPorIdQuandoExiste() throws Exception {
        when(veiculoService.buscarPorId(1L)).thenReturn(Optional.of(veiculoSalvo(1L)));

        mockMvc.perform(get("/veiculos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.modelo").value("Fusca"))
                .andExpect(jsonPath("$.loginId").value(1));
    }

    @Test
    void deveRetornar404QuandoVeiculoNaoExiste() throws Exception {
        when(veiculoService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/veiculos/99"))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /veiculos/recentes ----------
    @Test
    void deveListarVeiculosRecentes() throws Exception {
        when(veiculoService.listarRecentes()).thenReturn(List.of(veiculoSalvo(1L), veiculoSalvo(2L)));

        mockMvc.perform(get("/veiculos/recentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ---------- POST /veiculos ----------
    @Test
    void deveCriarVeiculoERetornar201() throws Exception {
        when(veiculoService.criar(any(VeiculoDTO.class), any())).thenReturn(veiculoSalvo(1L));

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoDtoValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.marca").value("Volkswagen"));
    }

    @Test
    void deveRetornar400QuandoDtoInvalido() throws Exception {
        VeiculoDTO dto = veiculoDtoValido();
        dto.setMarca(""); // @NotBlank

        mockMvc.perform(post("/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Erro de validação"))
                .andExpect(jsonPath("$.detalhes", not(empty())));
    }

    // ---------- PUT /veiculos/{id} ----------
    @Test
    void deveAtualizarVeiculoERetornar200() throws Exception {
        Veiculo atualizado = veiculoSalvo(1L);
        atualizado.setValor(20000f);
        when(veiculoService.atualizar(eq(1L), any(VeiculoDTO.class), any())).thenReturn(atualizado);

        VeiculoDTO dto = veiculoDtoValido();
        dto.setValor(20000f);

        mockMvc.perform(put("/veiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(20000.0));
    }

    @Test
    void deveRetornar404AoAtualizarVeiculoInexistente() throws Exception {
        when(veiculoService.atualizar(eq(99L), any(VeiculoDTO.class), any()))
                .thenThrow(new RecursoNaoEncontradoException("Veículo não encontrado: 99"));

        mockMvc.perform(put("/veiculos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoDtoValido())))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /veiculos/{id} ----------
    @Test
    void deveDeletarVeiculoERetornar204() throws Exception {
        mockMvc.perform(delete("/veiculos/1"))
                .andExpect(status().isNoContent());

        verify(veiculoService).deletar(1L);
    }
}