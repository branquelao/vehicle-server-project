package br.edu.unifaj.cc.poo.appcompraveiculoserver.controllers;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.ErroResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.PaginaResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.AlterarSenhaDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginAtualizarDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginResponseDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoPerfil;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.services.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Tag(name = "Usuários", description = "Cadastro, consulta e gestão de contas de usuário (compradores e vendedores)")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    private Path uploadDir() {
        return Paths.get(System.getProperty("user.dir"), "uploads");
    }

    private static final Set<String> CAMPOS_ORDENACAO_PERMITIDOS_LOGIN = Set.of(
            "usuario", "role", "tipoPerfil", "loginCriadoEm", "loginAtualizadoEm"
    );
    private static final Sort ORDENACAO_PADRAO_LOGIN = Sort.by(Sort.Direction.DESC, "loginCriadoEm");

    private Pageable pageableSeguro(Pageable pageable) {
        List<Sort.Order> validos = pageable.getSort().stream()
                .filter(o -> CAMPOS_ORDENACAO_PERMITIDOS_LOGIN.contains(o.getProperty()))
                .toList();
        Sort sortFinal = validos.isEmpty() ? ORDENACAO_PADRAO_LOGIN : Sort.by(validos);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortFinal);
    }

    @Operation(
            summary = "Listar usuários",
            description = "Retorna usuários cadastrados, com paginação e filtros opcionais por role e tipoPerfil. Restrito a administradores."
    )
    @ApiResponse(responseCode = "200", description = "Página de usuários retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PaginaResponseDTO.class)))
    @GetMapping("/login")
    public PaginaResponseDTO<LoginResponseDTO> getLogins(
            @Parameter(description = "Filtra por role (USER ou ADMIN)") @RequestParam(required = false) String role,
            @Parameter(description = "Filtra por tipo de perfil") @RequestParam(required = false) TipoPerfil tipoPerfil,
            @Parameter(description = "Campos aceitos: usuario, role, tipoPerfil, loginCriadoEm, loginAtualizadoEm")
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Login> pagina = loginService.listarTodos(role, tipoPerfil, pageableSeguro(pageable));
        return PaginaResponseDTO.fromPage(pagina, LoginResponseDTO::fromEntity);
    }

    @Operation(
            summary = "Buscar usuário por ID",
            description = "Retorna os dados de um usuário específico. Restrito ao próprio dono da conta ou a um administrador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para visualizar este usuário",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @GetMapping("/login/{id}")
    public ResponseEntity<LoginResponseDTO> getLoginId(@PathVariable Long id) {
        return loginService.buscarPorId(id)
                .map(login -> ResponseEntity.ok(LoginResponseDTO.fromEntity(login)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria uma nova conta de usuário. Endpoint público, não exige autenticação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de cadastro inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @SecurityRequirements // público
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> postLogin(@Valid @RequestBody LoginDTO dto) {
        Login salvo = loginService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponseDTO.fromEntity(salvo));
    }

    @Operation(
            summary = "Atualizar dados do usuário",
            description = "Atualiza usuário, telefone e tipo de perfil. Não altera a senha, use " +
                    "PUT /login/{id}/senha para isso. Apenas o próprio dono da conta ou um ADMIN pode " +
                    "realizar essa operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar esta conta",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PutMapping("/login/{id}")
    public ResponseEntity<LoginResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody LoginAtualizarDTO dto) {
        Login salvo = loginService.atualizar(id, dto);
        return ResponseEntity.ok(LoginResponseDTO.fromEntity(salvo));
    }

    @Operation(
            summary = "Alterar senha",
            description = "Troca a senha do usuário. O próprio dono da conta precisa informar a senha atual " +
                    "para confirmar a troca. Um ADMIN pode resetar a senha de qualquer conta sem informar a " +
                    "senha atual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha atual incorreta",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar esta conta",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PutMapping("/login/{id}/senha")
    public ResponseEntity<LoginResponseDTO> alterarSenha(@PathVariable Long id, @Valid @RequestBody AlterarSenhaDTO dto) {
        Login salvo = loginService.alterarSenha(id, dto);
        return ResponseEntity.ok(LoginResponseDTO.fromEntity(salvo));
    }

    @Operation(
            summary = "Atualizar imagem de perfil",
            description = "Substitui a imagem de perfil do usuário. A imagem anterior é removida do disco. " +
                    "Apenas o próprio dono da conta ou um ADMIN pode realizar essa operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagem atualizada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar esta conta",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Falha ao processar o upload da imagem",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @PutMapping(value = "/login/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LoginResponseDTO> atualizarImagem(
            @PathVariable Long id,
            @Parameter(description = "Arquivo de imagem (jpg, png, etc.)")
            @RequestParam("imagem") MultipartFile imagem) throws IOException {
        Login salvo = loginService.atualizarImagem(id, imagem, uploadDir());
        return ResponseEntity.ok(LoginResponseDTO.fromEntity(salvo));
    }

    @Operation(
            summary = "Excluir usuário",
            description = "Remove a conta do usuário. Apenas o próprio dono da conta ou um ADMIN pode realizar essa operação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir esta conta",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    @DeleteMapping("/login/{id}")
    public ResponseEntity<Void> deletarLogin(@PathVariable Long id) {
        loginService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}