package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Conversa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    Optional<Conversa> findByVeiculo_IdAndComprador_Id(Long veiculoId, Long compradorId);

    List<Conversa> findByComprador_IdOrVendedor_IdOrderByAtualizadaEmDesc(Long compradorId, Long vendedorId);

    boolean existsByComprador_IdAndVendedor_Id(Long compradorId, Long vendedorId);
}