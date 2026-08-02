package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByConversa_IdOrderByEnviadaEmAsc(Long conversaId);
}