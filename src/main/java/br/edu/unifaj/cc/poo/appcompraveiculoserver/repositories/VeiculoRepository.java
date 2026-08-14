package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Veiculo;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.StatusAnuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long>, JpaSpecificationExecutor<Veiculo> {

    List<Veiculo> findTop3ByOrderByIdDesc();

    List<Veiculo> findByLogin_Id(Long loginId);

    List<Veiculo> findByAnunciadoEmAfterAndStatus(LocalDateTime anunciadoEm, StatusAnuncio status);
}