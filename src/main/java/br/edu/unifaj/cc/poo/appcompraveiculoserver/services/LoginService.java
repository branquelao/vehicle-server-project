package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {

    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Login> listarTodos() {
        return loginRepository.findAll();
    }

    public Optional<Login> buscarPorId(Long id) {
        Optional<Login> login = loginRepository.findById(id);
        login.ifPresent(this::verificarPermissao);
        return login;
    }

    private void verificarPermissao(Login alvo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogado = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !alvo.getUsuario().equals(usuarioLogado)) {
            throw new AccessDeniedException("Você não tem permissão para modificar esta conta");
        }
    }

    public Login criar(LoginDTO dto) {
        Login login = new Login();
        login.setUsuario(dto.getUsuario());
        login.setSenha(passwordEncoder.encode(dto.getSenha()));
        login.setTelefone(dto.getTelefone());
        return loginRepository.save(login);
    }

    public Login atualizar(Long id, LoginDTO dto) {
        Login login = loginRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Login não encontrado: " + id));

        verificarPermissao(login);

        login.setUsuario(dto.getUsuario());
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            login.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        login.setTelefone(dto.getTelefone());

        return loginRepository.save(login);
    }

    public Login atualizarImagem(Long id, MultipartFile imagem, Path uploadDir) throws IOException {
        Login login = loginRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Login não encontrado: " + id));

        verificarPermissao(login);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        UploadPathResolver.apagarSeExistir(uploadDir, login.getLoginImagem());

        String extensao = extrairExtensao(imagem.getOriginalFilename());
        String nomeArquivo = UUID.randomUUID() + extensao;
        Path destino = UploadPathResolver.resolveDentroDeUploads(uploadDir, nomeArquivo);
        Files.copy(imagem.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        login.setLoginImagem(nomeArquivo);
        return loginRepository.save(login);
    }

    public void deletar(Long id) {
        Login login = loginRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Login não encontrado: " + id));

        verificarPermissao(login);

        loginRepository.deleteById(id);
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            return "";
        }
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }
}