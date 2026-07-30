package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDTO {

    @NotNull(message = "Tipo é obrigatório")
    private TipoVeiculo tipo;

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 50)
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 50)
    private String modelo;

    @Min(value = 1950, message = "Ano de fabricação inválido")
    @Max(value = 2100, message = "Ano de fabricação inválido")
    private int anoFabricacao;

    @Min(value = 1950, message = "Ano modelo inválido")
    @Max(value = 2100, message = "Ano modelo inválido")
    private int anoModelo;

    @PositiveOrZero(message = "Km não pode ser negativo")
    private int km;

    @NotBlank(message = "Cor é obrigatória")
    @Size(max = 25)
    private String cor;

    @NotNull(message = "Combustível é obrigatório")
    private Combustivel combustivel;

    @NotNull(message = "Câmbio é obrigatório")
    private Cambio cambio;

    private boolean unicoDono;
    private boolean aceitaTroca;

    @NotNull(message = "Estado de conservação é obrigatório")
    private EstadoConservacao estadoConservacao;

    @Positive(message = "Valor deve ser maior que zero")
    private float valor;

    @Size(max = 1000)
    private String descricao;

    // Específicos de carro (validados manualmente no Service conforme o tipo)
    private Carroceria carroceria;
    private Integer portas;
    private Integer potenciaCv;
    private String cilindradaCarro;
    private Boolean blindado;

    // Específicos de moto
    private Integer cilindradaMoto;
    private CategoriaMoto categoriaMoto;
    private TipoPartida tipoPartida;

    @NotNull(message = "Login (loginId) é obrigatório")
    private Long loginId;

    private List<String> imagens; // nomes de arquivo já enviados via /uploads

    private Set<Long> opcionalIds; // ids do catálogo de opcionais
}