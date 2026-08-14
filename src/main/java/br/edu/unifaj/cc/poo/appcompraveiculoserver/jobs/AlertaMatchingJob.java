package br.edu.unifaj.cc.poo.appcompraveiculoserver.jobs;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Alerta;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.BuscaSalva;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.AlertaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.BuscaSalvaRepository;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories.VeiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Roda periodicamente e cria um Alerta pra cada par (BuscaSalva, Veiculo) cujos
 * critérios batem, considerando apenas veículos ATIVO anunciados desde a última
 * execução. Roda fora do fluxo de criação de anúncio de propósito: mantém
 * VeiculoService.criar() rápido e simples de testar, e o matching escala
 * independente de quantas buscas salvas existirem.
 */
@Component
public class AlertaMatchingJob {

    private static final Logger log = LoggerFactory.getLogger(AlertaMatchingJob.class);

    private final VeiculoRepository veiculoRepository;
    private final BuscaSalvaRepository buscaSalvaRepository;
    private final AlertaRepository alertaRepository;

    // Marca d'água simples em memória. Suficiente pra uma única instância do app;
    // se um dia rodar com múltiplas réplicas, isso precisa virar uma linha na tabela.
    private LocalDateTime ultimaExecucao = LocalDateTime.now();

    public AlertaMatchingJob(VeiculoRepository veiculoRepository, BuscaSalvaRepository buscaSalvaRepository,
                             AlertaRepository alertaRepository) {
        this.veiculoRepository = veiculoRepository;
        this.buscaSalvaRepository = buscaSalvaRepository;
        this.alertaRepository = alertaRepository;
    }

    @Scheduled(fixedDelayString = "${alertas.job.fixed-delay-ms:300000}")
    @Transactional
    public void processar() {
        LocalDateTime inicioDestaExecucao = LocalDateTime.now();

        List<Veiculo> veiculosNovos = veiculoRepository
                .findByAnunciadoEmAfterAndStatus(ultimaExecucao, StatusAnuncio.ATIVO);

        if (veiculosNovos.isEmpty()) {
            ultimaExecucao = inicioDestaExecucao;
            return;
        }

        List<BuscaSalva> buscas = buscaSalvaRepository.findAll();
        int alertasCriados = 0;

        for (BuscaSalva busca : buscas) {
            for (Veiculo veiculo : veiculosNovos) {
                if (!casaComFiltro(veiculo, busca)) {
                    continue;
                }
                // não avisa o próprio vendedor sobre o próprio anúncio
                if (veiculo.getLogin().getId().equals(busca.getLogin().getId())) {
                    continue;
                }
                if (alertaRepository.existsByBuscaSalva_IdAndVeiculo_Id(busca.getId(), veiculo.getId())) {
                    continue;
                }

                alertaRepository.save(new Alerta(busca, veiculo));
                alertasCriados++;
            }
        }

        if (alertasCriados > 0) {
            log.info("Job de alertas: {} alerta(s) criado(s) a partir de {} veículo(s) novo(s)",
                    alertasCriados, veiculosNovos.size());
        }

        ultimaExecucao = inicioDestaExecucao;
    }

    /**
     * Mesma lógica de comparação da VeiculoSpecification, mas em memória: aqui já
     * temos os veículos candidatos carregados, então não vale a pena ir ao banco
     * de novo pra cada busca salva.
     */
    boolean casaComFiltro(Veiculo v, BuscaSalva b) {
        if (b.getTipo() != null && b.getTipo() != v.getTipo()) return false;
        if (isNotBlank(b.getMarca()) && !contemIgnoreCase(v.getMarca(), b.getMarca())) return false;
        if (isNotBlank(b.getModelo()) && !contemIgnoreCase(v.getModelo(), b.getModelo())) return false;
        if (b.getPrecoMin() != null && v.getValor() < b.getPrecoMin()) return false;
        if (b.getPrecoMax() != null && v.getValor() > b.getPrecoMax()) return false;
        if (b.getAnoMin() != null && v.getAnoModelo() < b.getAnoMin()) return false;
        if (b.getAnoMax() != null && v.getAnoModelo() > b.getAnoMax()) return false;
        if (b.getKmMax() != null && v.getKm() > b.getKmMax()) return false;
        if (isNotBlank(b.getCor()) && !v.getCor().equalsIgnoreCase(b.getCor())) return false;
        if (isNotBlank(b.getCidade()) && !v.getCidade().equalsIgnoreCase(b.getCidade())) return false;
        if (isNotBlank(b.getEstado()) && !v.getEstado().equalsIgnoreCase(b.getEstado())) return false;
        return true;
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private boolean contemIgnoreCase(String valor, String filtro) {
        return valor.toLowerCase().contains(filtro.toLowerCase());
    }
}