package br.edu.unifaj.cc.poo.appcompraveiculoserver.repositories;

import br.edu.unifaj.cc.poo.appcompraveiculoserver.entities.Login;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class LoginRepository {

    private final JdbcTemplate jdbc;

    public LoginRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Login> ROW_MAPPER = (rs, rowNum) -> {
        Login l = new Login();
        l.setId(rs.getLong("id"));
        l.setUsuario(rs.getString("usuario"));
        l.setSenha(rs.getString("senha"));
        l.setTelefone(rs.getString("telefone"));
        l.setLoginImagem(rs.getString("loginImagem"));
        l.setLoginCriadoEm(rs.getTimestamp("loginCriadoEm").toLocalDateTime());
        l.setLoginAtualizadoEm(rs.getTimestamp("loginAtualizadoEm").toLocalDateTime());
        return l;
    };

    public List<Login> findAll() {
        return jdbc.query("SELECT * FROM login ORDER BY id", ROW_MAPPER);
    }

    public Optional<Login> findById(Long id) {
        return jdbc.query("SELECT * FROM login WHERE id = ?", ROW_MAPPER, id)
                .stream().findFirst();
    }

    public Optional<Login> findByUsuario(String usuario) {
        return jdbc.query("SELECT * FROM login WHERE usuario = ?", ROW_MAPPER, usuario)
                .stream().findFirst();
    }

    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM login WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public Login save(Login login) {
        return login.getId() == null ? insert(login) : update(login);
    }

    private Login insert(Login login) {
        LocalDateTime agora = LocalDateTime .now();
        login.setLoginCriadoEm(agora);
        login.setLoginAtualizadoEm(agora);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO login (usuario, senha, telefone, loginImagem, loginCriadoEm, loginAtualizadoEm) VALUES (?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, login.getUsuario());
            ps.setString(2, login.getSenha());
            ps.setString(3, login.getTelefone());
            ps.setString(5, login.getLoginImagem());
            ps.setTimestamp(6, Timestamp.valueOf(login.getLoginCriadoEm()));
            ps.setTimestamp(7, Timestamp.valueOf(login.getLoginAtualizadoEm()));
            return ps;
        }, keyHolder);

        login.setId(keyHolder.getKey().longValue());
        return login;
    }

    private Login update(Login login) {
        login.setLoginAtualizadoEm(LocalDateTime.now());
        jdbc.update("UPDATE login SET usuario=?, senha=?, telefone=?, loginImagem=?, loginAtualizadoEm=? WHERE id=?",
                login.getUsuario(), login.getSenha(), login.getTelefone(),login.getLoginImagem(),
                Timestamp.valueOf(login.getLoginAtualizadoEm()), login.getId());
        return login;
    }

    public void deleteById(Long id) {
        jdbc.update("DELETE FROM login WHERE id = ?", id);
    }
}