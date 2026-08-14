package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.BuscaSalva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuscaSalvaRepository extends JpaRepository<BuscaSalva, Long> {

    List<BuscaSalva> findByLogin_IdOrderByCriadaEmDesc(Long loginId);
}