package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.AlterarSenhaDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginAtualizarDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.login.LoginDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoPerfil;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public Page<Login> listarTodos(String role, TipoPerfil tipoPerfil, Pageable pageable) {
        boolean temRole = role != null && !role.isBlank();
        boolean temTipoPerfil = tipoPerfil != null;

        if (temRole && temTipoPerfil) {
            return loginRepository.findByRoleIgnoreCaseAndTipoPerfil(role, tipoPerfil, pageable);
        }
        if (temRole) {
            return loginRepository.findByRoleIgnoreCase(role, pageable);
        }
        if (temTipoPerfil) {
            return loginRepository.findByTipoPerfil(tipoPerfil, pageable);
        }
        return loginRepository.findAll(pageable);
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
        validarCamposPorTipoPerfil(dto.getTipoPerfil(), dto.getRazaoSocial(), dto.getCnpj());

        Login login = new Login();
        login.setUsuario(dto.getUsuario());
        login.setSenha(passwordEncoder.encode(dto.getSenha()));
        login.setTelefone(dto.getTelefone());
        login.setTipoPerfil(dto.getTipoPerfil());
        login.setRazaoSocial(dto.getTipoPerfil() == TipoPerfil.LOJA ? dto.getRazaoSocial() : null);
        login.setCnpj(dto.getTipoPerfil() == TipoPerfil.LOJA ? dto.getCnpj() : null);
        return loginRepository.save(login);
    }

    private void validarCamposPorTipoPerfil(TipoPerfil tipoPerfil, String razaoSocial, String cnpj) {
        if (tipoPerfil == TipoPerfil.LOJA) {
            if (razaoSocial == null || razaoSocial.isBlank()) {
                throw new ValidationException("Razão social é obrigatória para perfil de loja");
            }
            if (cnpj == null || cnpj.isBlank()) {
                throw new ValidationException("CNPJ é obrigatório para perfil de loja");
            }
        }
    }

    public Login atualizar(Long id, LoginAtualizarDTO dto) {
        Login login = loginRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Login não encontrado: " + id));

        verificarPermissao(login);
        validarCamposPorTipoPerfil(dto.getTipoPerfil(), dto.getRazaoSocial(), dto.getCnpj());

        login.setUsuario(dto.getUsuario());
        login.setTelefone(dto.getTelefone());
        login.setTipoPerfil(dto.getTipoPerfil());
        login.setRazaoSocial(dto.getTipoPerfil() == TipoPerfil.LOJA ? dto.getRazaoSocial() : null);
        login.setCnpj(dto.getTipoPerfil() == TipoPerfil.LOJA ? dto.getCnpj() : null);

        return loginRepository.save(login);
    }

    public Login alterarSenha(Long id, AlterarSenhaDTO dto) {
        Login login = loginRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Login não encontrado: " + id));

        verificarPermissao(login);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean donoTrocandoAPropriaSenha = auth.getName().equals(login.getUsuario());

        if (donoTrocandoAPropriaSenha) {
            if (dto.getSenhaAtual() == null || dto.getSenhaAtual().isBlank()) {
                throw new ValidationException("Senha atual é obrigatória para trocar a própria senha");
            }
            if (!passwordEncoder.matches(dto.getSenhaAtual(), login.getSenha())) {
                throw new ValidationException("Senha atual incorreta");
            }
        }
        // ADMIN trocando a senha de outra conta (reset): não exige senha atual.

        login.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
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