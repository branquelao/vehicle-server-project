package br.edu.unifaj.cc.poo.appcompraveiculoserver.jobs;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Alerta;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.BuscaSalva;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoVeiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.AlertaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.BuscaSalvaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Foco: a regra de match (casaComFiltro) e a orquestração do job (não cria
 * duplicado, não avisa o próprio dono do anúncio, ignora quando não há
 * veículo novo). Sem Spring context — @Scheduled não entra em ação aqui.
 */
@ExtendWith(MockitoExtension.class)
class AlertaMatchingJobTest {

    @Mock
    private VeiculoRepository veiculoRepository;
    @Mock
    private BuscaSalvaRepository buscaSalvaRepository;
    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private AlertaMatchingJob job;

    private Login comprador;
    private Login vendedor;

    @BeforeEach
    void setUp() {
        comprador = new Login();
        comprador.setId(1L);

        vendedor = new Login();
        vendedor.setId(2L);
    }

    private Veiculo veiculo(String marca, float valor, int anoModelo, int km, String cor, String cidade, String estado) {
        Veiculo v = new Veiculo();
        v.setId(100L);
        v.setTipo(TipoVeiculo.CARRO);
        v.setMarca(marca);
        v.setModelo("Modelo X");
        v.setValor(valor);
        v.setAnoModelo(anoModelo);
        v.setKm(km);
        v.setCor(cor);
        v.setCidade(cidade);
        v.setEstado(estado);
        v.setStatus(StatusAnuncio.ATIVO);
        v.setLogin(vendedor);
        return v;
    }

    private BuscaSalva buscaSalva(Login login) {
        BuscaSalva b = new BuscaSalva();
        b.setId(10L);
        b.setLogin(login);
        b.setMarca("Volkswagen");
        b.setPrecoMax(30000f);
        b.setCidade("Pedreira");
        return b;
    }

    // ---------- casaComFiltro() ----------

    @Test
    void casaComFiltro_deveDarMatchQuandoTodosCriteriosBatem() {
        Veiculo v = veiculo("Volkswagen", 25000f, 2018, 40000, "Azul", "Pedreira", "SP");
        BuscaSalva b = buscaSalva(comprador);

        assertThat(job.casaComFiltro(v, b)).isTrue();
    }

    @Test
    void casaComFiltro_deveFalharQuandoPrecoAcimaDoMaximo() {
        Veiculo v = veiculo("Volkswagen", 35000f, 2018, 40000, "Azul", "Pedreira", "SP");
        BuscaSalva b = buscaSalva(comprador);

        assertThat(job.casaComFiltro(v, b)).isFalse();
    }

    @Test
    void casaComFiltro_deveFalharQuandoCidadeDiferente() {
        Veiculo v = veiculo("Volkswagen", 25000f, 2018, 40000, "Azul", "Campinas", "SP");
        BuscaSalva b = buscaSalva(comprador);

        assertThat(job.casaComFiltro(v, b)).isFalse();
    }

    @Test
    void casaComFiltro_deveAceitarMarcaParcialCaseInsensitive() {
        Veiculo v = veiculo("volkswagen", 25000f, 2018, 40000, "Azul", "Pedreira", "SP");
        BuscaSalva b = buscaSalva(comprador);
        b.setMarca("volks");

        assertThat(job.casaComFiltro(v, b)).isTrue();
    }

    // ---------- processar() ----------

    @Test
    void processar_naoDeveConsultarBuscasQuandoNaoHaVeiculoNovo() {
        when(veiculoRepository.findByAnunciadoEmAfterAndStatus(any(LocalDateTime.class), eq(StatusAnuncio.ATIVO)))
                .thenReturn(List.of());

        job.processar();

        verifyNoInteractions(buscaSalvaRepository, alertaRepository);
    }

    @Test
    void processar_naoDeveCriarAlertaQuandoDonoDaBuscaEODonoDoAnuncio() {
        Veiculo veiculoDoProprioVendedor = veiculo("Volkswagen", 25000f, 2018, 40000, "Azul", "Pedreira", "SP");
        BuscaSalva buscaDoVendedor = buscaSalva(vendedor); // mesma pessoa que anunciou

        when(veiculoRepository.findByAnunciadoEmAfterAndStatus(any(LocalDateTime.class), eq(StatusAnuncio.ATIVO)))
                .thenReturn(List.of(veiculoDoProprioVendedor));
        when(buscaSalvaRepository.findAll()).thenReturn(List.of(buscaDoVendedor));

        job.processar();

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void processar_naoDeveDuplicarAlertaJaExistente() {
        Veiculo v = veiculo("Volkswagen", 25000f, 2018, 40000, "Azul", "Pedreira", "SP");
        BuscaSalva b = buscaSalva(comprador);

        when(veiculoRepository.findByAnunciadoEmAfterAndStatus(any(LocalDateTime.class), eq(StatusAnuncio.ATIVO)))
                .thenReturn(List.of(v));
        when(buscaSalvaRepository.findAll()).thenReturn(List.of(b));
        when(alertaRepository.existsByBuscaSalva_IdAndVeiculo_Id(10L, 100L)).thenReturn(true);

        job.processar();

        verify(alertaRepository, never()).save(any());
    }

    @Test
    void processar_deveCriarAlertaQuandoMatchNovoEValido() {
        Veiculo v = veiculo("Volkswagen", 25000f, 2018, 40000, "Azul", "Pedreira", "SP");
        BuscaSalva b = buscaSalva(comprador);

        when(veiculoRepository.findByAnunciadoEmAfterAndStatus(any(LocalDateTime.class), eq(StatusAnuncio.ATIVO)))
                .thenReturn(List.of(v));
        when(buscaSalvaRepository.findAll()).thenReturn(List.of(b));
        when(alertaRepository.existsByBuscaSalva_IdAndVeiculo_Id(10L, 100L)).thenReturn(false);

        job.processar();

        verify(alertaRepository).save(argThat((Alerta a) ->
                a.getBuscaSalva().equals(b) && a.getVeiculo().equals(v)));
    }
}