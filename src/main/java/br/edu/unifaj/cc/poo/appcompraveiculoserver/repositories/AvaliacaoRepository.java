package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByAvaliador_IdAndAvaliado_Id(Long avaliadorId, Long avaliadoId);

    List<Avaliacao> findByAvaliado_IdOrderByCriadaEmDesc(Long avaliadoId);

    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.avaliado.id = :avaliadoId")
    Double calcularMediaPorAvaliado(@Param("avaliadoId") Long avaliadoId);

    long countByAvaliado_Id(Long avaliadoId);
}