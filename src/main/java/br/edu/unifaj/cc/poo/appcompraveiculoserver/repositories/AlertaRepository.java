package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByBuscaSalva_Login_IdOrderByCriadoEmDesc(Long loginId);

    boolean existsByBuscaSalva_IdAndVeiculo_Id(Long buscaSalvaId, Long veiculoId);
}