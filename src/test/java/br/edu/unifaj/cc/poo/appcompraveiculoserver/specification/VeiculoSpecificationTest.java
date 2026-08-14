package br.edu.unifaj.cc.poo.appcompraveiculoserver.specification;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoFiltroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.*;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de VeiculoSpecification usando @DataJpaTest + H2 em memória.
 * Cada teste roda dentro de uma transação que é revertida ao final (padrão do
 * @DataJpaTest), então os dados de um teste não vazam pro próximo.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VeiculoSpecificationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VeiculoRepository veiculoRepository;

    private Login vendedor;

    @BeforeEach
    void setUp() {
        vendedor = new Login();
        vendedor.setUsuario("joao123");
        vendedor.setSenha("hash");
        vendedor.setTelefone("19999887766");
        entityManager.persist(vendedor);
    }

    private Veiculo criarVeiculo(TipoVeiculo tipo, String marca, String modelo, float valor,
                                 int anoModelo, int km, String cor, String cidade, String estado,
                                 StatusAnuncio status) {
        Veiculo veiculo = new Veiculo();
        veiculo.setTipo(tipo);
        veiculo.setMarca(marca);
        veiculo.setModelo(modelo);
        veiculo.setAnoFabricacao(anoModelo);
        veiculo.setAnoModelo(anoModelo);
        veiculo.setKm(km);
        veiculo.setCor(cor);
        veiculo.setCombustivel(Combustivel.FLEX);
        veiculo.setCambio(Cambio.MANUAL);
        veiculo.setEstadoConservacao(EstadoConservacao.USADO);
        veiculo.setValor(valor);
        veiculo.setCidade(cidade);
        veiculo.setEstado(estado);
        veiculo.setStatus(status);
        if (tipo == TipoVeiculo.CARRO) {
            veiculo.setCarroceria(Carroceria.HATCH);
            veiculo.setPortas(4);
        } else {
            veiculo.setCilindradaMoto(150);
            veiculo.setCategoriaMoto(CategoriaMoto.NAKED);
        }
        veiculo.setLogin(vendedor);
        return entityManager.persist(veiculo);
    }

    private VeiculoFiltroDTO filtroVazio() {
        return new VeiculoFiltroDTO(null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private List<Veiculo> buscar(VeiculoFiltroDTO filtro) {
        return veiculoRepository.findAll(VeiculoSpecification.comFiltros(filtro));
    }

    @Test
    void deveRetornarApenasAtivosQuandoNenhumFiltroInformado() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Fiat", "Uno", 20000f, 2015, 60000, "Branco", "Campinas", "SP", StatusAnuncio.VENDIDO);
        entityManager.flush();

        List<Veiculo> resultado = buscar(filtroVazio());

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("Fusca");
    }

    @Test
    void deveFiltrarPorTipo() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.MOTO, "Honda", "CG 160", 12000f, 2020, 15000, "Preta", "Pedreira", "SP", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setTipo(TipoVeiculo.MOTO);

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("CG 160");
    }

    @Test
    void deveFiltrarPorMarcaParcialCaseInsensitive() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Fiat", "Uno", 20000f, 2015, 60000, "Branco", "Campinas", "SP", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setMarca("volks");

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getMarca).containsExactly("Volkswagen");
    }

    @Test
    void deveFiltrarPorFaixaDePreco() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Fiat", "Uno", 40000f, 2015, 60000, "Branco", "Campinas", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Toyota", "Corolla", 90000f, 2021, 20000, "Prata", "Campinas", "SP", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setPrecoMin(20000f);
        filtro.setPrecoMax(50000f);

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("Uno");
    }

    @Test
    void deveFiltrarPorFaixaDeAnoModelo() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Toyota", "Corolla", 90000f, 2021, 20000, "Prata", "Campinas", "SP", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setAnoMin(2000);
        filtro.setAnoMax(2023);

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("Corolla");
    }

    @Test
    void deveFiltrarPorKmMaximo() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Toyota", "Corolla", 90000f, 2021, 20000, "Prata", "Campinas", "SP", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setKmMax(30000);

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("Corolla");
    }

    @Test
    void deveFiltrarPorCorExataCaseInsensitive() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Fiat", "Uno", 20000f, 2015, 60000, "Branco", "Campinas", "SP", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setCor("AZUL");

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("Fusca");
    }

    @Test
    void deveFiltrarPorCidadeExataCaseInsensitive() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Fiat", "Uno", 20000f, 2015, 60000, "Branco", "Campinas", "SP", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setCidade("pedreira");

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("Fusca");
    }

    @Test
    void deveFiltrarPorEstadoIndependenteDeCaixa() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Fiat", "Uno", 20000f, 2015, 60000, "Branco", "Rio de Janeiro", "RJ", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setEstado("sp");

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("Fusca");
    }

    @Test
    void deveRetornarVeiculoComStatusEspecificoQuandoInformado() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.PAUSADO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setStatus(StatusAnuncio.PAUSADO);

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactly("Fusca");
    }

    @Test
    void deveCombinarMultiplosFiltros() {
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Fusca", 15000f, 1972, 85000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.CARRO, "Volkswagen", "Gol", 25000f, 2018, 40000, "Azul", "Pedreira", "SP", StatusAnuncio.ATIVO);
        criarVeiculo(TipoVeiculo.MOTO, "Honda", "CG 160", 12000f, 2020, 15000, "Preta", "Pedreira", "SP", StatusAnuncio.ATIVO);
        entityManager.flush();

        VeiculoFiltroDTO filtro = filtroVazio();
        filtro.setTipo(TipoVeiculo.CARRO);
        filtro.setMarca("volkswagen");
        filtro.setCor("azul");

        List<Veiculo> resultado = buscar(filtro);

        assertThat(resultado).extracting(Veiculo::getModelo).containsExactlyInAnyOrder("Fusca", "Gol");
    }
}