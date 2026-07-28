package br.edu.unifaj.cc.poo.appcompraveiculoserver.security;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class LoginUserDetails implements UserDetails {

    private final Login login;

    public LoginUserDetails(Login login) {
        this.login = login;
    }

    public Long getId() {
        return login.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + login.getRole()));
    }

    @Override
    public String getPassword() {
        return login.getSenha();
    }

    @Override
    public String getUsername() {
        return login.getUsuario();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}