package br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private String usuario;
    private String role;
}