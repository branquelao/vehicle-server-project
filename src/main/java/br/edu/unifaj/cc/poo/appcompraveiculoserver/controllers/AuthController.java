package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.auth.LoginRequestDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.auth.TokenResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(
            summary = "Autenticar usuário",
            description = "Valida usuário e senha e retorna um token JWT (Bearer) a ser usado nos demais endpoints."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuário ou senha inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @SecurityRequirements // endpoint público, sem exigir bearer no Swagger UI
    @PostMapping("/auth/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsuario(), dto.getSenha()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.gerarToken(userDetails);

        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return ResponseEntity.ok(new TokenResponseDTO(token, "Bearer", userDetails.getUsername(), role));
    }
}