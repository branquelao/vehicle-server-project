package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotoDTO {

    @NotBlank(message = "Nome da moto é obrigatório")
    @Size(max = 50, message = "Nome da moto deve ter no máximo 50 caracteres")
    private String motoNome;

    @NotBlank(message = "Cor é obrigatória")
    @Size(max = 25, message = "Cor deve ter no máximo 25 caracteres")
    private String motoCor;

    @Min(value = 1950, message = "Ano deve ser maior ou igual a 1950")
    @Max(value = 2100, message = "Ano inválido")
    private int motoAno;

    @Positive(message = "Valor deve ser maior que zero")
    private float motoValor;

    @NotBlank(message = "Imagem é obrigatória")
    private String motoImagem;

    @NotNull(message = "Login (loginId) é obrigatório")
    private Long loginId;
}