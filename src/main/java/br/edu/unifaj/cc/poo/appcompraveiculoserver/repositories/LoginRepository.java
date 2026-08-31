package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.enums.TipoPerfil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Long> {

    Optional<Login> findByUsuario(String usuario);

    Page<Login> findByRoleIgnoreCase(String role, Pageable pageable);

    Page<Login> findByTipoPerfil(TipoPerfil tipoPerfil, Pageable pageable);

    Page<Login> findByRoleIgnoreCaseAndTipoPerfil(String role, TipoPerfil tipoPerfil, Pageable pageable);
}