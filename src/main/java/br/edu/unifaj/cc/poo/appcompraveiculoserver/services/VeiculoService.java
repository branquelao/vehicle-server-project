package br.edu.unifaj.cc.poo.appcompraveiculoserver.services;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.dto.veiculo.VeiculoFiltroDTO;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.*;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoVeiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.ImagemInvalidaException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.exceptions.RecursoNaoEncontradoException;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.LoginRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.OpcionalRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.VeiculoRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.specification.VeiculoSpecification;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.util.UploadPathResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.ValidationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final LoginRepository loginRepository;
    private final OpcionalRepository opcionalRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, LoginRepository loginRepository, OpcionalRepository opcionalRepository) {
        this.veiculoRepository = veiculoRepository;
        this.loginRepository = loginRepository;
        this.opcionalRepository = opcionalRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Veiculo> buscarPorId(Long id) {
        return veiculoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarRecentes() {
        return veiculoRepository.findTop3ByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Page<Veiculo> buscar(VeiculoFiltroDTO filtro, Pageable pageable) {
        return veiculoRepository.findAll(VeiculoSpecification.comFiltros(filtro), pageable);
    }

    private void verificarPermissao(Login dono) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuarioLogado = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !dono.getUsuario().equals(usuarioLogado)) {
            throw new AccessDeniedException("Você não tem permissão para modificar este anúncio");
        }
    }

    private void validarCamposPorTipo(VeiculoDTO dto) {
        if (dto.getTipo() == TipoVeiculo.CARRO) {
            if (dto.getCarroceria() == null) {
                throw new ValidationException("Carroceria é obrigatória para carro");
            }
            if (dto.getPortas() == null) {
                throw new ValidationException("Número de portas é obrigatório para carro");
            }
        } else if (dto.getTipo() == TipoVeiculo.MOTO) {
            if (dto.getCilindradaMoto() == null) {
                throw new ValidationException("Cilindrada é obrigatória para moto");
            }
            if (dto.getCategoriaMoto() == null) {
                throw new ValidationException("Categoria é obrigatória para moto");
            }
        }
    }

    private void validarImagens(List<String> nomesImagem, Path uploadDir) {
        if (nomesImagem == null || nomesImagem.isEmpty()) {
            throw new ImagemInvalidaException("Ao menos uma imagem é obrigatória");
        }
        for (String nome : nomesImagem) {
            Path caminho;
            try {
                caminho = UploadPathResolver.resolveDentroDeUploads(uploadDir, nome);
            } catch (IllegalArgumentException e) {
                throw new ImagemInvalidaException("Nome de imagem inválido: " + nome);
            }
            if (!Files.exists(caminho)) {
                throw new ImagemInvalidaException("A imagem '" + nome + "' não foi encontrada em /uploads/");
            }
        }
    }

    private Set<Opcional> buscarOpcionais(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.HashSet<>();
        }
        List<Opcional> encontrados = opcionalRepository.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw new RecursoNaoEncontradoException("Um ou mais opcionais informados não existem");
        }
        return new java.util.HashSet<>(encontrados);
    }

    @Transactional
    public Veiculo criar(VeiculoDTO dto, Path uploadDir) {
        validarCamposPorTipo(dto);
        validarImagens(dto.getImagens(), uploadDir);

        Login login = loginRepository.findById(dto.getLoginId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Login não encontrado: " + dto.getLoginId()));

        Veiculo veiculo = montarVeiculo(new Veiculo(), dto, login);

        // Imagens
        List<VeiculoImagem> imagens = dto.getImagens().stream()
                .map(nome -> {
                    boolean principal = dto.getImagens().indexOf(nome) == 0;
                    int ordem = dto.getImagens().indexOf(nome);
                    return new VeiculoImagem(veiculo, nome, principal, ordem);
                })
                .collect(Collectors.toList());
        veiculo.setImagens(imagens);

        // Opcionais
        veiculo.setOpcionais(buscarOpcionais(dto.getOpcionalIds()));

        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public Veiculo atualizar(Long id, VeiculoDTO dto, Path uploadDir) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado: " + id));

        verificarPermissao(veiculo.getLogin());
        validarCamposPorTipo(dto);

        if (dto.getImagens() != null && !dto.getImagens().isEmpty()) {
            validarImagens(dto.getImagens(), uploadDir);
            veiculo.getImagens().clear();
            List<VeiculoImagem> novasImagens = dto.getImagens().stream()
                    .map(nome -> {
                        boolean principal = dto.getImagens().indexOf(nome) == 0;
                        int ordem = dto.getImagens().indexOf(nome);
                        return new VeiculoImagem(veiculo, nome, principal, ordem);
                    })
                    .collect(Collectors.toList());
            veiculo.getImagens().addAll(novasImagens);
        }

        montarVeiculo(veiculo, dto, veiculo.getLogin());
        veiculo.setOpcionais(buscarOpcionais(dto.getOpcionalIds()));

        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public void deletar(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado: " + id));

        verificarPermissao(veiculo.getLogin());

        veiculoRepository.deleteById(id);
    }

    private Veiculo montarVeiculo(Veiculo veiculo, VeiculoDTO dto, Login login) {
        veiculo.setTipo(dto.getTipo());
        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setAnoFabricacao(dto.getAnoFabricacao());
        veiculo.setAnoModelo(dto.getAnoModelo());
        veiculo.setKm(dto.getKm());
        veiculo.setCor(dto.getCor());
        veiculo.setCombustivel(dto.getCombustivel());
        veiculo.setCambio(dto.getCambio());
        veiculo.setUnicoDono(dto.isUnicoDono());
        veiculo.setAceitaTroca(dto.isAceitaTroca());
        veiculo.setEstadoConservacao(dto.getEstadoConservacao());
        veiculo.setValor(dto.getValor());
        veiculo.setDescricao(dto.getDescricao());
        veiculo.setCarroceria(dto.getCarroceria());
        veiculo.setPortas(dto.getPortas());
        veiculo.setPotenciaCv(dto.getPotenciaCv());
        veiculo.setCilindradaCarro(dto.getCilindradaCarro());
        veiculo.setBlindado(dto.getBlindado());
        veiculo.setCilindradaMoto(dto.getCilindradaMoto());
        veiculo.setCategoriaMoto(dto.getCategoriaMoto());
        veiculo.setTipoPartida(dto.getTipoPartida());
        veiculo.setCidade(dto.getCidade());
        veiculo.setEstado(dto.getEstado());
        if (dto.getStatus() != null) {
            veiculo.setStatus(dto.getStatus());
        }
        veiculo.setLogin(login);
        return veiculo;
    }
}