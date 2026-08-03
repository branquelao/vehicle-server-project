package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Favorito;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.FavoritoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritoRepository extends JpaRepository<Favorito, FavoritoId> {

    List<Favorito> findByLogin_IdOrderByCriadoEmDesc(Long loginId);

    boolean existsByLogin_IdAndVeiculo_Id(Long loginId, Long veiculoId);

    void deleteByLogin_IdAndVeiculo_Id(Long loginId, Long veiculoId);
}