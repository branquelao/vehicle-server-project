package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepository extends JpaRepository<Login, Long> {
}
